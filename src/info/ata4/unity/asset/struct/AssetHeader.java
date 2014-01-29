/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.asset.struct;

import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
import info.ata4.io.Struct;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetHeader implements Struct {
    
    public static final int SIZE = 20;
    
    // size of the structure data
    private int treeSize;
    
    // size of the whole asset file
    private int fileSize;
    
    // 5 = 2.0
    // 6 = 2.6
    // 7 = ???
    // 8 = 3.1 - 3.4
    // 9 = 3.5 - 4.x
    private int format;
    
    // apparently the offset to the serialized data
    private int dataOffset;
    
    // always 0?
    private int unknown;

    @Override
    public void read(DataInputReader in) throws IOException {
        treeSize = in.readInt();
        fileSize = in.readInt();
        format = in.readInt();
        dataOffset = in.readInt();
        unknown = in.readInt();
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        out.writeInt(treeSize);
        out.writeInt(fileSize);
        out.writeInt(format);
        out.writeInt(dataOffset);
        out.writeInt(unknown);
    }

    public int getTreeSize() {
        return treeSize;
    }

    public void setTreeSize(int treeSize) {
        this.treeSize = treeSize;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public int getDataOffset() {
        return dataOffset;
    }

    public void setDataOffset(int dataOffset) {
        this.dataOffset = dataOffset;
    }
}
