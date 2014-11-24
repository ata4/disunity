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
import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.io.socket.IOSocket;
import info.ata4.io.socket.Sockets;
import info.ata4.util.progress.DummyProgress;
import info.ata4.util.progress.Progress;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.*;
import static java.nio.file.StandardOpenOption.*;
import java.util.ArrayList;
import java.util.List;
import lzma.LzmaDecoder;
import lzma.LzmaEncoder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Asset bundle file utility class.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetBundleUtils {
    
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
        }
    }
    
    public static void extract(Path file, Path outDir) throws IOException {
        extract(file, outDir, new DummyProgress());
    }
    
    public static List<Pair<String, IOSocket>> buffer(AssetBundleReader reader, Progress progress) throws IOException {
        long current = 0;
        long total = 0;
        for (AssetBundleEntryInfo entry : reader.getEntries()) {
            total += entry.getSize();
        }

        progress.setLimit(total);
        
        List<Pair<String, IOSocket>> entries = new ArrayList<>();

        for (AssetBundleEntry entry : reader) {
            if (progress.isCanceled()) {
                break;
            }

            progress.setLabel(entry.getName());

            entries.add(new ImmutablePair<>(entry.getName(), buffer(entry)));

            current += entry.getSize();
            progress.update(current);
        }

        return entries;
    }
    
    public static IOSocket buffer(AssetBundleEntry entry) throws IOException {
        if (entry.getSize() < Integer.MAX_VALUE) {
            ByteBuffer bb = ByteBufferUtils.allocate((int) entry.getSize());
            OutputStream os = new ByteBufferOutputStream(bb);
            IOUtils.copyLarge(entry.getInputStream(), os);
            bb.flip();

            return Sockets.forByteBuffer(bb);
        } else {
            // TODO: create temporary file
            throw new IllegalArgumentException("Entry is too large for buffering");
        }
    }
    
    public static List<Pair<String, IOSocket>> buffer(AssetBundleReader reader) throws IOException {
        return buffer(reader, new DummyProgress());
    }
    
    public static List<Pair<String, IOSocket>> buffer(Path file, Progress progress) throws IOException {
        try (AssetBundleReader reader = new AssetBundleReader(file)) {
            return buffer(reader, progress);
        }
    }
    
    public static List<Pair<String, IOSocket>> buffer(Path file) throws IOException {
        return buffer(file, new DummyProgress());
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
