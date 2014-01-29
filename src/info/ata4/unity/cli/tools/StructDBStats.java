/*
 ** 2013 December 06
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.tools;

import info.ata4.log.LogUtils;
import info.ata4.unity.asset.struct.AssetFieldType;
import info.ata4.unity.serdes.db.FieldTypeMap;
import info.ata4.unity.serdes.db.StructDatabase;
import info.ata4.unity.util.ClassID;
import info.ata4.util.collection.Pair;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility program to print structure database stats.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class StructDBStats {
    
    private static final Logger L = Logger.getLogger(StructDBStats.class.getName());
    
    public static void main(String[] args) {
        LogUtils.configure();
        
        FieldTypeMap ftm = StructDatabase.getInstance().getFieldTypeMap();
        Set<AssetFieldType> fieldNodes = new HashSet<>();
        Set<String> revs = new TreeSet<>();
        Set<Integer> classIDs = new TreeSet<>();

        for (Map.Entry<Pair<Integer, String>, AssetFieldType> entry : ftm.entrySet()) {
            revs.add(entry.getKey().getRight());
            classIDs.add(entry.getKey().getLeft());
            fieldNodes.add(entry.getValue());
        }
        
        L.log(Level.INFO, "Class IDs: {0}", classIDs.size());
        L.log(Level.INFO, "Revisions: {0}", revs.size());
        L.log(Level.INFO, "Fields: {0}", fieldNodes.size());
        
        System.out.println();
        System.out.print("        |");
        
        for (Integer classID : classIDs) {
            System.out.print(StringUtils.leftPad(String.valueOf(classID), 4));
            System.out.print(" |");
        }
        
        System.out.println();
        System.out.print("--------|");
        System.out.print(StringUtils.repeat("-----|", classIDs.size()));
        System.out.println();
        
        for (String rev : revs) {
            System.out.print(rev);
            System.out.print(" |");
            
            for (Integer classID : classIDs) {
                System.out.print("  ");
                if (ftm.containsKey(new Pair(classID, rev))) {
                    System.out.print("x");
                } else {
                    System.out.print(" ");
                }
                System.out.print("  |");
            }
            
            System.out.println();
        }
        
        System.out.println();

        for (Integer classID : classIDs) {
            String className = ClassID.getNameForID(classID);
            String classIDStr = StringUtils.rightPad(String.valueOf(classID), 3);
            if (className == null) {
                System.out.printf("%s : ???\n", classIDStr);
            } else {
                System.out.printf("%s : %s\n", classIDStr, className);
            }
        }
    }
}
