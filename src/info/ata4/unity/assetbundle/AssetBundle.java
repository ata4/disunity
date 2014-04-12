/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.assetbundle;

import info.ata4.io.DataInputReader;
import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.io.file.FileHandler;
import info.ata4.log.LogUtils;
import info.ata4.unity.assetbundle.struct.AssetBundleHeader;
import info.ata4.unity.util.UnityVersion;
import info.ata4.util.io.lzma.LzmaBufferUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reader for Unity asset bundles.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetBundle extends FileHandler {
    
    private static final Logger L = LogUtils.getLogger();
    
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
    
    private AssetBundleHeader header = new AssetBundleHeader();
    private Map<String, ByteBuffer> entries = new LinkedHashMap<>();
    private boolean compressed = false;
    
    @Override
    public void load(ByteBuffer bb) throws IOException {
        DataInputReader in = DataInputReader.newReader(bb);

        header.read(in);

        // check signature
        if (!header.hasValidSignature()) {
            throw new AssetBundleException("Invalid signature");
        }
        
        compressed = header.getSignature().equals(AssetBundleHeader.SIGNATURE_WEB);

        ByteBuffer bbData = ByteBufferUtils.getSlice(bb, header.getDataOffset());
        
        // uncompress bundle data if required
        if (isCompressed()) {
            L.log(Level.INFO, "Uncompressing {0}, this may take a while", getSourceFile().getFileName());
            bbData = LzmaBufferUtils.decode(bbData);
        }

        in = DataInputReader.newReader(bbData);
        
        // add stored entries
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
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Map<String, ByteBuffer> getEntries() {
        return entries;
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
