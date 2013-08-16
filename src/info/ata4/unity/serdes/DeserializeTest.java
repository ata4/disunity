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

import info.ata4.unity.asset.Asset;
import info.ata4.unity.struct.ObjectPath;
import info.ata4.unity.util.ClassID;
import info.ata4.util.log.LogUtils;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
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
        
        for (String arg : args) {
            try {
                File file = new File(arg);

                Asset asset = new Asset();
                asset.load(file);
                
                Deserializer deser = new Deserializer(asset);
                
                boolean all = false;
                
                if (all) {
                    for (ObjectPath path : asset.getObjectPaths()) {
                        try {
                            if (path.classID2 == 43 || path.classID2 == 83 || path.classID2 == 129 || path.classID1 < 0) {
                                continue;
                            }

                            deser.deserialize(path);
                        } catch (Exception ex) {
                            L.log(Level.SEVERE, "Deserialization failed for " + path.pathID + " (" + ClassID.getNameForID(path.classID2) + ")", ex);
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
    
    public static void dump(PrintStream ps, UnityObject obj, int level) {
        if (obj.getName().equals("Base")) {
            ps.println(obj.getType());
        } else {
            ps.println(obj.getType() + " " + obj.getName());
        }
        
        level++;

        for (UnityField field : obj.values()) {
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
