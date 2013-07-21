// LzmaOutputStream.java -- transparently compress LZMA while writing
// Copyright (c)2007 Christopher League <league@contrapunctus.net>
// This is free software, but it comes with ABSOLUTELY NO WARRANTY.
// GNU Lesser General Public License 2.1 or Common Public License 1.0
package net.contrapunctus.lzma;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class LzmaOutputStream extends FilterOutputStream {

    /* true for compatibility with lzma(1) command-line tool, false
     * for compatibility with previous versions of LZMA streams.
     */
    public static boolean LZMA_HEADER = true;
    
    private EncoderThread eth;

    private static final PrintStream dbg = System.err;
    private static final boolean DEBUG;

    static {
        String ds = null;
        try {
            ds = System.getProperty("DEBUG_LzmaStreams");
        } catch (SecurityException e) {
        }
        DEBUG = ds != null;
    }

    public LzmaOutputStream(OutputStream _out) {
        this(_out, EncoderThread.DEFAULT_DICT_SZ_POW2, null);
    }

    public LzmaOutputStream(OutputStream _out, Integer dictSzPow2, Integer fastBytes) {
        super(null);
        eth = new EncoderThread(_out, dictSzPow2, fastBytes);
        out = ConcurrentBufferOutputStream.create(eth.q);
        if (DEBUG) {
            dbg.printf("%s >> %s (%s)%n", this, out, eth.q);
        }
        eth.start();
    }

    @Override
    public void write(int i) throws IOException {
        if (eth.exn != null) {
            throw eth.exn;
        }
        out.write(i);
    }

    @Override
    public void close() throws IOException {
        if (DEBUG) {
            dbg.printf("%s closed%n", this);
        }
        out.close();
        try {
            eth.join();
            if (DEBUG) {
                dbg.printf("%s joined %s%n", this, eth);
            }
        } catch (InterruptedException exn) {
            throw new InterruptedIOException(exn.getMessage());
        }
        if (eth.exn != null) {
            throw eth.exn;
        }
    }

    @Override
    public String toString() {
        return String.format("lzmaOut@%x", hashCode());
    }
}
