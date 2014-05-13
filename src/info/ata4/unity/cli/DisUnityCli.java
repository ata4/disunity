/*
 ** 2013 July 05
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import info.ata4.log.LogUtils;
import info.ata4.unity.DisUnity;
import info.ata4.unity.cli.classfilter.SimpleClassFilter;
import info.ata4.unity.util.ClassID;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 * DisUnity command line interface.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DisUnityCli implements Runnable {
    
    private static final Logger L = LogUtils.getLogger();
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LogUtils.configure();
        
        L.log(Level.INFO, DisUnity.getSignature());
        
        try {
            DisUnityCli cli = new DisUnityCli();
            
            JCommander cmd = new JCommander();
            cmd.setProgramName(DisUnity.getProgramName());
            cmd.addObject(cli);
            cmd.parse(args);
            
            if (cli.showUsage()) {
                cmd.usage();
            } else {
                cli.run();
            }
        } catch (Throwable t) {
            L.log(Level.SEVERE, "Fatal error", t);
        }
    }
    
    @Parameter(
        names = { "-v", "--verbose" },
        description = "Show more verbose log output."
    )
    private boolean verbose;
    
    @Parameter(
        names = { "-f", "--include" },
        description = "Only process objects that use these classes. Expects a string with class names or IDs, separated by commas."
    )
    private String classListInclude;
    
    @Parameter(
        names = { "-x", "--exclude" },
        description = "Exclude objects from processing that use these classes. Expects a string with class names or IDs, separated by commas."
    )
    private String classListExclude;
    
    @Parameter(
        description = "<command> <files>"
    )
    private List<String> remaining;
    
    public boolean showUsage() {
        return remaining == null || remaining.size() < 2;
    }

    @Override
    public void run() {
        // convert unnamed argument list to deque
        Deque<String> args = new ArrayDeque<>(remaining);
        
        DisUnityOptions ops = new DisUnityOptions();
        
        // set command
        ops.setCommand(args.pollFirst());
        
        // add files
        for (String path : args) {
            ops.getFiles().add(Paths.get(path));
        }
        
        // set class filter lists
        if (classListInclude != null || classListExclude != null) {
            SimpleClassFilter classFilter = new SimpleClassFilter();
            parseClassList(classFilter.getAcceptedIDs(), classListInclude);
            parseClassList(classFilter.getAcceptedIDs(), classListExclude);
            ops.setClassFilter(classFilter);
        }
        
        // increase logging level if requested
        if (verbose) {
            LogUtils.configure(Level.ALL);
        }
        
        // run processor
        DisUnityProcessor processor = new DisUnityProcessor(ops);
        processor.run();
    }
    
    private void parseClassList(Set<Integer> classIDList, String classListString) {
        if (StringUtils.isEmpty(classListString)) {
            return;
        }
        
        String[] values = StringUtils.split(classListString, ",");
        for (String className : values) {
            Integer classID;

            try {
                classID = Integer.parseInt(className);
            } catch (NumberFormatException e) {
                classID = ClassID.getIDForName(className, true);
            }

            if (classID == null) {
                L.log(Level.WARNING, "Invalid class name or ID for filter: {0}", className);
                continue;
            }

            classIDList.add(classID);
        }
    }
}
