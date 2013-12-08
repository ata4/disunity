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

import info.ata4.util.io.DataInputReader;
import info.ata4.util.io.DataOutputWriter;
import info.ata4.util.io.Struct;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class Vector2f implements Struct {
    
    public float x;
    public float y;

    @Override
    public void read(DataInputReader in) throws IOException {
        x = in.readFloat();
        y = in.readFloat();
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        out.writeFloat(x);
        out.writeFloat(y);
    }
}
