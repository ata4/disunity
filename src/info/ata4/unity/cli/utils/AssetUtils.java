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
import info.ata4.unity.asset.struct.AssetRef;
import info.ata4.unity.asset.struct.AssetClassType;
import info.ata4.unity.serdes.Deserializer;
import info.ata4.unity.serdes.UnityObject;
import info.ata4.unity.serdes.db.StructDatabase;
import info.ata4.unity.util.ClassID;
import info.ata4.util.collection.MapUtils;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;
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
        List<AssetObjectPath> paths = asset.getPaths();
        List<AssetRef> refTable = asset.getReferences();
        AssetHeader header = asset.getHeader();
        AssetClassType fieldTree = asset.getTypeTree();
        
        ps.println("Header");
        ps.println("  File size: " + humanReadableByteCount(header.getFileSize(), true));
        ps.println("  Tree size: " + humanReadableByteCount(header.getTreeSize(), true));
        ps.println("  Format: " + header.getFormat());
        ps.println("  Data offset: " + header.getDataOffset());
        ps.println();
        
        ps.println("Serialized data");
        ps.println("  Revision: " + fieldTree.getRevision());
        ps.println("  Version: " + fieldTree.getVersion());
        ps.println("  Standalone: " + (fieldTree.isStandalone() ? "yes" : "no"));
        ps.println("  Objects: " + paths.size());
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
        Path sourceFile = asset.getSourceFile();
        Path sourceParent = sourceFile.getParent();
        String assetPath;
        
        if (sourceParent == null) {
            assetPath = "";
        } else {
            assetPath = sourceParent.toAbsolutePath().toString();
            assetPath = FilenameUtils.separatorsToUnix(assetPath) + "/";
        }

        // fix path for all assets with .sharedassets extension
        boolean changed = false;
        for (AssetRef ref : asset.getReferences()) {
            Path refFile = Paths.get(ref.filePath);
            String refExt = FilenameUtils.getExtension(refFile.getFileName().toString());
            if (refExt.endsWith("assets") && !Files.exists(refFile)) {
                String filePathOld = ref.filePath;
                String filePathNew = assetPath + FilenameUtils.getName(ref.filePath);
                Path refFileNew = Paths.get(filePathNew);
                
                if (Files.exists(refFileNew)) {
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
                String backupFileName = sourceFile.getFileName().toString() + ".bak";
                Path backupFile = sourceFile.resolveSibling(backupFileName);
                Files.copy(sourceFile, backupFile);
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
        List<AssetObjectPath> paths = asset.getPaths();
        Map<String, Integer> classCounts = new HashMap<>();
        Map<String, Integer> classSizes = new HashMap<>();
        
        for (AssetObjectPath path : paths) {
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
    
    public void list(PrintStream ps) {
        List<AssetObjectPath> paths = asset.getPaths();
        Deserializer deser = new Deserializer(asset);
        
        // dirty hardcoded table printer
        int p1 = 12;
        int p2 = 4;
        int p3 = 24;
        int p4 = 10;
        int p5 = 10;
        int p6 = 11;
        
        ps.print(StringUtils.rightPad("PID", p1));
        ps.print(" | ");
        ps.print(StringUtils.rightPad("CID", p2));
        ps.print(" | ");
        ps.print(StringUtils.rightPad("Class name", p3));
        ps.print(" | ");
        ps.print(StringUtils.rightPad("Offset", p4));
        ps.print(" | ");
        ps.print(StringUtils.leftPad("Length", p5));
        ps.print(" | ");
        ps.print(StringUtils.rightPad("Object name", p6));
        ps.println();
        
        ps.print(StringUtils.repeat("-", p1));
        ps.print(" | ");
        ps.print(StringUtils.repeat("-", p2));
        ps.print(" | ");
        ps.print(StringUtils.repeat("-", p3));
        ps.print(" | ");
        ps.print(StringUtils.repeat("-", p4));
        ps.print(" | ");
        ps.print(StringUtils.repeat("-", p5));
        ps.print(" | ");
        ps.print(StringUtils.repeat("-", p6));
        ps.println();
        
        for (AssetObjectPath path : paths) {
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
            
            ps.print(StringUtils.rightPad(String.valueOf(path.pathID), p1));
            ps.print(" | ");
            ps.print(StringUtils.rightPad(String.valueOf(path.classID2), p2));
            ps.print(" | ");
            ps.print(StringUtils.rightPad(ClassID.getNameForID(path.classID2, true), p3));
            ps.print(" | ");
            ps.print(StringUtils.rightPad(String.format("0x%x", path.offset), p4));
            ps.print(" | ");
            ps.print(StringUtils.leftPad(String.valueOf(path.length), p5));
            ps.print(" | ");
            ps.print(name);
            ps.println();
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
