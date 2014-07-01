/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.asset.bundle;

import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.io.file.FileHandler;
import info.ata4.log.LogUtils;
import info.ata4.unity.asset.bundle.codec.AssetBundleCodec;
import info.ata4.unity.asset.bundle.codec.XianjianCodec;
import info.ata4.unity.asset.bundle.struct.AssetBundleHeader;
import info.ata4.unity.util.UnityVersion;
import info.ata4.util.io.lzma.LzmaBufferUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Reader for Unity asset bundles.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetBundle extends FileHandler {
    
    private static final Logger L = LogUtils.getLogger();
    
    private static final int ALIGN = 4;
    
    private static int align(int length) {
        int rem = length % ALIGN;
        if (rem != 0) {
            length += ALIGN - rem;
        }
        return length;
    }
    
    public static boolean isAssetBundle(Path file) {
        // check signature of the file
        try (DataInputReader in = DataInputReader.newReader(file)) {
            AssetBundleHeader info = new AssetBundleHeader();
            info.setSignature(in.readStringNull());
            return info.hasValidSignature();
        } catch (IOException ex) {
            return false;
        }
    }
    
    private final List<AssetBundleCodec> codecsLoad = new ArrayList<>();
    private final List<AssetBundleCodec> codecsSave = new ArrayList<>();
    private final AssetBundleHeader header = new AssetBundleHeader();
    private final Map<String, ByteBuffer> entries = new LinkedHashMap<>();
    private boolean compressed = false;
    
    public AssetBundle() {
        // register known codecs
        codecsLoad.add(new XianjianCodec());
    }
    
    @Override
    public void load(ByteBuffer bb) throws IOException {
        // decode buffer if required
        for (AssetBundleCodec codec : codecsLoad) {
            if (codec.isEncoded(bb)) {
                L.log(Level.INFO, "Decoding: {0}", codec.getName());
                bb = codec.decode(bb);
                codecsSave.add(codec);
            }
        }
        
        DataInputReader in = DataInputReader.newReader(bb);
        in.readStruct(header);
        
        // check signature
        if (!header.hasValidSignature()) {
            throw new AssetBundleException("Invalid signature");
        }
        
        // check compression flag in the signature
        compressed = header.isCompressed();
        
        // get buffer slice for bundle data
        ByteBuffer bbData = ByteBufferUtils.getSlice(bb, header.getDataOffset());
        
        // uncompress bundle data if required
        if (isCompressed()) {
            L.log(Level.INFO, "Uncompressing asset bundle, this may take a while");
            bbData = LzmaBufferUtils.decode(bbData);
        }

        // read bundle entries
        in = DataInputReader.newReader(bbData);
        int files = in.readInt();
        for (int i = 0; i < files; i++) {
            String name = in.readStringNull();
            int offset = in.readInt();
            int length = in.readInt();
            ByteBuffer bbEntry = ByteBufferUtils.getSlice(bbData, offset, length);
            entries.put(name, bbEntry);
        }
    }

    @Override
    public void save(Path file) throws IOException {
        int bundleHeaderSize = 4;
        int bundleDataSize = 0;
        int assets = 0;
        
        Set<Map.Entry<String, ByteBuffer>> entrySet = entries.entrySet();        
        for (Map.Entry<String, ByteBuffer> entry : entrySet) {
            String name = entry.getKey();
            ByteBuffer buffer = entry.getValue();

            // name length + null byte + 2 ints
            bundleHeaderSize += name.length() + 9;
            
            // count asset files
            if (name.equals("mainData") || name.startsWith("level") || name.startsWith("CAB")) {
                assets++;
            }
            
            bundleDataSize += align(buffer.limit());
        }
        
        // first entry starts after the header
        int bundleDataOffset = align(bundleHeaderSize);
        
        // allocate data buffer
        ByteBuffer bbData = ByteBuffer.allocateDirect(bundleDataOffset + bundleDataSize);
        DataOutputWriter out = DataOutputWriter.newWriter(bbData.duplicate());
        
        // write bundle entries
        out.writeInt(entrySet.size());
        for (Map.Entry<String, ByteBuffer> entry : entrySet) {
            String name = entry.getKey();
            ByteBuffer buffer = entry.getValue();
            buffer.rewind();
            
            out.writeStringNull(name);
            out.writeInt(bundleDataOffset);
            out.writeInt(buffer.limit());
            
            bbData.position(bundleDataOffset);
            bbData.put(buffer);
            
            bundleDataOffset += align(buffer.limit());
        }
        
        bbData.flip();
        
        int dataSizeC = bbData.limit();
        int dataSizeU = dataSizeC;
        
        // compress bundle data if required
        if (isCompressed()) {
            L.log(Level.INFO, "Compressing asset bundle, this may take a while");
            bbData = LzmaBufferUtils.encode(bbData);
            dataSizeC = bbData.limit();
        }
        
        // configure header
        int headerSize = header.getSize();
        int bundleSize = headerSize + dataSizeC;
        header.setCompressed(isCompressed());
        header.setDataOffset(headerSize);
        header.setFileSize1(bundleSize);
        header.setFileSize2(bundleSize);
        header.setUnknown1(assets);
        header.setUnknown2(bundleHeaderSize);
        
        List<Pair<Integer, Integer>> offsetMap = header.getOffsetMap();
        offsetMap.clear();
        
        // TODO: Original asset bundles have ascending lengths for each asset
        // file. The exact calculation of these values is not yet known, so use
        // the maximum size for each entry for now to avoid crashes.
        for (int i = 0; i < assets; i++) {
            offsetMap.add(new ImmutablePair<>(dataSizeC, dataSizeU));
        }
        
        // create bundle buffer
        ByteBuffer bb = ByteBuffer.allocateDirect(bundleSize);
        out = DataOutputWriter.newWriter(bb);
        out.writeStruct(header);
        out.writeBuffer(bbData);
        bb.flip();
        
        // encode bundle buffer
        for (AssetBundleCodec codec : codecsSave) {
            L.log(Level.INFO, "Encoding: {0}", codec.getName());
            bb = codec.encode(bb);
        }
        
        // write buffer to file
        bb.rewind();
        ByteBufferUtils.save(file, bb);
    }
    
    public Map<String, ByteBuffer> getEntries() {
        return entries;
    }
    
    public List<AssetBundleCodec> getLoadCodecs() {
        return codecsLoad;
    }

    public List<AssetBundleCodec> getSaveCodecs() {
        return codecsSave;
    }

    public int getFormat() {
        return header.getFormat();
    }
    
    public void setFormat(byte format) {
        header.setFormat(format);
    }

    public UnityVersion getPlayerVersion() {
        return header.getPlayerVersion();
    }
    
    public void setPlayerVersion(UnityVersion version) {
        header.setPlayerVersion(version);
    }

    public UnityVersion getEngineVersion() {
        return header.getEngineVersion();
    }
    
    public void setEngineVersion(UnityVersion revision) {
        header.setEngineVersion(revision);
    }

    public boolean isCompressed() {
        return compressed;
    }
    
    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }
}
