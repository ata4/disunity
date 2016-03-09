/*
 ** 2015 December 01
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.serialize.objectidentifier;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import info.ata4.junity.UnityTableStruct;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ObjectIdentifierTable extends UnityTableStruct<ObjectIdentifier> {

    public ObjectIdentifierTable() {
        super(ObjectIdentifier.class);
    }

    @Override
    public void read(DataReader in) throws IOException {
        super.read(in);
        in.align(4);
    }

    @Override
    public void write(DataWriter out) throws IOException {
        super.write(out);
        out.align(4);
    }
}
