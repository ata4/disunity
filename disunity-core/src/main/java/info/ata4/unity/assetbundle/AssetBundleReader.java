/*
 ** 2014 September 25
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.assetbundle;

import info.ata4.io.DataReader;
import info.ata4.io.DataReaders;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.READ;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import net.contrapunctus.lzma.LzmaInputStream;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.io.input.CountingInputStream;

/**
 * Streaming reader for Unity asset bundles.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetBundleReader implements Closeable, Iterable<AssetBundleEntry> {
    
    private final AssetBundleHeader header = new AssetBundleHeader();
    private final List<AssetBundleEntry> entries = new ArrayList<>();
    private final List<AssetBundleEntryInfo> entryInfos = new ArrayList<>();
    
    private final DataReader in;
    private CountingInputStream lzma;

    public AssetBundleReader(Path file) throws AssetBundleException, IOException {
        in = DataReaders.forFile(file, READ);
        header.read(in);

        // check signature
        if (!header.hasValidSignature()) {
            throw new AssetBundleException("Invalid signature");
        }
        
        long dataHeaderSize = header.dataHeaderSize();
        if (dataHeaderSize == 0) {
            // old stream versions don't store the data header size, so use a large
            // fixed number instead
            dataHeaderSize = 4096;
        }
        
        InputStream is = getDataInputStream(0, dataHeaderSize);
        DataReader inData = DataReaders.forInputStream(is);
        int files = inData.readInt();

        for (int i = 0; i < files; i++) {
            AssetBundleEntryInfo entryInfo = new AssetBundleEntryInfo();
            entryInfo.read(inData);
            entryInfos.add(entryInfo);
        }
        
        // sort entries by offset so that they're in the order in which they
        // appear in the file, which is convenient for compressed bundles
        Collections.sort(entryInfos, new EntryComparator());
        
        for (AssetBundleEntryInfo entryInfo : entryInfos) {
            entries.add(new AssetBundleInternalEntry(this, entryInfo));
        }
    }
    
    private InputStream getDataInputStream(long offset, long size) throws IOException {
        InputStream is;
        
        // use LZMA stream if the bundle is compressed
        if (header.compressed()) {
            // create initial input stream if required
            if (lzma == null) {
                lzma = getLZMAInputStream();
            }
            
            // recreate stream if the offset is behind
            long lzmaOffset = lzma.getByteCount();
            if (lzmaOffset > offset) {
                lzma.close();
                lzma = getLZMAInputStream();
            }
            
            // skip forward if required
            if (lzmaOffset < offset) {
                lzma.skip(offset - lzmaOffset);
            }
            
            is = lzma;
        } else {
            in.position(header.headerSize() + offset);
            is = in.stream();
        }
        
        return new BoundedInputStream(is, size);
    }
    
    private CountingInputStream getLZMAInputStream() throws IOException {
        in.position(header.headerSize());
        return new CountingInputStream(new LzmaInputStream(in.stream()));
    }
    
    InputStream getInputStreamForEntry(AssetBundleEntryInfo info) throws IOException {
        return getDataInputStream(info.offset(), info.size());
    }

    public AssetBundleHeader header() {
        return header;
    }
    
    public List<AssetBundleEntryInfo> entryInfos() {
        return Collections.unmodifiableList(entryInfos);
    }
    
    public List<AssetBundleEntry> entries() {
        return Collections.unmodifiableList(entries);
    }

    @Override
    public Iterator<AssetBundleEntry> iterator() {
        return entries.iterator();
    }
    
    @Override
    public void close() throws IOException {
        if (lzma != null) {
            lzma.close();
        }
        in.close();
    }
    
    private class EntryComparator implements Comparator<AssetBundleEntryInfo> {

        @Override
        public int compare(AssetBundleEntryInfo o1, AssetBundleEntryInfo o2) {
            long ofs1 = o1.offset();
            long ofs2 = o2.offset();

            if (ofs1 > ofs2) {
                return 1;
            } else if (ofs1 < ofs2) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
