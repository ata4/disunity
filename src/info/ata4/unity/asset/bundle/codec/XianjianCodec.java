/*
 ** 2014 July 01
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.asset.bundle.codec;

import info.ata4.io.buffer.ByteBufferUtils;
import java.nio.ByteBuffer;
import javax.xml.bind.DatatypeConverter;

/**
 * Codec for Xianjian asset bundles where some bytes are XOR'd with a fixed key.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class XianjianCodec implements AssetBundleCodec {
    
    private static final byte[] KEY = DatatypeConverter.parseHexBinary("56F45921699978FC92B3");
    
    @Override
    public String getName() {
        return "Xianjian XOR";
    }

    @Override
    public boolean isEncoded(ByteBuffer bb) {
        return bb.get(1 << 5) == KEY[5] && bb.get(1 << 6) == KEY[6];
    }

    @Override
    public ByteBuffer encode(ByteBuffer bb) {
        if (bb.isReadOnly()) {
            bb = ByteBufferUtils.copy(bb);
        }
        
        for (int exp = 5; 1 << exp < bb.limit(); exp++) {
            int offset = 1 << exp;
            int b = bb.get(offset) & 0xff;
            b ^= KEY[exp % KEY.length];
            bb.put(offset, (byte) b);
        }
        
        return bb;
    }

    @Override
    public ByteBuffer decode(ByteBuffer bb) {
        return encode(bb);
    }
    
}
