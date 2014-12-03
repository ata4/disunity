/*
 ** 2014 September 25
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.assetbundle;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import info.ata4.io.buffer.ByteBufferOutputStream;
import info.ata4.io.socket.IOSocket;
import info.ata4.io.socket.Sockets;
import info.ata4.util.progress.DummyProgress;
import info.ata4.util.progress.Progress;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.*;
import static java.nio.file.StandardOpenOption.*;
import java.util.Properties;
import lzma.LzmaDecoder;
import lzma.LzmaEncoder;
import org.apache.commons.io.IOUtils;

/**
 * Asset bundle file utility class.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetBundleUtils {
    
    private static final String PROPERTIES_FILE = "bundle.properties";
    
    private AssetBundleUtils() {
    }
    
    public static boolean isAssetBundle(Path file) {
        if (!Files.isRegularFile(file)) {
            return false;
        }
        
        try (InputStream is = Files.newInputStream(file)) {
            byte[] header = new byte[8];
            is.read(header);
            String headerString = new String(header, "ASCII");
            return headerString.equals(AssetBundleHeader.SIGNATURE_WEB)
                    || headerString.equals(AssetBundleHeader.SIGNATURE_RAW);
        } catch (IOException ex) {
        }
        
        return false;
    }
    
    public static void extract(Path file, Path outDir, Progress progress) throws IOException {
        try(
            AssetBundleReader assetBundle = new AssetBundleReader(file)
        ) {
            long current = 0;
            long total = 0;
            for (AssetBundleEntryInfo entry : assetBundle.getEntries()) {
                total += entry.getSize();
            }
            
            progress.setLimit(total);

            for (AssetBundleEntry entry : assetBundle) {
                if (progress.isCanceled()) {
                    break;
                }
                
                progress.setLabel(entry.getName());
                
                Path entryFile = outDir.resolve(entry.getName());
                Files.createDirectories(entryFile.getParent());
                Files.copy(entry.getInputStream(), entryFile, REPLACE_EXISTING);
                
                current += entry.getSize();
                progress.update(current);
            }
            
            // create metadata file
            AssetBundleHeader header = assetBundle.getHeader();
            
            Properties props = new Properties();
            props.setProperty("compressed", String.valueOf(header.isCompressed()));
            props.setProperty("streamVersion", String.valueOf(header.getStreamVersion()));
            props.setProperty("unityVersion", header.getUnityVersion().toString());
            props.setProperty("unityRevision", header.getUnityRevision().toString());
            
            Path propsFile = outDir.resolve(PROPERTIES_FILE);
            
            try (Writer out = Files.newBufferedWriter(propsFile,
                    Charset.forName("US-ASCII"), WRITE, CREATE, TRUNCATE_EXISTING)) {
                props.store(out, null);
            }
        }
    }
    
    public static void extract(Path file, Path outDir) throws IOException {
        extract(file, outDir, new DummyProgress());
    }
    
    public static IOSocket getSocketForEntry(AssetBundleEntry entry) throws IOException {
        IOSocket socket;
        
        // check if the entry is larger than 128 MiB
        long size = entry.getSize();
        if (size > 1 << 27) {
            // copy entry to temporary file
            Path tmpFile = Files.createTempFile("disunity", ".assets");
            socket = Sockets.forFile(tmpFile, READ, WRITE, DELETE_ON_CLOSE);
            IOUtils.copy(entry.getInputStream(), socket.getOutputStream());
            socket.getPositionable().position(0);
        } else {
            // copy entry to memory
            ByteBuffer bb = ByteBuffer.allocateDirect((int) size);
            IOUtils.copy(entry.getInputStream(), new ByteBufferOutputStream(bb));
            bb.flip();
            socket = Sockets.forByteBuffer(bb);
        }
        
        return socket;
    }
    
    public static void compress(Path inFile, Path outFile) throws IOException {
        try (
            IOSocket inSocket = Sockets.forBufferedReadFile(inFile);
            IOSocket outSocket = Sockets.forFile(inFile, CREATE, READ, WRITE, TRUNCATE_EXISTING);
        ) {
            compress(inSocket, outSocket);
        }
    }
    
    private static void compress(IOSocket inSocket, IOSocket outSocket) throws IOException {
        DataReader in = new DataReader(inSocket);
        
        AssetBundleHeader tmpHeader = new AssetBundleHeader();
        in.readStruct(tmpHeader);

        // check signature
        if (!tmpHeader.hasValidSignature()) {
            throw new AssetBundleException("Invalid signature");
        }

        if (tmpHeader.isCompressed()) {
            throw new AssetBundleException("Asset bundle is already compressed");
        }

        tmpHeader.setCompressed(true);

        DataWriter out = new DataWriter(outSocket);
        out.writeStruct(tmpHeader);

        compressData(in, out);

        // write header again with fixed file size
        out.position(0);
        tmpHeader.setCompleteFileSize((int) out.size());
        tmpHeader.setMinimumStreamedBytes((int) out.size());
        out.writeStruct(tmpHeader);
    }
    
    private static void compressData(DataReader in, DataWriter out) throws IOException {
        int lc = 3;
        int lp = 0;
        int pb = 2;
        int dictSize = 1 << 23;
        
        LzmaEncoder enc = new LzmaEncoder();
        enc.setEndMarkerMode(true);
        
        if (!enc.setLcLpPb(lc, lp, pb)) {
            throw new IOException("Invalid LZMA props");
        }
        
        if (!enc.setDictionarySize(dictSize)) {
            throw new IOException("Invalid dictionary size");
        }
        
        boolean swap = out.isSwap();
        out.setSwap(true);
        out.write(enc.getCoderProperties());
        out.writeLong(in.remaining());
        out.setSwap(swap);

        try (
            InputStream is = new BufferedInputStream(in.getSocket().getInputStream());
            OutputStream os = new BufferedOutputStream(out.getSocket().getOutputStream());
        ) {
            enc.code(is, os);
        }
    }

    public static void uncompress(Path inFile, Path outFile) throws IOException {
        try (
            IOSocket inSocket = Sockets.forBufferedReadFile(inFile);
            IOSocket outSocket = Sockets.forBufferedWriteFile(inFile);
        ) {
            uncompress(inSocket, outSocket);
        }
    }
    
    public static void uncompress(IOSocket inSocket, IOSocket outSocket) throws IOException {
        DataReader in = new DataReader(inSocket);
        
        AssetBundleHeader tmpHeader = new AssetBundleHeader();
        in.readStruct(tmpHeader);

        // check signature
        if (!tmpHeader.hasValidSignature()) {
            throw new AssetBundleException("Invalid signature");
        }

        if (!tmpHeader.isCompressed()) {
            throw new AssetBundleException("Asset bundle is not compressed");
        }

        tmpHeader.setCompressed(false);

        DataWriter out = new DataWriter(outSocket);
        out.writeStruct(tmpHeader);

        in.setSwap(true);
        uncompressData(in, out);
    }
    
    private static void uncompressData(DataReader in, DataWriter out) throws IOException {
        boolean swap = in.isSwap();
        
        in.setSwap(true);
        byte[] lzmaProps = new byte[5];
        in.readFully(lzmaProps);
        long lzmaSize = in.readLong();
        in.setSwap(swap);

        LzmaDecoder dec = new LzmaDecoder();
        if (!dec.setDecoderProperties(lzmaProps)) {
            throw new IOException("Invalid LZMA props");
        }

        try (
            InputStream is = new BufferedInputStream(in.getSocket().getInputStream());
            OutputStream os = new BufferedOutputStream(out.getSocket().getOutputStream());
        ) {
            if (!dec.code(is, os, lzmaSize)) {
                throw new IOException("LZMA decoding error");
            }
        }
    }
}
