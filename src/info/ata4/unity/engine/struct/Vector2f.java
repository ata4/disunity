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
public class Vector2f implements Struct {
    
    private boolean half;
    
    public float x;
    public float y;
    
    public boolean isHalf() {
        return half;
    }

    public void setHalf(boolean half) {
        this.half = half;
    }

    @Override
    public void read(DataInputReader in) throws IOException {
        if (half) {
            x = in.readHalf();
            y = in.readHalf();
        } else {
            x = in.readFloat();
            y = in.readFloat();
        }
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        if (half) {
            out.writeHalf(x);
            out.writeHalf(y);
        } else {
            out.writeFloat(x);
            out.writeFloat(y);
        }
    }
}
