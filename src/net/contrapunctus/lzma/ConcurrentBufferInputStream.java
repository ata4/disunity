// ConcurrentBufferInputStream.java -- read bytes from blocking queue
// Copyright (c)2007 Christopher League <league@contrapunctus.net>
// This is free software, but it comes with ABSOLUTELY NO WARRANTY.
// GNU Lesser General Public License 2.1 or Common Public License 1.0
package net.contrapunctus.lzma;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.PrintStream;
import java.util.concurrent.ArrayBlockingQueue;

class ConcurrentBufferInputStream extends InputStream {

    protected ArrayBlockingQueue<byte[]> q;
    protected byte[] buf = null;
    protected int next = 0;
    protected boolean eof = false;
    private static final PrintStream dbg = System.err;
    private static final boolean DEBUG;

    static {
        String ds = null;
        try {
            ds = System.getProperty("DEBUG_ConcurrentBuffer");
        } catch (SecurityException e) {
        }
        DEBUG = ds != null;
    }

    private ConcurrentBufferInputStream(ArrayBlockingQueue<byte[]> q) {
        if (DEBUG) {
            dbg.printf("%s << %s%n", this, q);
        }
        this.q = q;
        this.eof = false;
    }

    static InputStream create(ArrayBlockingQueue<byte[]> q) {
        InputStream in = new ConcurrentBufferInputStream(q);
        return in;
    }

    protected byte[] guarded_take() throws IOException {
        try {
            return q.take();
        } catch (InterruptedException exn) {
            throw new InterruptedIOException(exn.getMessage());
        }
    }

    protected boolean prepareAndCheckEOF() throws IOException {
        if (eof) {
            return true;
        }
        if (buf == null || next >= buf.length) {
            buf = guarded_take();
            next = 0;
            if (buf.length == 0) {
                eof = true;
                return true;
            }
        }
        return false;
    }

    @Override
    public int read() throws IOException {
        if (prepareAndCheckEOF()) {
            return -1;
        }
        int x = buf[next];
        next++;
        return x & 0xff;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (prepareAndCheckEOF()) {
            return -1;
        }
        int k = buf.length - next;
        if (len < k) {
            k = len;
        }
        System.arraycopy(buf, next, b, off, k);
        next += k;
        return k;
    }

    @Override
    public String toString() {
        return String.format("cbIn@%x", hashCode());
    }
}
