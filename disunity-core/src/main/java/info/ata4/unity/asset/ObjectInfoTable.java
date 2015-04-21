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

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import info.ata4.unity.util.UnityStruct;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ObjectInfoTable extends UnityStruct {
    
    private final Map<Long, ObjectInfo> infoMap;

    public ObjectInfoTable(VersionInfo versionInfo, Map<Long, ObjectInfo> infoMap) {
        super(versionInfo);
        this.infoMap = infoMap;
    }
    
    @Override
    public void read(DataReader in) throws IOException {
        int entries = in.readInt();
        
        if (versionInfo.assetVersion() > 13) {
            in.align(4);
        }

        for (int i = 0; i < entries; i++) {
            long pathID;
            if (versionInfo.assetVersion() > 13) {
                pathID = in.readLong();
            } else {
                pathID = in.readUnsignedInt();
            }
            
            ObjectInfo info = new ObjectInfo(versionInfo);
            info.read(in);
            infoMap.put(pathID, info);
        }
    }

    @Override
    public void write(DataWriter out) throws IOException {
        int entries = infoMap.size();
        out.writeInt(entries);
        
        if (versionInfo.assetVersion() > 13) {
            out.align(4);
        }

        for (Map.Entry<Long, ObjectInfo> infoEntry : infoMap.entrySet()) {
            long pathID = infoEntry.getKey();
            ObjectInfo info = infoEntry.getValue();
            
            if (versionInfo.assetVersion() > 13) {
                out.writeLong(pathID);
            } else {
                out.writeUnsignedInt(pathID);
            }
            
            info.write(out);
        }
    }
}
