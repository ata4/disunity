/*
 ** 2013 July 05
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.extract;

import info.ata4.unity.asset.Asset;
import info.ata4.unity.serialization.AssetDeserializer;
import info.ata4.unity.serialization.AssetObject;
import info.ata4.unity.struct.AssetHeader;
import info.ata4.unity.struct.AssetRef;
import info.ata4.unity.struct.FieldNode;
import info.ata4.unity.struct.FieldTree;
import info.ata4.unity.struct.ObjectPath;
import info.ata4.unity.struct.ObjectTable;
import info.ata4.unity.util.ClassID;
import info.ata4.util.collection.MapUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;

/**
 * Helper class to output information about an asset file.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetStructure {
    
    private static final Logger L = Logger.getLogger(AssetStructure.class.getName());
    
    private final Asset asset;

    public AssetStructure(Asset asset) {
        this.asset = asset;
    }
    
    public void learnStruct() {                
        int learned = StructDatabase.getInstance().learn(asset);
        if (learned > 0) {
            L.log(Level.INFO, "New structs: {0}", learned);
        }
    }
    
    public void dumpStruct(File dir) throws FileNotFoundException {
        printStruct(null, dir);
    }
    
    public void printStruct(PrintStream ps) throws FileNotFoundException {
        printStruct(ps, null);
    }
    
    private void printStruct(PrintStream ps, File dir) throws FileNotFoundException {
        FieldTree fieldTree = asset.getFieldTree();
        if (!fieldTree.isStandalone()) {
            L.info("No structure data available");
            return;
        }
        
        Set<Integer> classIDs = asset.getClassIDs();
        
        for (Integer classID : classIDs) {
            FieldNode classField = fieldTree.get(classID);
            
            if (classField == null) {
                continue;
            }
            
            String className = ClassID.getSafeNameForID(classID);
            StringBuilder indent = new StringBuilder(256);
            
            if (dir != null) {
                File structFile = new File(dir, className + ".txt");
                try (PrintStream psClass = new PrintStream(structFile)) {
                    printField(psClass, classField, indent, true);
                }
            } else {
                printField(ps, classField, indent, true);
            }
        }
        
        L.log(Level.INFO, "Dumped {0} class structures", classIDs.size());
    }
    
    private void printField(PrintStream ps, FieldNode field, StringBuilder indent, boolean last) {
        ps.print(indent.toString());
        
        if (last) {
            ps.print("+--");
            indent.append("   ");
        } else {
            ps.print("|--");
            indent.append("|  ");
        }
        
        final int subFieldCount = field.size();
        
        ps.print(field.type + " \"" + field.name + "\"");
        
        if (subFieldCount > 0) {
            ps.print(" (" + subFieldCount + ")");
        }

        ps.println();
        
        for (int i = 0; i < subFieldCount; i++) {
            FieldNode subField = field.get(i);
            boolean lastField = i == subFieldCount - 1;
            printField(ps, subField, indent, lastField);
        }
        
        // remove indent used for this node
        indent.delete(indent.length() - 3, indent.length());
    }
    
    public void printInfo(PrintStream ps) {
        ObjectTable objTable = asset.getObjectTable();
        AssetHeader header = asset.getHeader();
        FieldTree fieldTree = asset.getFieldTree();
        
        ps.println("Engine: " + fieldTree.revision);
        ps.println("Objects: " + objTable.getPaths().size());
        ps.println("Size: " + humanReadableByteCount(header.fileSize, true));
        ps.println("Format: " + header.format);
        
        if (!fieldTree.isEmpty()) {
            printPlayerSettings(ps);
        }
        
        if (!objTable.getRefs().isEmpty()) {
            ps.println("External references:");
            for (AssetRef ref : objTable.getRefs()) {
                ps.println("  Path: " + ref.path);
                ps.println("  GUID: " + DatatypeConverter.printHexBinary(ref.guid));
                ps.println("  Type: " + ref.type);
                ps.println();
            }
        }
    }
    
    private void printPlayerSettings(PrintStream ps) {
        try {
            List<ObjectPath> paths = asset.getPathsByID(ClassID.getIDForName("PlayerSettings"));
            
            // only the mainData contains a PlayerSettings object
            if (paths.isEmpty()) {
                return;
            }
            
            AssetDeserializer deser = new AssetDeserializer(asset);
            AssetObject playerSettings = deser.deserializeObject(paths.get(0));

            // only print the most interesting fields
            printField(ps, playerSettings, "productName");
            printField(ps, playerSettings, "companyName");
            printField(ps, playerSettings, "targetPlatform");
            printField(ps, playerSettings, "targetDevice");
            printField(ps, playerSettings, "targetResolution");
        } catch (Exception ex) {
            L.log(Level.WARNING, "Can't read PlayerSettings", ex);
        }
    }
    
    private void printField(PrintStream ps, AssetObject obj, String fieldName) {
        if (obj.containsKey(fieldName)) {
            ps.println(fieldName + ": " + obj.get(fieldName));
        }
    }
    
    public void printStats(PrintStream ps) {
        ObjectTable objTable = asset.getObjectTable();
        Map<String, Integer> classCounts = new HashMap<>();
        Map<String, Integer> classSizes = new HashMap<>();
        
        for (ObjectPath path : objTable.getPaths()) {
            String className = ClassID.getSafeNameForID(path.classID2);
            
            if (!classCounts.containsKey(className)) {
                classCounts.put(className, 0);
                classSizes.put(className, 0);
            }
            
            classCounts.put(className, classCounts.get(className) + 1);
            classSizes.put(className, classSizes.get(className) + path.length);
        }
        
        ps.println("Classes by quantity:");
        Map<String, Integer> classCountsSorted = MapUtils.sortByValue(classCounts, true);
        for (Map.Entry<String, Integer> entry : classCountsSorted.entrySet()) {
            String className = entry.getKey();
            int classCount = entry.getValue();
            ps.printf("  %s: %d\n", className, classCount);
        }

        ps.println("Classes by data size:");
        Map<String, Integer> classSizesSorted = MapUtils.sortByValue(classSizes, true);
        for (Map.Entry<String, Integer> entry : classSizesSorted.entrySet()) {
            String className = entry.getKey();
            String classSize = humanReadableByteCount(entry.getValue(), true);
            ps.printf("  %s: %s\n", className, classSize);
        }
        ps.println();
    }
    
    private String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
