/*
 ** 2015 November 29
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.serialize.objectinfo;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ObjectInfoV3 extends ObjectInfoV2 {

    private boolean stripped;

    public boolean isStripped() {
        return stripped;
    }

    public void setStripped(boolean stripped) {
        this.stripped = stripped;
    }

    @Override
    public void read(DataReader in) throws IOException {
        super.read(in);
        stripped = in.readBoolean();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        super.write(out);
        out.writeBoolean(stripped);
    }
}
