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

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import info.ata4.io.socket.IOSocket;
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
    public boolean isEncoded(IOSocket socket) throws IOException {
        DataReader in = new DataReader(socket);
        
        in.position(1 << 5);
        int b1 = in.readByte();
        
        in.position(1 << 6);
        int b2 = in.readByte();
        
        return b1 == KEY[5] && b2 == KEY[6];
    }
    
    private void code(IOSocket socket) throws IOException {
        DataReader in = new DataReader(socket);
        DataWriter out = new DataWriter(socket);
        
        for (int exp = 5; 1 << exp < in.size(); exp++) {
            int offset = 1 << exp;
            in.position(offset);
            int b = in.readByte();
            b ^= KEY[exp % KEY.length];
            out.position(offset);
            out.writeByte(b);
        }
    }

    @Override
    public void encode(IOSocket socket) throws IOException {
        code(socket);
    }

    @Override
    public void decode(IOSocket socket) throws IOException {
        code(socket);
    }
    
}
