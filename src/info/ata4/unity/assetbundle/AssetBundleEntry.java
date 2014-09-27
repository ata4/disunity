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

import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
import info.ata4.io.Struct;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetBundleEntry implements Struct {
    
    private String name;
    private long offset;
    private long length;
    private InputStream is;

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

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public InputStream getInputStream() {
        return is;
    }

    public void setInputStream(InputStream is) {
        this.is = is;
    }
    
    public boolean isAsset() {
        String ext = FilenameUtils.getExtension(getName());
        return !ext.equals("dll") && !ext.equals("mdb");
    }

    @Override
    public void read(DataInputReader in) throws IOException {
        name = in.readStringNull();
        offset = in.readUnsignedInt();
        length = in.readUnsignedInt();
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        out.writeStringNull(name);
        out.writeUnsignedInt(offset);
        out.writeUnsignedInt(length);
    }

    @Override
    public String toString() {
        return getName();
    }
}
