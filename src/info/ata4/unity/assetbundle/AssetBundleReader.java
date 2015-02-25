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
import info.ata4.io.Positionable;
import java.io.BufferedInputStream;
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
    private DataReader inLZMA;

    public AssetBundleReader(Path file) throws AssetBundleException, IOException {
        in = DataReaders.forFile(file, READ);
        header.read(in);

        // check signature
        if (!header.hasValidSignature()) {
            throw new AssetBundleException("Invalid signature");
        }
        
        DataReader inData = getDataReader(0);
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
    
    private DataReader getDataReader(long offset) throws IOException {
        // use LZMA stream if the bundle is compressed
        if (header.isCompressed()) {
            // create initial reader if required
            if (inLZMA == null) {
                inLZMA = getLZMADataReader();
            }
            
            // recreate socket if the offset is behind
            if (inLZMA.position() > offset) {
                inLZMA.close();
                inLZMA = getLZMADataReader();
            }
            
            inLZMA.position(offset);
            return inLZMA;
        } else {
            in.position(header.getHeaderSize() + offset);
            return in;
        }
    }
    
    private DataReader getLZMADataReader() throws IOException {
        in.position(header.getHeaderSize());
        return DataReaders.forInputStream(new LzmaInputStream(in.stream()));
    }
    
    InputStream getInputStreamForEntry(AssetBundleEntryInfo info) throws IOException {
        DataReader inEntry = getDataReader(info.getOffset());
        return new BoundedInputStream(inEntry.stream(), info.getSize());
    }

    public AssetBundleHeader getHeader() {
        return header;
    }
    
    public List<AssetBundleEntryInfo> getEntryInfos() {
        return Collections.unmodifiableList(entryInfos);
    }
    
    public List<AssetBundleEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    @Override
    public Iterator<AssetBundleEntry> iterator() {
        return entries.iterator();
    }
    
    @Override
    public void close() throws IOException {
        if (inLZMA != null) {
            inLZMA.close();
        }
        in.close();
    }
    
    private class EntryComparator implements Comparator<AssetBundleEntryInfo> {

        @Override
        public int compare(AssetBundleEntryInfo o1, AssetBundleEntryInfo o2) {
            long ofs1 = o1.getOffset();
            long ofs2 = o2.getOffset();

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
