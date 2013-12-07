/*
 ** 2013 December 06
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.tools;

import info.ata4.unity.asset.struct.AssetFieldType;
import info.ata4.unity.serdes.db.FieldTypeMap;
import info.ata4.unity.serdes.db.StructDatabase;
import info.ata4.util.collection.Pair;
import info.ata4.util.log.LogUtils;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class StructDBStats {
    
    private static final Logger L = Logger.getLogger(StructDBStats.class.getName());
    
    public static void main(String[] args) {
        LogUtils.configure();
        
        FieldTypeMap ftm = StructDatabase.getInstance().getFieldTypeMap();
        Map<String, AtomicInteger> classCounts = new TreeMap<>();
        Set<AssetFieldType> fieldNodes = new HashSet<>();

        int classCountTotal = 0;
        for (Map.Entry<Pair<Integer, String>, AssetFieldType> entry : ftm.entrySet()) {
            String revision = entry.getKey().getRight();
            fieldNodes.add(entry.getValue());
            if (!classCounts.containsKey(revision)) {
                classCounts.put(revision, new AtomicInteger(1));
            } else {
                AtomicInteger value = classCounts.get(revision);
                value.addAndGet(1);
                classCountTotal++;
            }
        }

        for (Map.Entry<String, AtomicInteger> entry : classCounts.entrySet()) {
            int classCount = entry.getValue().get();
            L.log(Level.INFO, "{0}: {1}", new Object[]{entry.getKey(), classCount});
        }

        L.log(Level.INFO, "Total structs:  {0}", classCountTotal);
        L.log(Level.INFO, "Unique structs: {0}", fieldNodes.size());
    }
}
