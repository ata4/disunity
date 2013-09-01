/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.asset;

import info.ata4.unity.struct.AssetBundleHeader;
import info.ata4.util.io.ByteBufferInput;
import info.ata4.util.io.ByteBufferInputStream;
import info.ata4.util.io.ByteBufferOutputStream;
import info.ata4.util.io.DataInputReader;
import info.ata4.util.io.MappedFileHandler;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.contrapunctus.lzma.LzmaInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;

/**
 * Reader for Unity asset bundles.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetBundle extends MappedFileHandler {
    
    private static final Logger L = Logger.getLogger(AssetBundle.class.getName());
    
    private static final String SIGNATURE_WEB = "UnityWeb";
    private static final String SIGNATURE_RAW = "UnityRaw";
    
    public static boolean isAssetBundle(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            DataInputReader in = new DataInputReader(new DataInputStream(fis));
            String signature = in.readStringFixed(8);
            return signature.equals(SIGNATURE_WEB) || signature.equals(SIGNATURE_RAW);
        } catch (IOException ex) {
            return false;
        }
    }
    
    private ByteBuffer bb;
    private ByteBuffer bbData;
    private AssetBundleHeader info;
    private List<Entry> entries;
    private boolean compressed;
    
    @Override
    public void load(ByteBuffer bb) throws IOException {
        this.bb = bb;
        DataInputReader in = new DataInputReader(new ByteBufferInput(bb));

        String header = in.readStringNull(8);
        switch (header) {
            case SIGNATURE_RAW:
                compressed = false;
                break;

            case SIGNATURE_WEB:
                compressed = true;
                break;

            default:
                throw new AssetException("Invalid signature");
        }

        info = new AssetBundleHeader();
        info.read(in);

        bb.position(info.dataOffset);

        try (InputStream is = getDataInputStream()) {
            in = new DataInputReader(new DataInputStream(is));

            int files = in.readInt();
            entries = new ArrayList<>(files);

            for (int i = 0; i < files; i++) {
                Entry entry = new Entry();
                entry.name = in.readStringNull(255);
                entry.offset = in.readInt();
                entry.length = in.readInt();
                entries.add(entry);
            }
        }
    }

    @Override
    public void save(File file) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public CountingInputStream getDataInputStream() throws IOException {
        ByteBuffer bbd = bb.duplicate();
        bbd.position(info.dataOffset);
        
        InputStream is = new ByteBufferInputStream(bbd);
        
        if (isCompressed()) {
            is = new LzmaInputStream(is);
        }
        
        return new CountingInputStream(is);
    }
    
    public ByteBuffer getDataByteBuffer() throws IOException {
        if (bbData == null) {
            if (isCompressed()) {
                bb.position(info.dataOffset + 5);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                
                long lzmaSize = bb.getLong();
                
                if (lzmaSize < 0) {
                    throw new IOException("Invalid LZMA size");
                }
                
                if (lzmaSize > Integer.MAX_VALUE) {
                    throw new IOException("Entry is too large for direct decompression");
                }
                
                bbData = ByteBuffer.allocateDirect((int) lzmaSize);
                
                bb.position(info.dataOffset);
                
                InputStream is = new LzmaInputStream(new ByteBufferInputStream(bb));
                OutputStream os = new ByteBufferOutputStream(bbData);
                
                IOUtils.copy(is, os);
                
                bbData.rewind();
            } else {
                bb.position(info.dataOffset);
                bbData = bb.slice();
            }
        }

        return bbData.duplicate();
    }
    
    public boolean isCompressed() {
        return compressed;
    }
    
    public byte getFileVersion() {
        return info.fileVersion;
    }

    public String getVersion() {
        return info.version;
    }

    public String getRevision() {
        return info.revision;
    }
    
    public void extractEntries(File dir) throws IOException {
        L.log(Level.INFO, "Extracting entries to {0}", dir);
        
        // for compressed files, use sorted entries and streaming
        // for uncompressed files, use direct buffers
        if (isCompressed()) {
            // sort entries by offset
            List<Entry> entriesSorted = new ArrayList<>(getEntries());
            Collections.sort(entriesSorted, new EntryOffsetComparator());

            try (
                CountingInputStream is = getDataInputStream()
            ) {
                for (Entry entry : entriesSorted) {
                    String entryName = entry.getName();
                    int entryOffset = entry.getOffset();
                    int entrySize = entry.getSize();

                    L.log(Level.INFO, "Extracting {0}", entryName);

                    // skip gaps between entries
                    if (is.getByteCount() < entry.getOffset()) {
                        long skipBytes = entryOffset - is.getByteCount();
                        L.log(Level.FINER, "Entry offset after current offset, skipped {0} bytes", skipBytes);
                        is.skip(skipBytes);
                    }
                    
                    File entryFile = new File(dir, entryName);

                    try (
                        OutputStream os = FileUtils.openOutputStream(entryFile);
                    ) {
                        IOUtils.copyLarge(is, os, 0, entrySize);
                    }
                }
            }
        } else {
            for (Entry entry : entries) {
                String entryName = entry.getName();
                int entryOffset = entry.getOffset();
                int entrySize = entry.getSize();
                
                L.log(Level.INFO, "Extracting {0}", entryName);
                
                bb.position(info.dataOffset + entryOffset);
                ByteBuffer entryBuffer = bb.slice();
                entryBuffer.limit(entrySize);
                
                File entryFile = new File(dir, entryName);
                
                try (
                    FileOutputStream os = FileUtils.openOutputStream(entryFile);
                ) {
                    os.getChannel().write(entryBuffer);
                }
            }
        }
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public class Entry {

        private String name;
        private int offset;
        private int length;
        
        public String getName() {
            return name;
        }
        
        public int getSize() {
            return length;
        }

        public int getOffset() {
            return offset;
        }
        
        public ByteBuffer getByteBuffer() throws IOException {
            ByteBuffer bbd = getDataByteBuffer();
            bbd.position(getOffset());
            ByteBuffer bb = bbd.slice();
            bb.limit(getSize());
            return bb;
        }
    }
    
    private class EntryOffsetComparator implements Comparator<Entry> {
        @Override
        public int compare(Entry o1, Entry o2) {
            if (o1.offset == o2.offset) {
                return 0;
            }
            return o1.offset > o2.offset ? 1 : -1;
        }
    }
}
