/*
 ** 2014 September 25
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.bundle;

import info.ata4.io.DataReader;
import info.ata4.io.DataReaders;
import info.ata4.io.buffer.ByteBufferChannel;
import info.ata4.io.buffer.ByteBufferOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.*;
import static java.nio.file.StandardOpenOption.*;
import org.apache.commons.io.IOUtils;

/**
 * Asset bundle file utility class.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BundleUtils {

    private static final Charset PROP_CHARSET = Charset.forName("US-ASCII");

    private BundleUtils() {
    }

    public static boolean isBundle(Path file) {
        if (!Files.isRegularFile(file)) {
            return false;
        }

        try (InputStream is = Files.newInputStream(file)) {
            byte[] header = new byte[8];
            is.read(header);
            String headerString = new String(header, PROP_CHARSET);
            return headerString.equals(BundleHeader.SIGNATURE_WEB)
                    || headerString.equals(BundleHeader.SIGNATURE_RAW);
        } catch (IOException ex) {
            return false;
        }
    }

    public static SeekableByteChannel byteChannelForEntry(BundleEntry entry) throws IOException {
        SeekableByteChannel chan;

        // check if the entry is larger than 128 MiB
        long size = entry.size();
        if (size > 1 << 27) {
            // copy entry to temporary file
            Path tmpFile = Files.createTempFile("disunity", null);
            Files.copy(entry.inputStream(), tmpFile, REPLACE_EXISTING);
            chan = Files.newByteChannel(tmpFile, READ, DELETE_ON_CLOSE);
        } else {
            // copy entry to memory
            ByteBuffer bb = ByteBuffer.allocateDirect((int) size);
            IOUtils.copy(entry.inputStream(), new ByteBufferOutputStream(bb));
            bb.flip();
            chan = new ByteBufferChannel(bb);
        }

        return chan;
    }

    public static DataReader dataReaderForEntry(BundleEntry entry) throws IOException {
        return DataReaders.forSeekableByteChannel(BundleUtils.byteChannelForEntry(entry));
    }
}
