/*
 ** 2013 June 17
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.serialize.objectinfo;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ObjectInfoTableV1<T extends ObjectInfo> extends ObjectInfoTable<T> {

    public ObjectInfoTableV1(Class<T> elementFactory) {
        super(elementFactory);
    }

    @Override
    public void read(DataReader in) throws IOException {
        int entries = in.readInt();

        for (int i = 0; i < entries; i++) {
            long pathID = in.readUnsignedInt();
            T info = createElement();
            in.readStruct(info);
            infoMap.put(pathID, info);
        }
    }

    @Override
    public void write(DataWriter out) throws IOException {
        int entries = infoMap.size();
        out.writeInt(entries);

        for (Map.Entry<Long, T> infoEntry : infoMap.entrySet()) {
            long pathID = infoEntry.getKey();
            ObjectInfo info = infoEntry.getValue();
            out.writeUnsignedInt(pathID);
            out.writeStruct(info);
        }
    }
}
