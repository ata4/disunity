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

import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
import info.ata4.io.DataRandomAccess;
import info.ata4.unity.gui.util.progress.DummyProgress;
import info.ata4.unity.gui.util.progress.Progress;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.*;
import static java.nio.file.StandardOpenOption.*;
import java.util.ArrayList;
import java.util.List;
import lzma.LzmaDecoder;
import lzma.LzmaEncoder;

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
            for (BundleEntryInfo entry : assetBundle.getEntries()) {
                total += entry.getLength();
            }
            
            progress.setLimit(total);

            for (BundleEntryStreamed entry : assetBundle) {
                if (progress.isCanceled()) {
                    break;
                }
                
                progress.setLabel(entry.getInfo().getName());
                
                Path entryFile = outDir.resolve(entry.getInfo().getName());
                Files.createDirectories(entryFile.getParent());
                Files.copy(entry.getInputStream(), entryFile, REPLACE_EXISTING);
                
                current += entry.getInfo().getLength();
                progress.update(current);
            }
        }
    }
    
    public static void extract(Path file, Path outDir) throws IOException {
        extract(file, outDir, new DummyProgress());
    }
    
    public static List<BundleEntryBuffered> buffer(AssetBundleReader reader, Progress progress) throws IOException {
        long current = 0;
        long total = 0;
        for (BundleEntryInfo entry : reader.getEntries()) {
            total += entry.getLength();
        }

        progress.setLimit(total);

        List<BundleEntryBuffered> entries = new ArrayList<>();
        for (BundleEntryStreamed entry : reader) {
            if (progress.isCanceled()) {
                break;
            }

            progress.setLabel(entry.getInfo().getName());

            entries.add(entry.buffer());

            current += entry.getInfo().getLength();
            progress.update(current);
        }

        return entries;
    }
    
    public static List<BundleEntryBuffered> buffer(AssetBundleReader reader) throws IOException {
        return buffer(reader, new DummyProgress());
    }
    
    public static List<BundleEntryBuffered> buffer(Path file, Progress progress) throws IOException {
        try (AssetBundleReader reader = new AssetBundleReader(file)) {
            return buffer(reader, progress);
        }
    }
    
    public static List<BundleEntryBuffered> buffer(Path file) throws IOException {
        return buffer(file, new DummyProgress());
    }
    
    public static void compress(Path inFile, Path outFile) throws IOException {
        try (
            DataInputReader in = DataInputReader.newReader(inFile);
            DataRandomAccess out = DataRandomAccess.newRandomAccess(outFile, CREATE, READ, WRITE, TRUNCATE_EXISTING);
        ) {
            compress(in, out);
        }
    }
    
    private static void compress(DataInputReader in, DataRandomAccess out) throws IOException {
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

        out.writeStruct(tmpHeader);

        compressData(in, out.getWriter());

        // write header again with fixed file size
        out.position(0);
        tmpHeader.setCompleteFileSize((int) out.size());
        tmpHeader.setMinimumStreamedBytes((int) out.size());
        out.writeStruct(tmpHeader);
    }
    
    private static void compressData(DataInputReader in, DataOutputWriter out) throws IOException {
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
            DataInputReader in = DataInputReader.newBufferedReader(inFile);
            DataOutputWriter out = DataOutputWriter.newBufferedWriter(outFile);
        ) {
            uncompress(in, out);
        }
    }
    
    public static void uncompress(DataInputReader in, DataOutputWriter out) throws IOException {
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

        out.writeStruct(tmpHeader);

        in.setSwap(true);
        uncompressData(in, out);
    }
    
    private static void uncompressData(DataInputReader in, DataOutputWriter out) throws IOException {
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
