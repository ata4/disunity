/*
 ** 2014 April 15
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.dump;

import info.ata4.log.LogUtils;
import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.asset.struct.ClassType;
import info.ata4.unity.asset.struct.FieldType;
import info.ata4.unity.asset.struct.ObjectPath;
import info.ata4.unity.cli.classfilter.ClassFilter;
import info.ata4.unity.serdes.Deserializer;
import info.ata4.unity.serdes.UnityBuffer;
import info.ata4.unity.serdes.UnityField;
import info.ata4.unity.serdes.UnityList;
import info.ata4.unity.serdes.UnityObject;
import info.ata4.unity.util.ClassID;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
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
    
    private static final Logger L = LogUtils.getLogger();
    private static final String INDENT_STRING = "  ";
    private static final Charset CHARSET = Charset.forName("UTF-8");
    
    private final AssetFile asset;
    private ClassFilter cf;
    private Path outputDir;
    private int indentLevel;
    
    public AssetDumper(AssetFile asset) {
        this.asset = asset;
    }
    
    public Path getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(Path outputDir) {
        this.outputDir = outputDir;
    }
    
    public ClassFilter getClassFilter() {
        return cf;
    }

    public void setClassFilter(ClassFilter cf) {
        this.cf = cf;
    }
    
    public void dumpData() throws IOException {
        Deserializer deser = new Deserializer(asset);

        for (ObjectPath path : asset.getPaths()) {
            // skip MonoBehaviours
            if (path.isScript()) {
                continue;
            }

            // skip filtered classes
            if (cf != null && !cf.accept(path)) {
                continue;
            }

            UnityObject obj;
            try {
                obj = deser.deserialize(path);
            } catch (Exception ex) {
                L.log(Level.SEVERE, "Deserialization failed for " + path, ex);
                continue;
            }
            
            if (outputDir != null) {
                String className = ClassID.getNameForID(path.getClassID(), true);
                
                Path classDir = outputDir.resolve(className);
                if (!Files.exists(classDir)) {
                    Files.createDirectory(classDir);
                }
                
                String fileName = String.format("%06d.txt", path.getPathID());
                Path dumpFile = classDir.resolve(fileName);
                
                L.log(Level.FINE, "Writing {0}", fileName);
                
                try (Writer writer = Files.newBufferedWriter(dumpFile, CHARSET)) {
                    dumpObject(new PrintWriter(writer), obj);
                }
            } else {
                PrintWriter pw = new PrintWriter(System.out);
                dumpObject(pw, obj);
                pw.flush();
            }
        }
    }
    
    public void dumpStruct() throws IOException {
        ClassType classType = asset.getClassType();
        
        if (!classType.hasTypeTree()) {
            L.info("No type tree available");
            return;
        }
        
        Set<Integer> classIDs = asset.getClassIDs();
        
        for (Integer classID : classIDs) {
            FieldType classField = classType.getTypeTree().get(classID);
            
            // skip filtered classes
            if (cf != null && !cf.accept(classID)) {
                continue;
            }
            
            if (classField == null) {
                continue;
            }
            
            if (outputDir != null) {
                String className = ClassID.getNameForID(classID, true);
                String fileName = String.format("%s.txt", className);
                Path file = outputDir.resolve(fileName);
                try (Writer writer = Files.newBufferedWriter(file, CHARSET)) {
                    dumpType(new PrintWriter(writer), classField);
                }
            } else {
                PrintWriter pw = new PrintWriter(System.out);
                dumpType(pw, classField);
                pw.flush();
            }
        }
    }
    
    public void dumpObject(PrintWriter pw, UnityObject obj) {
        pw.print(obj.getType());
        
        if (!obj.getName().equals("Base")) {
            pw.print(" ");
            pw.println(obj.getName());
        } else {
            pw.println();
        }
        
        indentLevel++;
        
        for (UnityField field : obj.getFields()) {
            indent(pw);
            
            Object value = field.getValue();
            if (value instanceof UnityObject) {
                dumpObject(pw, (UnityObject) value);
            } else {
                dumpField(pw, field);
            }
        }
        
        indentLevel--;
    }
    
    public void dumpField(PrintWriter pw, UnityField field) {
        String name = field.getName();
        String type = field.getType();
        Object value = field.getValue();
        
        if (name.contains(" ")) {
            pw.printf("%s \"%s\" = ", type, name, value);
        } else {
            pw.printf("%s %s = ", type, name, value);
        }
        
        indentLevel++;
        
        dumpValue(pw, value);
        
        indentLevel--;
    }
    
    private void dumpValue(PrintWriter pw, Object value) {
        if (value instanceof UnityObject) {
            dumpObject(pw, (UnityObject) value);
        } else if (value instanceof UnityList) {
            UnityList array = (UnityList) value;
            List<Object> list = array.getList();
            pw.printf("%s[%d]\n", array.getType(), list.size());
            for (Object value2 : list) {
                indent(pw);
                dumpValue(pw, value2);
            }
        } else if (value instanceof UnityBuffer) {
            ByteBuffer bb = ((UnityBuffer) value).getBuffer();
            pw.printf("byte[%d]\n", bb.capacity());
            dumpBytes(pw, bb);
        } else if (value instanceof String) {
            pw.printf("\"%s\"\n", value);
        } else {
            pw.println(value);
        }
    }

    private void dumpType(PrintWriter pw, FieldType field) {
        String name = field.getName();
        String type = field.getType();
        
        indent(pw);
        
        pw.print(type);
        
        if (!name.equals("Base")) {
            pw.print(" ");
            pw.println(name);
        } else {
            pw.println();
        }
        
        indentLevel++;
        
        for (FieldType subField : field.getChildren()) {
            dumpType(pw, subField);
        }
        
        indentLevel--;
    }

    private void dumpBytes(PrintWriter pw, ByteBuffer bb) {
        byte[] block = new byte[256];
        ByteBuffer bb2 = bb.duplicate();
        bb2.rewind();
        while (bb2.hasRemaining()) {
            int len = Math.min(bb2.remaining(), block.length);
            bb2.get(block, 0, len);
            indent(pw);
            pw.println(DatatypeConverter.printHexBinary(block));
        }
    }
    
    private void indent(PrintWriter pw) {
        for (int i = 0; i < indentLevel; i++) {
            pw.print(INDENT_STRING);
        }
    }
}
