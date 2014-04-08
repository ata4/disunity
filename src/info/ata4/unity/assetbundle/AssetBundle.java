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
import info.ata4.io.buffer.ByteBufferInputStream;
import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.io.file.FileHandler;
import info.ata4.log.LogUtils;
import info.ata4.unity.asset.AssetException;
import info.ata4.unity.assetbundle.struct.AssetBundleHeader;
import info.ata4.util.io.lzma.LzmaBufferUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import net.contrapunctus.lzma.LzmaInputStream;

/**
 * Reader for Unity asset bundles.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetBundle extends FileHandler implements Iterable<AssetBundleEntry> {
    
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
    
    private ByteBuffer bbFile;
    private ByteBuffer bbData;
    private AssetBundleHeader header;
    private List<AssetBundleEntry> entries;
    
    @Override
    public void load(ByteBuffer bb) throws IOException {
        this.bbFile = bb;
        
        DataInputReader in = DataInputReader.newReader(bb);

        header = new AssetBundleHeader();
        header.read(in);

        if (!header.hasValidSignature()) {
            throw new AssetException("Invalid signature");
        }

        bb.position(header.getDataOffset());

        try (InputStream is = getDataInputStream()) {
            in = DataInputReader.newReader(is);

            int files = in.readInt();
            entries = new ArrayList<>(files);

            for (int i = 0; i < files; i++) {
                AssetBundleEntry entry = new AssetBundleEntry(this);
                entry.setName(in.readStringNull(255));
                entry.setOffset(in.readInt());
                entry.setLength(in.readInt());
                entries.add(entry);
            }
        }
    }

    @Override
    public void save(Path file) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public InputStream getDataInputStream() throws IOException {
        ByteBuffer bb = bbFile.duplicate();
        bb.position(header.getDataOffset());
        
        InputStream is = new ByteBufferInputStream(bb);
        
        if (header.isCompressed()) {
            return new LzmaInputStream(is);
        } else {
            return is;
        }
    }
    
    public ByteBuffer getDataByteBuffer() throws IOException {
        if (bbData == null) {
            if (header.isCompressed()) {
                bbFile.order(ByteOrder.LITTLE_ENDIAN);
                bbData = LzmaBufferUtils.decode(bbFile);
            } else {
                bbData = ByteBufferUtils.getSlice(bbFile, header.getDataOffset());
            }
        }

        return bbData.duplicate();
    }
    
    public AssetBundleHeader getHeader() {
        return header;
    }

    public List<AssetBundleEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    @Override
    public Iterator<AssetBundleEntry> iterator() {
        return getEntries().iterator();
    }
}
