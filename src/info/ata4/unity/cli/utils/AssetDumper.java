/*
 ** 2013 August 18
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.utils;

import info.ata4.unity.cli.DisUnitySettings;
import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.serdes.Deserializer;
import info.ata4.unity.serdes.UnityArray;
import info.ata4.unity.serdes.UnityField;
import info.ata4.unity.serdes.UnityObject;
import info.ata4.unity.struct.FieldType;
import info.ata4.unity.struct.ObjectPath;
import info.ata4.unity.struct.TypeTree;
import info.ata4.unity.struct.db.ClassID;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetDumper {
    
    private static final Logger L = Logger.getLogger(AssetDumper.class.getName());
    private static final String INDENT_STRING = "  ";
    
    private final AssetFile asset;
    private final DisUnitySettings settings;
    
    private PrintStream ps;
    private int indentLevel;

    public AssetDumper(AssetFile asset, DisUnitySettings settings) {
        this.asset = asset;
        this.settings = settings;
    }
    
    public void dumpData(PrintStream ps) {
        this.ps = ps;
        
        Deserializer deser = new Deserializer(asset);
        
        for (ObjectPath path : asset.getObjectPaths()) {
            try {
                if (path.classID2 < 0) {
                    continue;
                }
                
                // skip filtered classes
                if (settings.isClassFiltered(path.classID2)) {
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
            // skip filtered classes
            if (settings.isClassFiltered(classID)) {
                continue;
            }
            
            FieldType classField = typeTree.get(classID);
            
            if (classField == null) {
                continue;
            }
            
            printType(classField);
        }
    }
    
    private void printObject(UnityObject obj) {
        ps.print(obj.getType());
        
        if (!obj.getName().equals("Base")) {
            ps.print(" ");
            ps.println(obj.getName());
        } else {
            ps.println();
        }
        
        indentLevel++;
        
        for (UnityField field : obj.getFields()) {
            printIndent();
            
            Object value = field.getValue();
            if (value instanceof UnityObject) {
                printObject((UnityObject) value);
            } else {
                printField(field);
            }
        }
        
        indentLevel--;
    }
    
    private void printField(UnityField field) {
        String name = field.getName();
        String type = field.getType();
        Object value = field.getValue();
        
        if (name.contains(" ")) {
            ps.printf("%s \"%s\" = ", type, name, value);
        } else {
            ps.printf("%s %s = ", type, name, value);
        }
        
        indentLevel++;
        
        printValue(value);
        
        indentLevel--;
    }
    
    private void printValue(Object value) {
        if (value instanceof UnityObject) {
            printObject((UnityObject) value);
        } else if (value instanceof UnityArray) {
            UnityArray array = (UnityArray) value;
            if (array.isRaw()) {
                ps.printf("byte[%d]\n", array.getRaw().capacity());
                printBytes(array.getRaw());
            } else {
                List<Object> list = array.getList();
                ps.printf("%s[%d]\n", array.getType(), list.size());
                
                for (Object value2 : list) {
                    printIndent();
                    printValue(value2);
                }
                
            }
        } else if (value instanceof String) {
            ps.printf("\"%s\"\n", value);
        } else {
            ps.println(value);
        }
    }

    private void printType(FieldType field) {
        String name = field.name;
        String type = field.type;
        
        printIndent();
        
        ps.print(type);
        
        if (!name.equals("Base")) {
            ps.print(" ");
            ps.println(name);
        } else {
            ps.println();
        }
        
        indentLevel++;
        
        for (FieldType subField : field) {
            printType(subField);
        }
        
        indentLevel--;
    }

    private void printBytes(ByteBuffer bb) {
        byte[] block = new byte[256];
        ByteBuffer bb2 = bb.duplicate();
        bb2.rewind();
        while (bb2.hasRemaining()) {
            int len = Math.min(bb2.remaining(), block.length);
            bb2.get(block, 0, len);
            printIndent();
            ps.println(DatatypeConverter.printHexBinary(block));
        }
    }
    
    private void printIndent() {
        for (int i = 0; i < indentLevel; i++) {
            ps.print(INDENT_STRING);
        }
    }
}
