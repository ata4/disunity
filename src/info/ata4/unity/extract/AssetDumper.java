/*
 ** 2013 August 18
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.extract;

import info.ata4.unity.asset.Asset;
import info.ata4.unity.serdes.Deserializer;
import info.ata4.unity.serdes.UnityArray;
import info.ata4.unity.serdes.UnityField;
import info.ata4.unity.serdes.UnityObject;
import info.ata4.unity.struct.FieldType;
import info.ata4.unity.struct.ObjectPath;
import info.ata4.unity.struct.TypeTree;
import info.ata4.unity.util.ClassID;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetDumper {
    
    private static final Logger L = Logger.getLogger(AssetDumper.class.getName());
    
    private final Asset asset;
    private PrintStream ps;

    public AssetDumper(Asset asset) {
        this.asset = asset;
    }
    
    public void dumpData(PrintStream ps) {
        this.ps = ps;
        
        Deserializer deser = new Deserializer(asset);
        
        for (ObjectPath path : asset.getObjectPaths()) {
            try {
                if (path.classID1 < 0) {
                    continue;
                }

                printObject(deser.deserialize(path));
            } catch (Exception ex) {
                L.log(Level.SEVERE, "Deserialization failed for " + path.pathID + " (" + ClassID.getInstance().getNameForID(path.classID2) + ")", ex);
                break;
            }
        }
    }
    
    public void dumpStruct(PrintStream ps) {
        this.ps = ps;
        
        TypeTree typeTree = asset.getTypeTree();
        
        if (typeTree.isStandalone()) {
            L.info("No type tree available");
            return;
        }
        
        Set<Integer> classIDs = asset.getClassIDs();
        
        for (Integer classID : classIDs) {
            FieldType classField = typeTree.get(classID);
            
            if (classField == null) {
                continue;
            }
            
            printField(classField);
        }
    }
    
    private void printObject(UnityObject obj) {
        printObject(obj, 0);
    }
    
    private void printObject(UnityObject obj, int level) {
        ps.print(obj.getType());
        
        if (!obj.getName().equals("Base")) {
            ps.print(" ");
            ps.println(obj.getName());
        } else {
            ps.println();
        }
        
        level++;
        
        for (UnityField field : obj.getFields()) {
            printIndent(level);
            
            Object value = field.getValue();
            if (value instanceof UnityObject) {
                printObject((UnityObject) value, level);
            } else {
                printField(field, level);
            }
        }
    }
    
    private void printField(UnityField field, int level) {
        String name = field.getName();
        String type = field.getType();
        Object value = field.getValue();
        
        if (name.contains(" ")) {
            ps.printf("%s \"%s\" = ", type, name, value);
        } else {
            ps.printf("%s %s = ", type, name, value);
        }
        
        printValue(value, level + 1);
    }
    
    private void printValue(Object value, int level) {
        if (value instanceof UnityObject) {
            printObject((UnityObject) value, level + 1);
        } else if (value instanceof UnityArray) {
            UnityArray array = (UnityArray) value;
            if (array.isRaw()) {
                // TODO: print bytes?
                ps.printf("byte[%d]\n", array.getRaw().capacity());
            } else {
                List<Object> list = array.getList();
                ps.printf("%s[%d]\n", array.getType(), list.size());
                for (Object value2 : list) {
                    printIndent(level + 1);
                    printValue(value2, level + 1);
                }
            }
        } else if (value instanceof String) {
            ps.printf("\"%s\"\n", value);
        } else {
            ps.println(value);
        }
    }
    
    private void printIndent(int level) {
        for (int i = 0; i < level; i++) {
            ps.print("  ");
        }
    }
    
    private void printField(FieldType field) {
        printField(field, 0);
    }

    private void printField(FieldType field, int level) {
        String name = field.name;
        String type = field.type;
        
        printIndent(level);
        
        ps.print(type);
        
        if (!name.equals("Base")) {
            ps.print(" ");
            ps.println(name);
        } else {
            ps.println();
        }
        
        for (FieldType subField : field) {
            printField(subField, level + 1);
        }
    }
}
