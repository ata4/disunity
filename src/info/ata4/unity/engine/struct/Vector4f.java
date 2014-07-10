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
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class Vector4f extends Vector3f {
    
    public float w;

    @Override
    public void read(DataInputReader in) throws IOException {
        super.read(in);
        if (isHalf()) {
            w = in.readHalf();
        } else {
            w = in.readFloat();
        }
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        super.write(out);
        if (isHalf()) {
            out.writeHalf(w);
        } else {
            out.writeFloat(w);
        }
    }
}
