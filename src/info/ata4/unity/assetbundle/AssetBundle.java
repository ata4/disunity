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

import info.ata4.io.ByteBufferInputStream;
import info.ata4.io.ByteBufferOutputStream;
import info.ata4.io.DataInputReader;
import info.ata4.io.file.FileHandler;
import info.ata4.io.util.ByteBufferUtils;
import info.ata4.unity.asset.AssetException;
import info.ata4.unity.assetbundle.struct.AssetBundleHeader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.contrapunctus.lzma.LzmaInputStream;
import org.apache.commons.io.IOUtils;

/**
 * Reader for Unity asset bundles.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetBundle extends FileHandler implements Iterable<AssetBundleEntry> {
    
    private static final Logger L = Logger.getLogger(AssetBundle.class.getName());
    
    public static boolean isAssetBundle(Path file) {
        // check signature of the file
        try (DataInputReader in = DataInputReader.newReader(file)) {
            AssetBundleHeader info = new AssetBundleHeader();
            info.setSignature(in.readStringFixed(8));
            return info.hasValidSignature();
        } catch (IOException ex) {
            return false;
        }
    }
    
    private ByteBuffer bb;
    private ByteBuffer bbData;
    private AssetBundleHeader header;
    private List<AssetBundleEntry> entries;
    
    @Override
    public void load(ByteBuffer bb) throws IOException {
        this.bb = bb;
        
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
        ByteBuffer bbd = bb.duplicate();
        bbd.position(header.getDataOffset());
        
        InputStream is = new ByteBufferInputStream(bbd);
        
        if (header.isCompressed()) {
            is = new LzmaInputStream(is);
        }
        
        return is;
    }
    
    public ByteBuffer getDataByteBuffer() throws IOException {
        if (bbData == null) {
            if (header.isCompressed()) {
                // may take a while to decompress it in-memory
                L.log(Level.INFO, "Uncompressing {0}", getSourceFile().getFileName());
                
                // get uncompressed data size from LZMA headers
                bb.position(header.getDataOffset() + 5);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                
                long lzmaSize = bb.getLong();
                if (lzmaSize < 0) {
                    throw new IOException("Invalid LZMA size");
                }
                
                // in theory, entries can be larger than 2GB and break memory
                // mapping using one single buffer, although that would be a bit
                // ridiculous for typical Unity web games
                if (lzmaSize > Integer.MAX_VALUE) {
                    throw new IOException("Entry is too large for direct decompression");
                }
                
                bbData = ByteBuffer.allocateDirect((int) lzmaSize);
                
                bb.position(header.getDataOffset());
                
                // decompress data
                InputStream is = new LzmaInputStream(new ByteBufferInputStream(bb));
                OutputStream os = new ByteBufferOutputStream(bbData);
                
                IOUtils.copy(is, os);
                
                bbData.rewind();
            } else {
                bbData = ByteBufferUtils.getSlice(bb, header.getDataOffset());
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
