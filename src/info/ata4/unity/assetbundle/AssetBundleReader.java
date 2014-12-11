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
import info.ata4.io.Positionable;
import info.ata4.io.socket.IOSocket;
import info.ata4.io.socket.Sockets;
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
    
    private final DataReader in;
    private IOSocket lzmaSocket;

    public AssetBundleReader(Path file) throws AssetBundleException, IOException {
        in = new DataReader(Sockets.forFile(file, READ));
        header.read(in);

        // check signature
        if (!header.hasValidSignature()) {
            throw new AssetBundleException("Invalid signature");
        }
        
        DataReader inData = new DataReader(getDataSocket(0));
        int files = inData.readInt();
        List<AssetBundleEntryInfo> entryInfos = new ArrayList<>(files);
        
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
    
    private IOSocket getDataSocket(long offset) throws IOException {
        // use LZMA stream if the bundle is compressed
        if (header.isCompressed()) {
            // create initial socket if required
            if (lzmaSocket == null) {
                lzmaSocket = getLZMADataSocket();
            }
            
            Positionable pos = lzmaSocket.getPositionable();
            
            // recreate socket if the offset is behind
            if (pos.position() > offset) {
                lzmaSocket.close();
                lzmaSocket = getLZMADataSocket();
            }
            
            pos.position(offset);
            return lzmaSocket;
        } else {
            in.position(header.getHeaderSize() + offset);
            return in.getSocket();
        }
    }
    
    private IOSocket getLZMADataSocket() throws IOException {
        in.position(header.getHeaderSize());
        InputStream is = new LzmaInputStream(
                new BufferedInputStream(in.getSocket().getInputStream()));
        return Sockets.forInputStream(is);
    }
    
    InputStream getInputStreamForEntry(AssetBundleEntryInfo info) throws IOException {
        IOSocket socket = getDataSocket(info.getOffset());
        return new BoundedInputStream(socket.getInputStream(), info.getSize());
    }

    public AssetBundleHeader getHeader() {
        return header;
    }
    
    @Override
    public void close() throws IOException {
        if (lzmaSocket != null) {
            lzmaSocket.close();
        }
        in.close();
    }

    @Override
    public Iterator<AssetBundleEntry> iterator() {
        return entries.iterator();
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
