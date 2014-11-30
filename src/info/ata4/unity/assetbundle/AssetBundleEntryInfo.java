/*
 ** 2014 September 25
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.assetbundle;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import info.ata4.io.Struct;
import java.io.IOException;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetBundleEntryInfo implements Struct {
    
    private String name;
    private long offset;
    private long size;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
    
    public boolean isAsset() {
        String ext = FilenameUtils.getExtension(getName());
        return !ext.equals("dll") && !ext.equals("mdb");
    }

    @Override
    public void read(DataReader in) throws IOException {
        name = in.readStringNull();
        offset = in.readUnsignedInt();
        size = in.readUnsignedInt();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeStringNull(name);
        out.writeUnsignedInt(offset);
        out.writeUnsignedInt(size);
    }

    @Override
    public String toString() {
        return getName();
    }
}
