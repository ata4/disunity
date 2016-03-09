/*
 ** 2014 September 25
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.bundle;

import info.ata4.io.DataReader;
import info.ata4.io.DataReaders;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.READ;
import java.util.List;
import net.contrapunctus.lzma.LzmaInputStream;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.io.input.CountingInputStream;

/**
 * Streaming reader for Unity asset bundles.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BundleReader implements Closeable {

    private final DataReader in;
    private Bundle bundle;
    private CountingInputStream lzma;
    private boolean closed;

    public BundleReader(Path file) throws IOException {
        in = DataReaders.forFile(file, READ);
    }

    public Bundle read() throws BundleException, IOException {
        bundle = new Bundle();

        in.position(0);

        BundleHeader header = bundle.header();
        in.readStruct(header);

        // check signature
        if (!header.hasValidSignature()) {
            throw new BundleException("Invalid signature");
        }

        long dataHeaderSize = header.dataHeaderSize();
        if (dataHeaderSize == 0) {
            // old stream versions don't store the data header size, so use a large
            // fixed number instead
            dataHeaderSize = 4096;
        }

        List<BundleEntryInfo> entryInfos = bundle.entryInfos();
        InputStream is = dataInputStream(0, dataHeaderSize);
        DataReader inData = DataReaders.forInputStream(is);
        int files = inData.readInt();

        for (int i = 0; i < files; i++) {
            BundleEntryInfo entryInfo = new BundleEntryInfo();
            inData.readStruct(entryInfo);
            entryInfos.add(entryInfo);
        }

        // sort entries by offset so that they're in the order in which they
        // appear in the file, which is convenient for compressed bundles
        entryInfos.sort((a, b) -> Long.compare(a.offset(), b.offset()));

        List<BundleEntry> entries = bundle.entries();
        entryInfos.forEach(entryInfo -> {
            entries.add(new BundleInternalEntry(entryInfo, this::inputStreamForEntry));
        });

        return bundle;
    }

    private InputStream dataInputStream(long offset, long size) throws IOException {
        InputStream is;

        // use LZMA stream if the bundle is compressed
        if (bundle.header().compressed()) {
            // create initial input stream if required
            if (lzma == null) {
                lzma = lzmaInputStream();
            }

            // recreate stream if the offset is behind
            long lzmaOffset = lzma.getByteCount();
            if (lzmaOffset > offset) {
                lzma.close();
                lzma = lzmaInputStream();
            }

            // skip forward if required
            if (lzmaOffset < offset) {
                lzma.skip(offset - lzmaOffset);
            }

            is = lzma;
        } else {
            in.position(bundle.header().headerSize() + offset);
            is = in.stream();
        }

        return new BoundedInputStream(is, size);
    }

    private CountingInputStream lzmaInputStream() throws IOException {
        in.position(bundle.header().headerSize());
        return new CountingInputStream(new LzmaInputStream(in.stream()));
    }

    private InputStream inputStreamForEntry(BundleEntryInfo info) throws IOException {
        if (closed) {
            throw new BundleException("Bundle reader is closed");
        }
        return dataInputStream(info.offset(), info.size());
    }

    @Override
    public void close() throws IOException {
        closed = true;
        if (lzma != null) {
            lzma.close();
        }
        in.close();
    }
}
