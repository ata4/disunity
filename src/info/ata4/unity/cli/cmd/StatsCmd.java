/*
 ** 2014 January 09
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.cmd;

import com.beust.jcommander.Parameters;
import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.asset.struct.ObjectPath;
import info.ata4.unity.util.ClassID;
import info.ata4.util.collection.MapUtils;
import info.ata4.util.string.StringUtils;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandNames = "info-stats",
    commandDescription = "Shows class usage stats."
)
public class StatsCmd extends AssetCommand {
    
    private final PrintStream ps;
    
    public StatsCmd(PrintStream ps) {
        this.ps = ps;
    }

    @Override
    protected void processAsset(AssetFile asset) throws IOException {
        List<ObjectPath> paths = asset.getPaths();
        Map<String, Integer> classCounts = new HashMap<>();
        Map<String, Integer> classSizes = new HashMap<>();
        
        for (ObjectPath path : paths) {
            String className = ClassID.getNameForID(path.getClassID(), true);
            
            if (!classCounts.containsKey(className)) {
                classCounts.put(className, 0);
                classSizes.put(className, 0);
            }
            
            classCounts.put(className, classCounts.get(className) + 1);
            classSizes.put(className, classSizes.get(className) + path.getLength());
        }
        
        ps.println("Classes by object instances:");
        Map<String, Integer> classCountsSorted = MapUtils.sortByValue(classCounts, true);
        for (Map.Entry<String, Integer> entry : classCountsSorted.entrySet()) {
            String className = entry.getKey();
            int classCount = entry.getValue();
            ps.printf("  %s: %d\n", className, classCount);
        }

        ps.println("Classes by object data size:");
        Map<String, Integer> classSizesSorted = MapUtils.sortByValue(classSizes, true);
        for (Map.Entry<String, Integer> entry : classSizesSorted.entrySet()) {
            String className = entry.getKey();
            String classSize = StringUtils.humanReadableByteCount(entry.getValue(), true);
            ps.printf("  %s: %s\n", className, classSize);
        }
        ps.println();
    }
}
