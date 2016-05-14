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
public class TypeV2 extends TypeV1 {

    // Unity 5+, the level of this type within the type tree
    protected int treeLevel;

    // Unity 5+, offset to the type string
    protected int typeOffset;

    // Unity 5+, offset to the name string
    protected int nameOffset;

    public int treeLevel() {
        return treeLevel;
    }

    public void treeLevel(int treeLevel) {
        this.treeLevel = treeLevel;
    }

    public int typeOffset() {
        return typeOffset;
    }

    public void typeOffset(int typeOffset) {
        this.typeOffset = typeOffset;
    }

    public int nameOffset() {
        return nameOffset;
    }

    public void nameOffset(int nameOffset) {
        this.nameOffset = nameOffset;
    }

    @Override
    public void read(DataReader in) throws IOException {
        version = in.readShort();
        treeLevel = in.readUnsignedByte();
        isArray = in.readBoolean();
        typeOffset = in.readInt();
        nameOffset = in.readInt();
        size = in.readInt();
        index = in.readInt();
        metaFlag = in.readInt();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeShort((short) version);
        out.writeUnsignedByte(treeLevel);
        out.writeBoolean(isArray);
        out.writeInt(typeOffset);
        out.writeInt(nameOffset);
        out.writeInt(size);
        out.writeInt(index);
        out.writeInt(metaFlag);
    }
}
