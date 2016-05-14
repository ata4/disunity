/*
 ** 2015 November 23
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.serialize.fileidentifier;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FileIdentifierV1 extends FileIdentifier {

    @Override
    public void read(DataReader in) throws IOException {
        in.readStruct(guid);
        type = in.readInt();
        filePath = in.readStringNull();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeStruct(guid);
        out.writeInt(type);
        out.writeStringNull(filePath);
    }
}
