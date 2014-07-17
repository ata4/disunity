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
import com.beust.jcommander.ParameterException;
import info.ata4.log.LogUtils;
import info.ata4.unity.DisUnity;
import info.ata4.unity.cli.cmd.BundleExtractCmd;
import info.ata4.unity.cli.cmd.BundleInjectCmd;
import info.ata4.unity.cli.cmd.BundleListCmd;
import info.ata4.unity.cli.cmd.Command;
import info.ata4.unity.cli.cmd.DebugDeserializerCmd;
import info.ata4.unity.cli.cmd.DebugStructDBCmd;
import info.ata4.unity.cli.cmd.DumpCmd;
import info.ata4.unity.cli.cmd.ExtractCmd;
import info.ata4.unity.cli.cmd.InfoCmd;
import info.ata4.unity.cli.cmd.LearnCmd;
import info.ata4.unity.cli.cmd.ListCmd;
import info.ata4.unity.cli.cmd.SplitCmd;
import info.ata4.unity.cli.cmd.StatsCmd;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DisUnity command line interface.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DisUnityCli implements Runnable {
    
    private static final Logger L = LogUtils.getLogger();
    
    private final DisUnityOptions opts = new DisUnityOptions();
    private final JCommander jc = new JCommander();
    
    public DisUnityCli() {
        jc.setProgramName(DisUnity.getProgramName());
        jc.addObject(opts);

        PrintStream out = System.out;
        
        // asset commands
        jc.addCommand("dump", new DumpCmd().setDumpToFiles(false));
        jc.addCommand("dump-struct", new DumpCmd().setDumpToFiles(false).setDumpStructs(true));
        jc.addCommand("extract", new ExtractCmd());
        jc.addCommand("extract-raw", new ExtractCmd().setRaw(true));
        jc.addCommand("extract-txt", new DumpCmd());
        jc.addCommand("extract-struct", new DumpCmd().setDumpStructs(true));
        jc.addCommand("info", new InfoCmd(out));
        jc.addCommand("info-stats", new StatsCmd(out));
        jc.addCommand("learn", new LearnCmd());
        jc.addCommand("list", new ListCmd(out));
        jc.addCommand("split", new SplitCmd());
        
        // bundle commands
        jc.addCommand("bundle-extract", new BundleExtractCmd());
        jc.addCommand("bundle-inject", new BundleInjectCmd());
        jc.addCommand("bundle-list", new BundleListCmd(out));
        
        // debug commands
        jc.addCommand("debug-deserializer", new DebugDeserializerCmd());
        jc.addCommand("debug-structdb", new DebugStructDBCmd());
    }
    
    public void parse(String[] args) {
        L.info(DisUnity.getSignature());
        
        jc.parse(args);
        
        // display usage
        if (opts.isHelp()) {
            jc.usage();
        }
        
        // increase logging level if requested
        if (opts.isVerbose()) {
            LogUtils.configure(Level.ALL);
        }
    }

    @Override
    public void run() {
        String cmdName = jc.getParsedCommand();
        if (cmdName == null) {
            return;
        }
        
        JCommander jcc = jc.getCommands().get(cmdName);
        
        Command cmd = (Command) jcc.getObjects().get(0);
        cmd.setOptions(opts);
        cmd.run();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LogUtils.configure();
        
        DisUnityCli cli = new DisUnityCli();
        
        try {
            cli.parse(args);
            cli.run();
        } catch (ParameterException ex) {
            L.log(Level.WARNING, "Parameter error: {0}", ex.getMessage());
        } catch (Throwable t) {
            L.log(Level.SEVERE, "Fatal error", t);
        }
    }
}
