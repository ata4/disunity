/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.asset;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import info.ata4.unity.util.UnityStruct;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @unity SerializedFileHeader
 */
public class AssetHeader extends UnityStruct {
    
    // size of the structure data
    private long metadataSize;
    
    // size of the whole asset file
    private long fileSize;
    
    // offset to the serialized data
    private long dataOffset;
    
    // byte order of the serialized data?
    private byte endianness;
    
    // unused
    private final byte[] reserved = new byte[3];

    public AssetHeader(VersionInfo versionInfo) {
        super(versionInfo);
    }

    @Override
    public void read(DataReader in) throws IOException {
        metadataSize = in.readInt();
        fileSize = in.readUnsignedInt();
        versionInfo.assetVersion(in.readInt());
        dataOffset = in.readUnsignedInt();
        if (versionInfo.assetVersion() >= 9) {
            endianness = in.readByte();
            in.readBytes(reserved);
        }
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeUnsignedInt(metadataSize);
        out.writeUnsignedInt(fileSize);
        out.writeInt(versionInfo.assetVersion());
        out.writeUnsignedInt(dataOffset);
        if (versionInfo.assetVersion() >= 9) {
            out.writeByte(endianness);
            out.writeBytes(reserved);
        }
    }

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
        return versionInfo.assetVersion();
    }

    public void version(int version) {
        versionInfo.assetVersion(version);
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
}
