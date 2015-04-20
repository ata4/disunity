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
import info.ata4.io.Struct;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @unity SerializedFileHeader
 */
public class AssetHeader extends VersionInfoContainer implements Struct {
    
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
        versionInfo.setAssetVersion(in.readInt());
        dataOffset = in.readUnsignedInt();
        if (versionInfo.getAssetVersion() >= 9) {
            endianness = in.readByte();
            in.readBytes(reserved);
        }
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeUnsignedInt(metadataSize);
        out.writeUnsignedInt(fileSize);
        out.writeInt(versionInfo.getAssetVersion());
        out.writeUnsignedInt(dataOffset);
        if (versionInfo.getAssetVersion() >= 9) {
            out.writeByte(endianness);
            out.writeBytes(reserved);
        }
    }

    public long getMetadataSize() {
        return metadataSize;
    }

    public void setMetadataSize(long metadataSize) {
        this.metadataSize = metadataSize;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getVersion() {
        return versionInfo.getAssetVersion();
    }

    public void setVersion(int version) {
        versionInfo.setAssetVersion(version);
    }

    public long getDataOffset() {
        return dataOffset;
    }

    public void setDataOffset(long dataOffset) {
        this.dataOffset = dataOffset;
    }

    public byte getEndianness() {
        return endianness;
    }

    public void setEndianness(byte endianness) {
        this.endianness = endianness;
    }
}
