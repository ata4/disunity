/*
 ** 2013 July 05
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.utils;

import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.asset.struct.AssetHeader;
import info.ata4.unity.asset.struct.AssetObjectPath;
import info.ata4.unity.asset.struct.AssetObjectPathTable;
import info.ata4.unity.asset.struct.AssetRef;
import info.ata4.unity.asset.struct.AssetRefTable;
import info.ata4.unity.asset.struct.AssetTypeTree;
import info.ata4.unity.serdes.Deserializer;
import info.ata4.unity.serdes.UnityObject;
import info.ata4.unity.serdes.db.StructDatabase;
import info.ata4.unity.util.ClassID;
import info.ata4.util.collection.MapUtils;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Helper class to output information about an asset file.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetUtils {
    
    private static final Logger L = Logger.getLogger(AssetUtils.class.getName());
    
    private final AssetFile asset;

    public AssetUtils(AssetFile asset) {
        this.asset = asset;
    }
    
    public void learnStruct() {                
        int learned = StructDatabase.getInstance().learn(asset);
        if (learned > 0) {
            L.log(Level.INFO, "New structs: {0}", learned);
        }
    }

    public void printInfo(PrintStream ps) {
        AssetObjectPathTable objTable = asset.getObjectPaths();
        AssetRefTable refTable = asset.getReferences();
        AssetHeader header = asset.getHeader();
        AssetTypeTree fieldTree = asset.getTypeTree();
        
        ps.println("Header");
        ps.println("  File size: " + humanReadableByteCount(header.fileSize, true));
        ps.println("  Tree size: " + humanReadableByteCount(header.treeSize, true));
        ps.println("  Format: " + header.format);
        ps.println("  Data offset: " + header.dataOffset);
        ps.println("  Unknown: " + header.unknown);
        ps.println();
        
        ps.println("Serialized data");
        ps.println("  Revision: " + fieldTree.revision);
        ps.println("  Version: " + fieldTree.version);
        ps.println("  Standalone: " + (fieldTree.isStandalone() ? "yes" : "no"));
        ps.println("  Objects: " + objTable.size());
        ps.println();
        
        if (!refTable.isEmpty()) {
            ps.println("External references");
            for (AssetRef ref : refTable) {
                if (!ref.assetPath.isEmpty()) {
                    ps.printf("  Asset path: \"%s\"\n", ref.assetPath);
                }
                if (!ref.filePath.isEmpty()) {
                    ps.printf("  File path: \"%s\"\n", ref.filePath);
                }
                ps.printf("  GUID: %s\n", DatatypeConverter.printHexBinary(ref.guid));
                ps.printf("  Type: %d\n", ref.type);
                ps.println();
            }
        }
    }
    
    public void fixRefs() throws IOException {
        File sourceFile = asset.getSourceFile();
        File sourceParent = sourceFile.getParentFile();
        String assetPath;
        
        if (sourceParent == null) {
            assetPath = "";
        } else {
            assetPath = sourceParent.getAbsolutePath();
            assetPath = FilenameUtils.separatorsToUnix(assetPath) + "/";
        }

        // fix path for all assets with .sharedassets extension
        boolean changed = false;
        for (AssetRef ref : asset.getReferences()) {
            File refFile = new File(ref.filePath);
            String refExt = FilenameUtils.getExtension(refFile.getName());
            if (refExt.endsWith("assets") && !refFile.exists()) {
                String filePathOld = ref.filePath;
                String filePathNew = assetPath + FilenameUtils.getName(ref.filePath);
                File refFileNew = new File(filePathNew);
                
                if (refFileNew.exists()) {
                    L.log(Level.FINE, "Fixed reference: {0} -> {1}", new Object[]{filePathOld, filePathNew});
                    ref.filePath = filePathNew;
                    changed = true;
                } else {
                    L.log(Level.FINE, "Fixed reference not found: {0}", refFileNew);
                }
            }
        }

        if (changed) {
            // create backup first
            try {
                File backupFile = new File(sourceFile.getPath() + ".bak");
                FileUtils.copyFile(sourceFile, backupFile);
            } catch (IOException ex) {
                // backup is mandatory, don't risk any loss of data 
                throw new IOException("Can't create backup copy", ex);
            }
            
            asset.save(sourceFile);
        } else {
            L.fine("No references changed, skipping saving");
        }
    }
    
    public void printStats(PrintStream ps) {
        AssetObjectPathTable pathTable = asset.getObjectPaths();
        Map<String, Integer> classCounts = new HashMap<>();
        Map<String, Integer> classSizes = new HashMap<>();
        
        for (AssetObjectPath path : pathTable) {
            String className = ClassID.getNameForID(path.classID2, true);
            
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
    
    public void list(PrintStream out) {
        AssetObjectPathTable pathTable = asset.getObjectPaths();
        Deserializer deser = new Deserializer(asset);
        
        // dirty hardcoded table printer
        int p1 = 12;
        int p2 = 4;
        int p3 = 24;
        int p4 = 10;
        int p5 = 10;
        int p6 = 11;
        
        System.out.print(StringUtils.rightPad("PID", p1));
        System.out.print(" | ");
        System.out.print(StringUtils.rightPad("CID", p2));
        System.out.print(" | ");
        System.out.print(StringUtils.rightPad("Class name", p3));
        System.out.print(" | ");
        System.out.print(StringUtils.rightPad("Offset", p4));
        System.out.print(" | ");
        System.out.print(StringUtils.leftPad("Length", p5));
        System.out.print(" | ");
        System.out.print(StringUtils.rightPad("Object name", p6));
        System.out.println();
        
        System.out.print(StringUtils.repeat("-", p1));
        System.out.print(" | ");
        System.out.print(StringUtils.repeat("-", p2));
        System.out.print(" | ");
        System.out.print(StringUtils.repeat("-", p3));
        System.out.print(" | ");
        System.out.print(StringUtils.repeat("-", p4));
        System.out.print(" | ");
        System.out.print(StringUtils.repeat("-", p5));
        System.out.print(" | ");
        System.out.print(StringUtils.repeat("-", p6));
        System.out.println();
        
        for (AssetObjectPath path : pathTable) {
            String name;
            
            try {
                UnityObject obj = deser.deserialize(path);
                name = obj.getValue("m_Name");
                if (name == null) {
                    name = "";
                }
            } catch (Exception ex) {
                // safety not guaranteed
                name = "<error>";
            }
            
            System.out.print(StringUtils.rightPad(String.valueOf(path.pathID), p1));
            System.out.print(" | ");
            System.out.print(StringUtils.rightPad(String.valueOf(path.classID2), p2));
            System.out.print(" | ");
            System.out.print(StringUtils.rightPad(ClassID.getNameForID(path.classID2, true), p3));
            System.out.print(" | ");
            System.out.print(StringUtils.rightPad(String.format("0x%x", path.offset), p4));
            System.out.print(" | ");
            System.out.print(StringUtils.leftPad(String.valueOf(path.length), p5));
            System.out.print(" | ");
            System.out.print(name);
            System.out.println();
        }
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
