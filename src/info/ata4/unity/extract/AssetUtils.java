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

import info.ata4.unity.struct.db.StructDatabase;
import info.ata4.unity.asset.Asset;
import info.ata4.unity.struct.AssetHeader;
import info.ata4.unity.struct.ExternalReference;
import info.ata4.unity.struct.ExternalReferenceTable;
import info.ata4.unity.struct.ObjectPath;
import info.ata4.unity.struct.ObjectPathTable;
import info.ata4.unity.struct.TypeTree;
import info.ata4.unity.struct.db.ClassID;
import info.ata4.util.collection.MapUtils;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;

/**
 * Helper class to output information about an asset file.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetUtils {
    
    private static final Logger L = Logger.getLogger(AssetUtils.class.getName());
    
    private final Asset asset;

    public AssetUtils(Asset asset) {
        this.asset = asset;
    }
    
    public void learnStruct() {                
        int learned = StructDatabase.getInstance().learn(asset);
        if (learned > 0) {
            L.log(Level.INFO, "New structs: {0}", learned);
        }
    }

    public void printInfo(PrintStream ps) {
        ObjectPathTable objTable = asset.getObjectPaths();
        ExternalReferenceTable refTable = asset.getExternalRefs();
        AssetHeader header = asset.getHeader();
        TypeTree fieldTree = asset.getTypeTree();
        
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
            for (ExternalReference ref : refTable) {
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
    
    public void printStats(PrintStream ps) {
        ObjectPathTable pathTable = asset.getObjectPaths();
        Map<String, Integer> classCounts = new HashMap<>();
        Map<String, Integer> classSizes = new HashMap<>();
        
        for (ObjectPath path : pathTable) {
            String className = ClassID.getInstance().getNameForID(path.classID2, true);
            
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
