/*
 ** 2014 January 08
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.action;

import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.asset.struct.AssetClassType;
import info.ata4.unity.asset.struct.AssetFieldType;
import info.ata4.unity.asset.struct.AssetObjectPath;
import info.ata4.unity.cli.classfilter.ClassFilter;
import info.ata4.unity.serdes.Deserializer;
import info.ata4.unity.serdes.UnityBuffer;
import info.ata4.unity.serdes.UnityField;
import info.ata4.unity.serdes.UnityList;
import info.ata4.unity.serdes.UnityObject;
import java.io.IOException;
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
public class DumpAction extends PrintAction {
    
    private static final Logger L = Logger.getLogger(DumpAction.class.getName());
    private static final String INDENT_STRING = "  ";
    
    private boolean dumpStructs;
    private int indentLevel;
    
    public DumpAction(PrintStream ps) {
        super(ps);
    }
    
    public boolean isDumpStructs() {
        return dumpStructs;
    }

    public DumpAction setDumpStructs(boolean structs) {
        this.dumpStructs = structs;
        return this;
    }

    @Override
    public boolean supportsAssets() {
        return true;
    }

    @Override
    public boolean supportsAssetBundes() {
        return false;
    }

    @Override
    public void processAsset(AssetFile asset) throws IOException {
        if (dumpStructs) {
            printStruct(asset);
        } else {
            printData(asset);
        }
    }
    
    public void printData(AssetFile asset) {
        Deserializer deser = new Deserializer(asset);

        for (AssetObjectPath path : asset.getPaths()) {
            try {
                // skip MonoBehaviours
                if (path.isScript()) {
                    continue;
                }

                // skip filtered classes
                ClassFilter cf = getClassFilter();
                if (cf != null && !cf.accept(path)) {
                    continue;
                }

                printObject(deser.deserialize(path));
            } catch (Exception ex) {
                L.log(Level.SEVERE, "Deserialization failed for " + path, ex);
                break;
            }
        }
    }
    
    public void printStruct(AssetFile asset) {
        AssetClassType classType = asset.getClassType();
        
        if (!classType.hasTypeTree()) {
            L.info("No type tree available");
            return;
        }
        
        Set<Integer> classIDs = asset.getClassIDs();
        
        for (Integer classID : classIDs) {
            AssetFieldType classField = classType.getTypeTree().get(classID);
            
            // skip filtered classes
            ClassFilter cf = getClassFilter();
            if (cf != null && !cf.accept(classID)) {
                continue;
            }
            
            if (classField == null) {
                continue;
            }
            
            printType(classField);
        }
    }
    
    public void printObject(UnityObject obj) {
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
    
    public void printField(UnityField field) {
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
        } else if (value instanceof UnityList) {
            UnityList array = (UnityList) value;
            List<Object> list = array.getList();
            ps.printf("%s[%d]\n", array.getType(), list.size());
            for (Object value2 : list) {
                printIndent();
                printValue(value2);
            }
        } else if (value instanceof UnityBuffer) {
            ByteBuffer bb = ((UnityBuffer) value).getBuffer();
            ps.printf("byte[%d]\n", bb.capacity());
            printBytes(bb);
        } else if (value instanceof String) {
            ps.printf("\"%s\"\n", value);
        } else {
            ps.println(value);
        }
    }

    private void printType(AssetFieldType field) {
        String name = field.getName();
        String type = field.getType();
        
        printIndent();
        
        ps.print(type);
        
        if (!name.equals("Base")) {
            ps.print(" ");
            ps.println(name);
        } else {
            ps.println();
        }
        
        indentLevel++;
        
        for (AssetFieldType subField : field) {
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
