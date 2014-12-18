/*
 ** 2013 July 05
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import info.ata4.disunity.cli.command.BundleBuildCommand;
import info.ata4.disunity.cli.command.BundleExtractCommand;
import info.ata4.disunity.cli.command.BundleInfoCommand;
import info.ata4.disunity.cli.command.BundleListCommand;
import info.ata4.disunity.cli.command.Command;
import info.ata4.disunity.cli.command.DebugAssetTest;
import info.ata4.disunity.cli.command.DebugBundleCopy;
import info.ata4.disunity.cli.command.DebugBundleMove;
import info.ata4.disunity.cli.command.GuiCommand;
import info.ata4.disunity.cli.command.LearnCommand;
import info.ata4.log.LogUtils;
import info.ata4.unity.DisUnity;
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
    private static final boolean DEBUG = true;
    
    private final DisUnityOptions opts = new DisUnityOptions();
    private final JCommander jc = new JCommander();
    
    public DisUnityCli() {
        jc.setProgramName(DisUnity.getProgramName());
        jc.addObject(opts);

        PrintStream out = System.out;
        
//        // asset commands
//        jc.addCommand(new DumpCmd());
//        jc.addCommand(new DumpStructCmd());
//        jc.addCommand(new ExtractCmd());
//        jc.addCommand(new ExtractRawCmd());
//        jc.addCommand(new ExtractTxtCmd());
//        jc.addCommand(new ExtractStructCmd());
//        jc.addCommand(new InfoCmd(out));
//        jc.addCommand(new StatsCmd(out));
        jc.addCommand(new LearnCommand());
//        jc.addCommand(new ListCmd(out));
//        jc.addCommand(new SplitCmd());
//        
//        // bundle commands
        jc.addCommand(new BundleExtractCommand());
        jc.addCommand(new BundleBuildCommand());
        jc.addCommand(new BundleListCommand(out));
        jc.addCommand(new BundleInfoCommand(out));
        
        // other commands
        jc.addCommand(new GuiCommand());
        
        // debug commands
        if (DEBUG) {
            jc.addCommand(new DebugAssetTest());
            jc.addCommand(new DebugBundleMove());
            jc.addCommand(new DebugBundleCopy());
        }
    }
    
    public void parse(String[] args) {
        jc.parse(args);
        
        // display usage
        if (opts.isHelp()) {
            jc.usage();
        }
        
        // increase logging level if requested
        if (opts.isVerbose()) {
            LogUtils.configure(Level.ALL);
        }
        
        // only print warnings and errors to stderr if the stdout format is not
        // plain text
        if (opts.getOutputFormat() != OutputFormat.PLAINTEXT) {
            LogUtils.configure(Level.WARNING);
        }
        
        L.info(DisUnity.getSignature());
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
