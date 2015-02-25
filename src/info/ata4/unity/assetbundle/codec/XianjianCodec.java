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
import info.ata4.io.DataReaders;
import info.ata4.io.DataWriter;
import info.ata4.io.DataWriters;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
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
    public boolean isEncoded(SeekableByteChannel chan) throws IOException {
        DataReader in = DataReaders.forSeekableByteChannel(chan);
        
        in.position(1 << 5);
        int b1 = in.readByte();
        
        in.position(1 << 6);
        int b2 = in.readByte();
        
        return b1 == KEY[5] && b2 == KEY[6];
    }
    
    private void code(SeekableByteChannel chan) throws IOException {
        DataReader in = DataReaders.forSeekableByteChannel(chan);
        DataWriter out = DataWriters.forSeekableByteChannel(chan);
        
        for (int exp = 5; 1 << exp < in.size(); exp++) {
            int offset = 1 << exp;
            in.position(offset);
            int b = in.readByte();
            b ^= KEY[exp % KEY.length];
            out.position(offset);
            out.writeUnsignedByte(b);
        }
    }

    @Override
    public void encode(SeekableByteChannel chan) throws IOException {
        code(chan);
    }

    @Override
    public void decode(SeekableByteChannel chan) throws IOException {
        code(chan);
    }
    
}
