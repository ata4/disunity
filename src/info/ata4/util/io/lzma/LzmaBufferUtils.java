/*
** 2014 April 8
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/
package info.ata4.util.io.lzma;

import info.ata4.io.buffer.ByteBufferInputStream;
import info.ata4.io.buffer.ByteBufferOutputStream;
import info.ata4.log.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import lzma.LzmaDecoder;
import lzma.LzmaEncoder;

/**
 * LZMA byte buffer utility class.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class LzmaBufferUtils {
    
    private static final Logger L = LogUtils.getLogger();

    private LzmaBufferUtils() {
    }
    
    public static ByteBuffer decode(ByteBuffer bb) throws IOException {
        ByteBuffer bbc = bb.duplicate();
        bbc.order(ByteOrder.LITTLE_ENDIAN);
        
        byte[] lzmaProps = new byte[5];
        bbc.get(lzmaProps);
        
        long lzmaSize = bbc.getLong();

        if (lzmaSize < 0) {
            throw new IOException("Invalid LZMA size");
        } else if (lzmaSize > Integer.MAX_VALUE) {
            throw new IOException("Uncompressed LZMA buffer is too large for byte buffers");
        }
        
        ByteBuffer bbu = ByteBuffer.allocateDirect((int) lzmaSize);
        
        LzmaDecoder dec = new LzmaDecoder();
        if (!dec.setDecoderProperties(lzmaProps)) {
            throw new IOException("Invalid LZMA props");
        }
        
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(new ByteBufferProgress(bbc), 2, 1, TimeUnit.SECONDS);
        
        try {
            InputStream is = new ByteBufferInputStream(bbc);
            OutputStream os = new ByteBufferOutputStream(bbu);
            if (!dec.code(is, os, lzmaSize)) {
                throw new IOException("LZMA decoding error");
            }
        } finally {    
            executor.shutdown();
        }
        
        bbu.flip();
        bbu.order(bb.order());
        
        return bbu;
    }
    
    public static ByteBuffer encode(ByteBuffer bb, int lc, int lp, int pb, int dictSize) throws IOException {
        ByteBuffer bbu = bb.duplicate();
 
        ByteBuffer bbc = ByteBuffer.allocateDirect(bbu.limit() + 13);
        bbc.order(ByteOrder.LITTLE_ENDIAN);
        
        LzmaEncoder enc = new LzmaEncoder();
        if (!enc.setLcLpPb(lc, lp, pb)) {
            throw new IOException("Invalid LZMA props");
        }
        if (!enc.setDictionarySize(dictSize)) {
            throw new IOException("Invalid dictionary size");
        }
        enc.setEndMarkerMode(true);
        
        bbc.put(enc.getCoderProperties());
        bbc.putLong(bbu.limit());
        
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(new ByteBufferProgress(bbu), 2, 2, TimeUnit.SECONDS);
        
        try {
            InputStream is = new ByteBufferInputStream(bbu);
            OutputStream os = new ByteBufferOutputStream(bbc);
            enc.code(is, os);
        } finally {
            executor.shutdown();
        }
        
        bbc.flip();
        bbc.order(bb.order());
        
        return bbc;
    }
    
    public static ByteBuffer encode(ByteBuffer bb) throws IOException {
        return encode(bb, 3, 0, 2, 1 << 19);
    }
    
    private static class ByteBufferProgress implements Runnable {
        
        private final ByteBuffer bb;
        
        ByteBufferProgress(ByteBuffer bb) {
            this.bb = bb;
        }
        
        @Override
        public void run() {
            double progress = Math.round(bb.position() / (double) bb.limit() * 100);
            L.log(Level.INFO, "{0}%", progress);
        }
        
    }
}
