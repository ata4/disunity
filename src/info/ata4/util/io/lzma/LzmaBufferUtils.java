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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import lzma.LzmaDecoder;
import lzma.LzmaEncoder;

/**
 * LZMA byte buffer utility class.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class LzmaBufferUtils {
    
    private LzmaBufferUtils() {
    }
    
    public static ByteBuffer decode(ByteBuffer bb) throws IOException {
        byte[] lzmaProps = new byte[5];
        bb.get(lzmaProps);
        
        long lzmaSize = bb.getLong();

        if (lzmaSize < 0) {
            throw new IOException("Invalid LZMA size");
        } else if (lzmaSize > Integer.MAX_VALUE) {
            throw new IOException("Uncompressed LZMA buffer is too large for byte buffers");
        }
        
        ByteBuffer bbu = ByteBuffer.allocateDirect((int) lzmaSize);
        bbu.order(bb.order());
        
        LzmaDecoder dec = new LzmaDecoder();
        dec.setDecoderProperties(lzmaProps);
        
        InputStream is = new ByteBufferInputStream(bb);
        OutputStream os = new ByteBufferOutputStream(bbu);
        dec.code(is, os, lzmaSize);
        
        bbu.flip();
        
        return bbu;
    }
    
    public static ByteBuffer encode(ByteBuffer bb, int lc, int lp, int pb, int dictSize) throws IOException {
        ByteBuffer bbc = ByteBuffer.allocateDirect(bb.limit() + 13);
        bbc.order(bb.order());
        
        LzmaEncoder enc = new LzmaEncoder();
        enc.setLcLpPb(lc, lp, pb);
        enc.setDictionarySize(dictSize);
        enc.setEndMarkerMode(true);
        
        bbc.put(enc.getCoderProperties());
        bbc.putLong(bb.limit());
        
        InputStream is = new ByteBufferInputStream(bb);
        OutputStream os = new ByteBufferOutputStream(bbc);
        enc.code(is, os);
        
        bbc.flip();
        
        return bbc;
    }
    
    public static ByteBuffer encode(ByteBuffer bb) throws IOException {
        return encode(bb, 3, 0, 2, 1 << 19);
    }
}
