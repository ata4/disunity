// ConcurrentBufferOutputStream.java -- write bytes to blocking queue
// Copyright (c)2007 Christopher League <league@contrapunctus.net>

// This is free software, but it comes with ABSOLUTELY NO WARRANTY.
// GNU Lesser General Public License 2.1 or Common Public License 1.0

package net.contrapunctus.lzma;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.ArrayBlockingQueue;

class ConcurrentBufferOutputStream extends OutputStream
{
    protected ArrayBlockingQueue<byte[]> q;
    static final int BUFSIZE = 16384;
    static final int QUEUESIZE = 4096;
    private static final PrintStream dbg = System.err;
    private static final boolean DEBUG;

    static {
        String ds = null;
        try { ds = System.getProperty("DEBUG_ConcurrentBuffer"); }
        catch(SecurityException e) { }
        DEBUG = ds != null;
    }

    private ConcurrentBufferOutputStream( ArrayBlockingQueue<byte[]> q )
    {
        if(DEBUG) dbg.printf("%s >> %s%n", this, q);
        this.q = q;
    }

    static OutputStream create( ArrayBlockingQueue<byte[]> q )
    {
        OutputStream out = new ConcurrentBufferOutputStream( q );
        out = new BufferedOutputStream( out, BUFSIZE );
        return out;
    }

    static ArrayBlockingQueue<byte[]> newQueue( )
    {
        return new ArrayBlockingQueue<byte[]>( QUEUESIZE );
    }

    protected void guarded_put( byte[] a ) throws IOException
    {
        try {
            q.put( a );
        }
        catch( InterruptedException exn ) {
            throw new InterruptedIOException( exn.getMessage() );
        }
    }

    public void write( int i ) throws IOException
    {
        byte b[] = new byte[1];
        b[0] = (byte) (i & 0xff);
        guarded_put( b );
    }

    public void write(byte[] b, int off, int len) throws IOException
    {
        byte[] a = new byte [len];
        System.arraycopy(b, off, a, 0, len);
        guarded_put( a );
    }

    public void close( ) throws IOException
    {
        if(DEBUG) dbg.printf("%s closed%n", this);
        byte b[] = new byte[0]; // sentinel
        guarded_put( b );
    }
}
