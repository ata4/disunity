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

import java.io.IOException;

/**
 * UnityFS-format bundle entry info
 */
public class BundleEntryInfoFS extends BundleEntryInfo {

    // unknown extra field, guessing flags
    private long flags;

    public long flags() { return flags; };

    public void flags(long flags) { this.flags = flags; }

    @Override
    public void read(DataReader in) throws IOException {
        offset(in.readLong());
        size(in.readLong());
        flags = in.readUnsignedInt();
        name(in.readStringNull());
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeLong(offset());
        out.writeLong(size());
        out.writeUnsignedInt(flags);
        out.writeStringNull(name());
    }

    @Override
    public String toString() {
        return name();
    }
}
