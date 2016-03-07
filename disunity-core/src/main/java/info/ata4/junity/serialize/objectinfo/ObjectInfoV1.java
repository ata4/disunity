/*
 ** 2015 November 26
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
public class ObjectInfoV1 extends ObjectInfo {

    // set to 1 if the object instance is destroyed?
    // (no longer stored in files starting with Unity 5)
    private short isDestroyed;

    public short isDestroyed() {
        return isDestroyed;
    }

    public void setDestroyed(short isDestroyed) {
        this.isDestroyed = isDestroyed;
    }

    @Override
    public void read(DataReader in) throws IOException {
        byteStart = in.readUnsignedInt();
        byteSize = in.readUnsignedInt();
        typeID = in.readInt();
        classID = in.readShort();
        isDestroyed = in.readShort();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeUnsignedInt(byteStart);
        out.writeUnsignedInt(byteSize);
        out.writeInt(typeID);
        out.writeShort((short) classID);
        out.writeShort(isDestroyed);
    }

}
