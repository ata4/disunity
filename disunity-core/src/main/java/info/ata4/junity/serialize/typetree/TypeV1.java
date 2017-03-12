/*
 ** 2015 November 23
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.serialize.typetree;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TypeV1 extends Type {

    @Override
    public void read(DataReader in) throws IOException {
        type = in.readStringNull(256);
        name = in.readStringNull(256);
        size = in.readInt();
        index = in.readInt();
        isArray = in.readInt() == 1;
        version = in.readInt();
        metaFlag = in.readInt();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeStringNull(type);
        out.writeStringNull(name);
        out.writeInt(size);
        out.writeInt(index);
        out.writeInt(isArray ? 1 : 0);
        out.writeInt(version);
        out.writeInt(metaFlag);
    }
}
