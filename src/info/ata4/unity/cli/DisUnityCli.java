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
import info.ata4.unity.cli.cmd.DumpStructCmd;
import info.ata4.unity.cli.cmd.ExtractCmd;
import info.ata4.unity.cli.cmd.ExtractRawCmd;
import info.ata4.unity.cli.cmd.ExtractStructCmd;
import info.ata4.unity.cli.cmd.ExtractTxtCmd;
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
        jc.addCommand(new DumpCmd());
        jc.addCommand(new DumpStructCmd());
        jc.addCommand(new ExtractCmd());
        jc.addCommand(new ExtractRawCmd());
        jc.addCommand(new ExtractTxtCmd());
        jc.addCommand(new ExtractStructCmd());
        jc.addCommand(new InfoCmd(out));
        jc.addCommand(new StatsCmd(out));
        jc.addCommand(new LearnCmd());
        jc.addCommand(new ListCmd(out));
        jc.addCommand(new SplitCmd());
        
        // bundle commands
        jc.addCommand(new BundleExtractCmd());
        jc.addCommand(new BundleInjectCmd());
        jc.addCommand(new BundleListCmd(out));
        
        // debug commands
        jc.addCommand(new DebugDeserializerCmd());
        jc.addCommand(new DebugStructDBCmd());
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
        if (opts.isHelp()) {
            return;
        }
        
        String cmdName = jc.getParsedCommand();
        if (cmdName == null) {
            jc.usage();
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
