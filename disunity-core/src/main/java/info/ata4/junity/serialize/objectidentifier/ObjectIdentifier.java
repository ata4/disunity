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
import info.ata4.io.Struct;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @unity LocalSerializedObjectIdentifier
 */
public class ObjectIdentifier implements Struct {

    private int serializedFileIndex;
    private long identifierInFile;

    public int serializedFileIndex() {
        return serializedFileIndex;
    }

    public void serializedFileIndex(int serializedFileIndex) {
        this.serializedFileIndex = serializedFileIndex;
    }

    public long identifierInFile() {
        return identifierInFile;
    }

    public void identifierInFile(long identifierInFile) {
        this.identifierInFile = identifierInFile;
    }

    @Override
    public void read(DataReader in) throws IOException {
        serializedFileIndex = in.readInt();
        identifierInFile = in.readLong();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeInt(serializedFileIndex);
        out.writeLong(identifierInFile);
    }

}
