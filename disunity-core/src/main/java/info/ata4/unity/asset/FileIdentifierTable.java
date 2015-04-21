/*
 ** 2013 August 16
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
import info.ata4.unity.util.UnityStruct;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FileIdentifierTable extends UnityStruct implements Iterable<FileIdentifier> {

    private final List<FileIdentifier> fileIDs;
    
    public FileIdentifierTable(VersionInfo versionInfo, List<FileIdentifier> fileIDs) {
        super(versionInfo);
        this.fileIDs = fileIDs;
    }
    
    @Override
    public void read(DataReader in) throws IOException {
        int entries = in.readInt();
        for (int i = 0; i < entries; i++) {
            FileIdentifier ref = new FileIdentifier(versionInfo);
            ref.read(in);
            fileIDs.add(ref);
        }
    }

    @Override
    public void write(DataWriter out) throws IOException {
        int entries = fileIDs.size();
        out.writeInt(entries);

        for (FileIdentifier ref : fileIDs) {
            ref.write(out);
        }
    }

    @Override
    public Iterator<FileIdentifier> iterator() {
        return fileIDs.iterator();
    }
}
