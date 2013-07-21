// DecoderThread.java -- run LZMA decoder in a separate thread
// Copyright (c)2007 Christopher League <league@contrapunctus.net>
// This is free software, but it comes with ABSOLUTELY NO WARRANTY.
// GNU Lesser General Public License 2.1 or Common Public License 1.0
package net.contrapunctus.lzma;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.ArrayBlockingQueue;
import lzma.LzmaDecoder;

class DecoderThread extends Thread {

    protected ArrayBlockingQueue<byte[]> q;
    protected InputStream in;
    protected OutputStream out;
    protected LzmaDecoder dec;
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

    DecoderThread(InputStream _in) {
        q = ConcurrentBufferOutputStream.newQueue();
        in = _in;
        out = ConcurrentBufferOutputStream.create(q);
        dec = new LzmaDecoder();
        exn = null;
        if (DEBUG) {
            dbg.printf("%s >> %s (%s)%n", this, out, q);
        }
    }
    static final int propSize = 5;
    static final byte[] props = new byte[propSize];

    static {
        // enc.SetEndMarkerMode( true );
        // enc.SetDictionarySize( 1 << 20 );
        props[0] = 0x5d;
        props[1] = 0x00;
        props[2] = 0x00;
        props[3] = 0x10;
        props[4] = 0x00;
    }

    @Override
    public void run() {
        try {
            long outSize = 0;
            if (LzmaOutputStream.LZMA_HEADER) {
                int n = in.read(props, 0, propSize);
                if (n != propSize) {
                    throw new IOException("input .lzma file is too short");
                }
                dec.setDecoderProperties(props);
                for (int i = 0; i < 8; i++) {
                    int v = in.read();
                    if (v < 0) {
                        throw new IOException("Can't read stream size");
                    }
                    outSize |= ((long) v) << (8 * i);
                }
            } else {
                outSize = -1;
                dec.setDecoderProperties(props);
            }
            if (DEBUG) {
                dbg.printf("%s begins%n", this);
            }
            dec.code(in, out, outSize);
            if (DEBUG) {
                dbg.printf("%s ends%n", this);
            }
            in.close(); //?
        } catch (IOException _exn) {
            exn = _exn;
            if (DEBUG) {
                dbg.printf("%s exception: %s%n", this, exn.getMessage());
            }
        }
        // close either way, so listener can unblock
        try {
            out.close();
        } catch (IOException _exn) {
        }
    }

    public void maybeThrow() throws IOException {
        if (exn != null) {
            throw exn;
        }
    }

    @Override
    public String toString() {
        return String.format("Dec@%x", hashCode());
    }
}
