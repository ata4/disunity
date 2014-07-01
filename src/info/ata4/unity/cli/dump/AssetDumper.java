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
import info.ata4.unity.asset.struct.ObjectPath;
import info.ata4.unity.asset.struct.TypeField;
import info.ata4.unity.asset.struct.TypeTree;
import info.ata4.unity.cli.classfilter.ClassFilter;
import info.ata4.unity.serdes.Deserializer;
import info.ata4.unity.serdes.UnityObject;
import info.ata4.unity.serdes.UnityTag;
import info.ata4.unity.util.ClassID;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
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
    private PrintWriter pw;
    
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
                if (Files.notExists(classDir)) {
                    Files.createDirectories(classDir);
                }
                
                String fileName = String.format("%06d.txt", path.getPathID());
                Path dumpFile = classDir.resolve(fileName);
                
                L.log(Level.FINE, "Writing {0}", fileName);
                
                try (Writer writer = Files.newBufferedWriter(dumpFile, CHARSET)) {
                    pw = new PrintWriter(writer);
                    dumpObject(obj);
                }
            } else {
                pw = new PrintWriter(System.out);
                dumpObject(obj);
                pw.flush();
            }
        }
    }
    
    public void dumpObject(UnityObject obj) {
        pw.print(obj.getType());
        
        if (!obj.getName().equals("Base")) {
            pw.print(" ");
            pw.println(obj.getName());
        } else {
            pw.println();
        }
        
        indentLevel++;       
        for (Map.Entry<String, UnityTag> field : obj.get().entrySet()) {
            indent();
            dumpField(field.getKey(), field.getValue());
        }
        indentLevel--;
    }
    
    public void dumpField(String name, UnityTag value) {
        if (!(value.get() instanceof UnityObject)) {
            pw.printf("%s %s = ", value.getType(), name);
        }
        dumpValue(value.get());
    }
    
    private void dumpValue(Object value) {
        if (value instanceof UnityObject) {
            UnityObject obj = (UnityObject) value;
            dumpObject(obj);
        } else if (value instanceof UnityTag) {
            UnityTag val = (UnityTag) value;
            Object valObj = val.get();
            
            if (valObj instanceof List) {
                dumpList(val, valObj);
            } else {
                dumpValue(valObj);
            }
        } else if (value instanceof ByteBuffer) {
            ByteBuffer bb = (ByteBuffer) value;
            pw.printf("byte[%d]\n", bb.capacity());
            dumpBytes(bb);
        } else if (value instanceof String) {
            pw.printf("\"%s\"\n", value);
        } else {
            pw.printf("%s\n", value);
        }
    }

    private void dumpBytes(ByteBuffer bb) {
        byte[] block = new byte[256];
        byte[] blockPrint;
        
        ByteBuffer bb2 = bb.duplicate();
        bb2.rewind();
        
        indentLevel++;
        
        while (bb2.hasRemaining()) {
            int len = Math.min(bb2.remaining(), block.length);
            bb2.get(block, 0, len);

            // copy block to smaller array if it's not completely filled,
            // since printHexBinary doesn't have a length parameter
            if (len != block.length) {
                blockPrint = new byte[len];
                System.arraycopy(block, 0, blockPrint, 0, len);
            } else {
                blockPrint = block;
            }
            
            indent();
            pw.println(DatatypeConverter.printHexBinary(blockPrint));
        }
        
        indentLevel--;
    }
    
    private void dumpList(UnityTag value, Object obj) {
        List list = (List) obj;
        pw.printf("%s[%d]\n", value.getType(), list.size());

        indentLevel++;
        
        for (Object valueInner : list) {
            indent();
            dumpValue(valueInner);
        }
        
        indentLevel--;
    }
    
    public void dumpStruct() throws IOException {
        TypeTree typeTree = asset.getTypeTree();
        
        if (typeTree.getFields().isEmpty()) {
            L.info("No type tree available");
            return;
        }
        
        Set<Integer> classIDs = asset.getClassIDs();
        
        for (Integer classID : classIDs) {
            TypeField classField = typeTree.getFields().get(classID);
            
            // skip filtered classes
            if (cf != null && !cf.accept(classID)) {
                continue;
            }
            
            if (classField == null) {
                continue;
            }
            
            if (outputDir != null) {
                if (Files.notExists(outputDir)) {
                    Files.createDirectories(outputDir);
                }
                
                String className = ClassID.getNameForID(classID, true);
                String fileName = String.format("%s.txt", className);
                Path file = outputDir.resolve(fileName);
                try (Writer writer = Files.newBufferedWriter(file, CHARSET)) {
                    pw = new PrintWriter(writer);
                    dumpType(classField);
                }
            } else {
                pw = new PrintWriter(System.out);
                dumpType(classField);
                pw.flush();
            }
        }
    }
    
    private void dumpType(TypeField field) {
        String name = field.getName();
        String type = field.getType();
        
        indent();
        
        pw.print(type);
        
        if (!name.equals("Base")) {
            pw.print(" ");
            pw.println(name);
        } else {
            pw.println();
        }
        
        indentLevel++;
        
        for (TypeField subField : field.getChildren()) {
            dumpType(subField);
        }
        
        indentLevel--;
    }
    
    private void indent() {
        for (int i = 0; i < indentLevel; i++) {
            pw.print(INDENT_STRING);
        }
    }
}
