/*
 ** 2014 May 20
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.util.io;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BitInputStream extends InputStream {
    
    private final InputStream is;
    private int bitBuffer;
    private int bitCount;
    private int bits = 8;

    public BitInputStream(InputStream is) {
        this.is = is;
    }
    
    public int getBitLength() {
        return bits;
    }

    public void setBitLength(int bits) {
        if (bits < 1 || bits > 32) {
            throw new IllegalArgumentException();
        }
        
        this.bits = bits;
    }

    @Override
    public int read() throws IOException {
        while (bitCount < bits) {
            int b = is.read();
            if (b == -1) {
                return b;
            }

            bitBuffer |= b << bitCount;
            bitCount += 8;
        }

        int code = bitBuffer;   
        if (bitCount != 32) {
             code &= (1 << bits) - 1;
        }
        
        bitBuffer >>= bits;
        bitCount -= bits;
        return code;
    }
}
