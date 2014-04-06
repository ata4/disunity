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

import info.ata4.log.LogUtils;
import info.ata4.unity.DisUnity;
import info.ata4.unity.cli.classfilter.SimpleClassFilter;
import info.ata4.unity.util.ClassID;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * DisUnity command line interface.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DisUnityCli {
    
    private static final Logger L = LogUtils.getLogger();
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LogUtils.configure();
        
        L.log(Level.INFO, "DisUnity v{0}", DisUnity.getVersion());
        
        try {
            DisUnityProcessor disunity = new DisUnityProcessor();
            if (configure(disunity.getSettings(), args)) {
                disunity.run();
            }
        } catch (Throwable t) {
            L.log(Level.SEVERE, "Fatal error", t);
        }
    }

    public static boolean configure(DisUnitySettings settings, String[] args) {
        Options opts = new Options();

        Option optClassFilter = new Option("f", null);
        optClassFilter.setDescription("Only process objects that use these classes. Expects a string with class names, separated by commas.");
        optClassFilter.setArgs(1);
        optClassFilter.setArgName("classes");
        opts.addOption(optClassFilter);
        
        Option optVerbose = new Option("v", "verbose", false, "Show more verbose log output.");
        opts.addOption(optVerbose);

        try {
            if (args.length < 2) {
                printUsage(opts);
                return false;
            }
            
            CommandLineParser parser = new PosixParser();
            CommandLine cl = parser.parse(opts, args);
            
            if (cl.hasOption(optClassFilter.getOpt())) {
                String value = cl.getOptionValue(optClassFilter.getOpt());
                String[] valueSplit = value.split(",");
                
                SimpleClassFilter classFilter = new SimpleClassFilter();

                for (String className : valueSplit) {
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
                    
                    classFilter.getAcceptedIDs().add(classID);
                }
                
                settings.setClassFilter(classFilter);
            }
            
            if (cl.hasOption(optVerbose.getOpt())) {
                LogUtils.configure(Level.ALL);
            }
            
            Deque<String> leftArgs = new ArrayDeque(cl.getArgList());
            
            // first argument is the command
            settings.setCommand(leftArgs.pop());
            
            // add remaining arguments as files
            for (String leftArg : leftArgs) {
                settings.getFiles().add(Paths.get(leftArg));
            }
        } catch (ParseException ex) {
            L.severe(ex.getMessage());
            return false;
        }
        
        return true;
    }
    
    /**
     * Prints application usage.
     */
    private static void printUsage(Options opts) {
        HelpFormatter clHelp = new HelpFormatter();
        clHelp.setWidth(100);
        clHelp.printHelp("disunity <options> [command] [file]...", opts);
        System.out.println();
        System.out.println("Available commands:");
        Set<String> cmds = new TreeSet<>(DisUnityProcessor.getCommands());
        for (String cmd : cmds) {
            System.out.println(" " + cmd);
        }
    }
}
