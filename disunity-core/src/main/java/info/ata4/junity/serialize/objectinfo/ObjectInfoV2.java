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
public class ObjectInfoV2 extends ObjectInfo {

    private short scriptTypeIndex;

    public short scriptTypeIndex() {
        return scriptTypeIndex;
    }

    public void scriptTypeIndex(short scriptTypeIndex) {
        this.scriptTypeIndex = scriptTypeIndex;
    }

    @Override
    public void read(DataReader in) throws IOException {
        byteStart = in.readUnsignedInt();
        byteSize = in.readUnsignedInt();
        typeID = in.readInt();
        classID = in.readShort();
        scriptTypeIndex = in.readShort();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeUnsignedInt(byteStart);
        out.writeUnsignedInt(byteSize);
        out.writeInt(typeID);
        out.writeShort((short) classID);
        out.writeShort(scriptTypeIndex);
    }

}
