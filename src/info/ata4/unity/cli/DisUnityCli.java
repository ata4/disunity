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

import info.ata4.unity.DisUnity;
import info.ata4.unity.DisUnity.Command;
import info.ata4.unity.util.ClassID;
import info.ata4.util.log.LogUtils;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
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
    
    private static final Logger L = Logger.getLogger(DisUnityCli.class.getName());
    
    private final DisUnity disunity;
    private Options opts;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LogUtils.configure();
        
        System.out.println("DisUnity " + DisUnity.VERSION);
        
        try {
            DisUnity disunity = new DisUnity();
            DisUnityCli cli = new DisUnityCli(disunity);
            if (cli.configure(args)) {
                disunity.run();
            }
        } catch (Throwable t) {
            L.log(Level.SEVERE, "Fatal error", t);
        }
    }

    public DisUnityCli(DisUnity disunity) {
        this.disunity = disunity;
    }

    public boolean configure(String[] args) {
        opts = new Options();
        
        Option optHelp = new Option("h", "help", false, "Print this help.");
        opts.addOption(optHelp);
        
        Option optCmd = new Option("c", null, true, null);
        optCmd.setDescription("Processing command:\n"
                + Command.EXTRACT + "  - Extracts the converted data from asset files or asset bundles.\n"
                + Command.RAW + "      - Extracts the raw object data from asset files or asset bundles.\n"
                + Command.UNBUNDLE + " - Extracts an Unity asset bundle.\n"
                + Command.STRUCT + "   - Outputs the data structure of asset files into text files.\n"
                + Command.FIXREFS + "  - Fixes asset references of unpacked .unity scene files so they can be loaded with the Unity editor.\n"
                + Command.INFO + "     - Outputs some information about the asset files.");
        optCmd.setArgs(1);
        optCmd.setArgName("cmd");
        opts.addOption(optCmd);
        
        Option optClassFilter = new Option("f", null);
        optClassFilter.setDescription("Only process objects that use these classes. Expects a string with class names, separated by commas.");
        optClassFilter.setLongOpt("filter-class");
        optClassFilter.setArgs(1);
        optClassFilter.setArgName("classes");
        opts.addOption(optClassFilter);
        
        Option optVerbose = new Option("v", "verbose", false, "Show more verbose log output.");
        opts.addOption(optVerbose);

        try {
            if (args.length == 0) {
                printUsage();
                return false;
            }
            
            CommandLineParser parser = new PosixParser();
            CommandLine cl = parser.parse(opts, args);
            
            if (cl.hasOption(optHelp.getOpt())) {
                printUsage();
                return false;
            }
            
            if (cl.hasOption(optCmd.getOpt())) {
                String value = cl.getOptionValue(optCmd.getOpt()).toUpperCase();
                try {
                    Command cmd = Command.valueOf(value);
                    disunity.setCommand(cmd);
                } catch (IllegalArgumentException ex) {
                    L.log(Level.SEVERE, "Invalid command: {0}", value);
                    printUsage();
                    return false;
                }
            }
            
            if (cl.hasOption(optClassFilter.getOpt())) {
                String value = cl.getOptionValue(optClassFilter.getOpt());
                String[] valueSplit = value.split(",");
                Set<Integer> classFilter = new HashSet<>();
                
                for (String className : valueSplit) {
                    Integer classID;
                    try {
                        classID = Integer.parseInt(className);
                    } catch (NumberFormatException e) {
                        classID = ClassID.getIDForName(className);
                        
                        if (classID == null) {
                            L.log(Level.WARNING, "Invalid class name or ID for filter: {0}", className);
                            continue;
                        }
                    }
                    classFilter.add(classID);
                }
                
                disunity.setClassFilter(classFilter);
            }
            
            if (cl.hasOption(optVerbose.getOpt())) {
                LogUtils.configure(Level.ALL);
            }
            
            // add remaining arguments as files
            for (String leftArg : cl.getArgs()) {
                disunity.getFiles().add(new File(leftArg));
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
    private void printUsage() {
        HelpFormatter clHelp = new HelpFormatter();
        clHelp.setWidth(100);
        clHelp.printHelp("disunity <options> [file]...", opts);
    }
}
