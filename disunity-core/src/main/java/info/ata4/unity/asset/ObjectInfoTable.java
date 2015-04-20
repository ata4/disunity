/*
 ** 2013 June 17
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.asset;

import info.ata4.unity.util.UnityStruct;
import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ObjectInfoTable extends UnityStruct {
    
    private final Map<Integer, ObjectInfo> infoMap;

    public ObjectInfoTable(Map<Integer, ObjectInfo> infoMap, VersionInfo versionInfo) {
        super(versionInfo);
        this.infoMap = infoMap;
    }
    
    @Override
    public void read(DataReader in) throws IOException {
        int entries = in.readInt();

        for (int i = 0; i < entries; i++) {
            int pathID = in.readInt();
            ObjectInfo info = new ObjectInfo(versionInfo);
            
            info.read(in);
            infoMap.put(pathID, info);
        }
    }

    @Override
    public void write(DataWriter out) throws IOException {
        int entries = infoMap.size();
        out.writeInt(entries);

        for (Map.Entry<Integer, ObjectInfo> infoEntry : infoMap.entrySet()) {
            int pathID = infoEntry.getKey();
            ObjectInfo info = infoEntry.getValue();
            
            out.writeInt(pathID);
            info.write(out);
        }
    }
}
