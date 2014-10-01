/*
 ** 2014 September 29
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.assetbundle;

import info.ata4.io.DataRandomAccess;
import info.ata4.io.buffer.ByteBufferOutputStream;
import info.ata4.io.buffer.ByteBufferUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BundleEntryStreamed extends BundleEntry {
    
    private InputStream is;
    private BundleEntryBuffered buf;

    public BundleEntryStreamed(BundleEntryInfo info) {
        super(info);
    }

    public InputStream getInputStream() {
        return is;
    }

    public void setInputStream(InputStream is) {
        this.is = is;
    }
    
    public BundleEntryBuffered buffer() throws IOException {
        if (buf == null) {
            buf = new BundleEntryBuffered(info);
            if (info.getLength() < Integer.MAX_VALUE) {
                ByteBuffer bb = ByteBufferUtils.allocate((int) info.getLength());
                OutputStream os = new ByteBufferOutputStream(bb);
                IOUtils.copyLarge(is, os);
                bb.flip();

                buf.setRandomAccess(DataRandomAccess.newRandomAccess(bb));
            } else {
                // TODO: create temporary file
                throw new IllegalArgumentException("Entry is too large for buffering");
            }
        }
        return buf;
    }
}
