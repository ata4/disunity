// EncoderThread.java -- run LZMA encoder in a separate thread
// Copyright (c)2007 Christopher League <league@contrapunctus.net>
// This is free software, but it comes with ABSOLUTELY NO WARRANTY.
// GNU Lesser General Public License 2.1 or Common Public License 1.0
package net.contrapunctus.lzma;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.ArrayBlockingQueue;
import lzma.LzmaEncoder;

class EncoderThread extends Thread {

    public static final Integer DEFAULT_DICT_SZ_POW2 = new Integer(20);
    
    protected ArrayBlockingQueue<byte[]> q;
    protected InputStream in;
    protected OutputStream out;
    protected LzmaEncoder enc;
    protected IOException exn;
    private static final PrintStream dbg = System.err;
    private static final boolean DEBUG;

    static {
        String ds = null;
        try {
            ds = System.getProperty("DEBUG_LzmaCoders");
        } catch (SecurityException e) {
        }
        DEBUG = ds != null;
    }

    /**
     * @param dictSzPow2 If non-null, equivalent to the N in the -dN arg to
     * LzmaAlone
     * @param fastBytes If non-null, equivalent to the N in the -fbN arg to
     * LzmaAlone
     */
    EncoderThread(OutputStream _out, Integer dictSzPow2, Integer fastBytes) {
        q = ConcurrentBufferOutputStream.newQueue();
        in = ConcurrentBufferInputStream.create(q);
        out = _out;
        enc = new LzmaEncoder();
        exn = null;
        enc.setDictionarySize(1 << (dictSzPow2 == null ? DEFAULT_DICT_SZ_POW2 : dictSzPow2).intValue());
        if (fastBytes != null) {
            enc.setNumFastBytes(fastBytes.intValue());
        }
        if (DEBUG) {
            dbg.printf("%s << %s (%s)%n", this, in, q);
        }
    }

    @Override
    public void run() {
        try {
            enc.setEndMarkerMode(true);
            if (LzmaOutputStream.LZMA_HEADER) {
                enc.writeCoderProperties(out);
                // 5d 00 00 10 00
                long fileSize = -1;
                for (int i = 0; i < 8; i++) {
                    out.write((int) (fileSize >>> (8 * i)) & 0xFF);
                }
            }
            if (DEBUG) {
                dbg.printf("%s begins%n", this);
            }
            enc.code(in, out);
            if (DEBUG) {
                dbg.printf("%s ends%n", this);
            }
            out.close();
        } catch (IOException _exn) {
            exn = _exn;
            if (DEBUG) {
                dbg.printf("%s exception: %s%n", exn.getMessage());
            }
        }
    }

    @Override
    public String toString() {
        return String.format("Enc@%x", hashCode());
    }
}
