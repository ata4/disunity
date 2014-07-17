/*
 ** 2014 Juli 17
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.cmd;

import com.beust.jcommander.Parameters;
import info.ata4.log.LogUtils;
import info.ata4.unity.asset.struct.TypeField;
import info.ata4.unity.serdes.db.FieldTypeMap;
import info.ata4.unity.serdes.db.StructDatabase;
import info.ata4.unity.util.ClassID;
import info.ata4.unity.util.UnityVersion;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Utility command to print structure database stats.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandNames = "debug-structdb",
    commandDescription = "Shows structure database stats."
)
public class DebugStructDBCmd extends Command {
    
    private static final Logger L = LogUtils.getLogger();

    @Override
    public void run() {
        FieldTypeMap ftm = StructDatabase.getInstance().getFieldTypeMap();
        Set<TypeField> fieldNodes = new HashSet<>();
        Set<UnityVersion> versions = new TreeSet<>();
        Set<Integer> classIDs = new TreeSet<>();

        for (Map.Entry<Pair<Integer, UnityVersion>, TypeField> entry : ftm.entrySet()) {
            versions.add(entry.getKey().getRight());
            classIDs.add(entry.getKey().getLeft());
            fieldNodes.add(entry.getValue());
        }
        
        L.log(Level.INFO, "Class IDs: {0}", classIDs.size());
        L.log(Level.INFO, "Versions: {0}", versions.size());
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
        
        for (UnityVersion rev : versions) {
            System.out.print(rev);
            System.out.print(" |");
            
            for (Integer classID : classIDs) {
                System.out.print("  ");
                if (ftm.containsKey(new ImmutablePair(classID, rev))) {
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
