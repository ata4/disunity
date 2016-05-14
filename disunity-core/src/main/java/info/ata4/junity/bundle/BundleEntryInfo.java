/*
 ** 2014 September 25
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.bundle;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import info.ata4.io.Struct;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @unity StreamingInfo
 */
public class BundleEntryInfo implements Struct {

    private String name;
    private long offset;
    private long size;

    public String name() {
        return name;
    }

    public void name(String name) {
        this.name = name;
    }

    public long offset() {
        return offset;
    }

    public void offset(long offset) {
        this.offset = offset;
    }

    public long size() {
        return size;
    }

    public void size(long size) {
        this.size = size;
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
        return name();
    }
}
