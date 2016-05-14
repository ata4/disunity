/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.serialize;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import info.ata4.io.Struct;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @unity SerializedFileHeader
 */
public class SerializedFileHeader implements Struct {

    // size of the structure data
    private long metadataSize;

    // size of the whole asset file
    private long fileSize;

    // 5 = 1.2 - 2.0
    // 6 = 2.1 - 2.6
    // 7 = 3.0 (?)
    // 8 = 3.1 - 3.4
    // 9 = 3.5 - 4.5
    // 11 = pre-5.0
    // 12 = pre-5.0
    // 13 = pre-5.0
    // 14 = 5.0
    // 15 = 5.0 (p3 and newer)
    private int version;

    // offset to the serialized data
    private long dataOffset;

    // byte order of the serialized data?
    private byte endianness;

    // unused
    private final byte[] reserved = new byte[3];

    public long metadataSize() {
        return metadataSize;
    }

    public void metadataSize(long metadataSize) {
        this.metadataSize = metadataSize;
    }

    public long fileSize() {
        return fileSize;
    }

    public void fileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int version() {
        return version;
    }

    public void version(int version) {
        this.version = version;
    }

    public long dataOffset() {
        return dataOffset;
    }

    public void dataOffset(long dataOffset) {
        this.dataOffset = dataOffset;
    }

    public byte endianness() {
        return endianness;
    }

    public void endianness(byte endianness) {
        this.endianness = endianness;
    }

    @Override
    public void read(DataReader in) throws IOException {
        metadataSize = in.readInt();
        fileSize = in.readUnsignedInt();
        version = in.readInt();
        dataOffset = in.readUnsignedInt();
        if (version >= 9) {
            endianness = in.readByte();
            in.readBytes(reserved);
        }
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeUnsignedInt(metadataSize);
        out.writeUnsignedInt(fileSize);
        out.writeInt(version);
        out.writeUnsignedInt(dataOffset);
        if (version >= 9) {
            out.writeByte(endianness);
            out.writeBytes(reserved);
        }
    }
}
