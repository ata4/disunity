/*
 ** 2014 July 01
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.assetbundle.codec;

import info.ata4.io.DataRandomAccess;
import java.io.IOException;
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
    public boolean isEncoded(DataRandomAccess ra) throws IOException {
        ra.position(1 << 5);
        int b1 = ra.readByte();
        
        ra.position(1 << 6);
        int b2 = ra.readByte();
        
        return b1 == KEY[5] && b2 == KEY[6];
    }
    
    private void code(DataRandomAccess ra) throws IOException {
        for (int exp = 5; 1 << exp < ra.size(); exp++) {
            int offset = 1 << exp;
            ra.position(offset);
            int b = ra.readByte();
            b ^= KEY[exp % KEY.length];
            ra.position(offset);
            ra.writeByte(b);
        }
    }

    @Override
    public void encode(DataRandomAccess ra) throws IOException {
        code(ra);
    }

    @Override
    public void decode(DataRandomAccess ra) throws IOException {
        code(ra);
    }
    
}
