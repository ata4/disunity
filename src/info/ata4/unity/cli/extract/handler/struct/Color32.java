/*
 ** 2013 Dezember 08
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.extract.handler.struct;

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
