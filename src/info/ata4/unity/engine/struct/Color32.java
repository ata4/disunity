/*
 ** 2013 December 08
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.engine.struct;

import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
import info.ata4.io.Struct;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class Color32 implements Struct {
    
    public int r;
    public int g;
    public int b;
    public int a;
    
    public void fromInt(int value) {
        r =  value & 0xff;
        g = (value >>> 8) & 0xff;
        b = (value >>> 16) & 0xff;
        a = (value >>> 24) & 0xff;
    }
    
    public int toInt() {
        int value = 0;
        value +=  r & 0xff;
        value += (g & 0xff) << 8;
        value += (b & 0xff) << 16;
        value += (a & 0xff) << 24;
        return value;
    }

    @Override
    public void read(DataInputReader in) throws IOException {
        r = in.readUnsignedByte();
        g = in.readUnsignedByte();
        b = in.readUnsignedByte();
        a = in.readUnsignedByte();
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        out.writeByte(r);
        out.writeByte(g);
        out.writeByte(b);
        out.writeByte(a);
    }
}
