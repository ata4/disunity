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

import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
import info.ata4.io.Struct;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @unity SerializedFileHeader
 */
public class AssetHeader implements Struct {
    
    private final AssetVersionInfo versionInfo;
    
    // size of the structure data
    private int metadataSize;
    
    // size of the whole asset file
    private int fileSize;
    
    // offset to the serialized data
    private int dataOffset;
    
    // byte order of the serialized data?
    private byte endianness;
    
    // unused
    private final byte[] reserved = new byte[3];
    
    public AssetHeader(AssetVersionInfo versionInfo) {
        this.versionInfo = versionInfo;
    }

    @Override
    public void read(DataInputReader in) throws IOException {
        metadataSize = in.readInt();
        fileSize = in.readInt();
        versionInfo.setAssetVersion(in.readInt());
        dataOffset = in.readInt();
        endianness = in.readByte();
        in.readFully(reserved);
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        out.writeInt(metadataSize);
        out.writeInt(fileSize);
        out.writeInt(versionInfo.getAssetVersion());
        out.writeInt(dataOffset);
        out.writeByte(endianness);
        out.write(reserved);
    }

    public int getMetadataSize() {
        return metadataSize;
    }

    public void setMetadataSize(int metadataSize) {
        this.metadataSize = metadataSize;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getVersion() {
        return versionInfo.getAssetVersion();
    }

    public void setVersion(int version) {
        versionInfo.setAssetVersion(version);
    }

    public int getDataOffset() {
        return dataOffset;
    }

    public void setDataOffset(int dataOffset) {
        this.dataOffset = dataOffset;
    }

    public byte getEndianness() {
        return endianness;
    }

    public void setEndianness(byte endianness) {
        this.endianness = endianness;
    }
}
