/*
 ** 2013 August 11
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.serdes;

import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.struct.FieldType;
import info.ata4.unity.struct.ObjectPath;
import info.ata4.unity.struct.db.FieldTypeMap;
import info.ata4.unity.struct.db.StructDatabase;
import info.ata4.unity.struct.db.ClassID;
import info.ata4.util.collection.Pair;
import info.ata4.util.log.LogUtils;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
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
public class DeserializeTest {
    
    private static final Logger L = Logger.getLogger(DeserializeTest.class.getName());
    
    public static void main(String[] args) {
        LogUtils.configure();
        
        if (args.length == 0) {
            FieldTypeMap ftm = StructDatabase.getInstance().getFieldTypeMap();
            Map<String, AtomicInteger> classCounts = new TreeMap<>();
            Set<FieldType> fieldNodes = new HashSet<>();
            
            int classCountMax = 0;
            int classCountTotal = 0;
            for (Map.Entry<Pair<Integer, String>, FieldType> entry : ftm.entrySet()) {
                String revision = entry.getKey().getRight();
                fieldNodes.add(entry.getValue());
                if (!classCounts.containsKey(revision)) {
                    classCounts.put(revision, new AtomicInteger(1));
                } else {
                    AtomicInteger value = classCounts.get(revision);
                    value.addAndGet(1);
                    classCountMax = Math.max(classCountMax, value.get());
                    classCountTotal++;
                }
            }
            
            for (Map.Entry<String, AtomicInteger> entry : classCounts.entrySet()) {
                int classCount = entry.getValue().get();
                float percent = classCount / (float) classCountMax * 100;
                System.out.printf("%s: %d (%.2f%%)\n", entry.getKey(), classCount, percent);
            }
            
            System.out.println("Total structs:  " + classCountTotal);
            System.out.println("Unique structs: " + fieldNodes.size());
        } else {
            for (String arg : args) {
                try {
                    File file = new File(arg);

                    AssetFile asset = new AssetFile();
                    asset.load(file);

                    Deserializer deser = new Deserializer(asset);

                    boolean all = false;

                    if (all) {
                        for (ObjectPath path : asset.getObjectPaths()) {
                            try {
                                if (path.classID1 < 0) {
                                    continue;
                                }

                                deser.deserialize(path);
                            } catch (Exception ex) {
                                L.log(Level.SEVERE, "Deserialization failed for " + path.pathID + " (" + ClassID.getInstance().getNameForID(path.classID2) + ")", ex);
                                break;
                            }
                        }
                    } else {
                        try {
                            ObjectPath path = asset.getPathsByID(129).get(0);
                            UnityObject obj = deser.deserialize(path);
                            dump(System.out, obj, 0);
                        } catch (Exception ex) {
                            L.log(Level.SEVERE, "Deserialization failed", ex);
                        }

                        try {
                            ObjectPath path = asset.getPathsByID(141).get(0);
                            UnityObject obj = deser.deserialize(path);
                            dump(System.out, obj, 0);
                        } catch (Exception ex) {
                            L.log(Level.SEVERE, "Deserialization failed", ex);
                        }
                    }
                } catch (IOException ex) {
                    L.log(Level.SEVERE, "Can't read asset file", ex);
                }
            }
        }
    }
    
    public static void dump(PrintStream ps, UnityObject obj, int level) {
        if (obj.getName().equals("Base")) {
            ps.println(obj.getType());
        } else {
            ps.println(obj.getType() + " " + obj.getName());
        }
        
        level++;

        for (UnityField field : obj.getFields()) {
            for (int i = 0; i < level; i++) {
                ps.print("  ");
            }
            
            String name = field.getName();
            String type = field.getType();
            Object value = field.getValue();
            
            if (field.getValue() instanceof UnityObject) {
                dump(ps, (UnityObject) field.getValue(), level);
            } else if (field.getValue() instanceof String) {
                ps.printf("%s %s = \"%s\"\n", type, name, value);
            } else {
                ps.printf("%s %s = %s\n", type, name, value);
            }
        }
    }
}
