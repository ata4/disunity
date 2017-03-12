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
import info.ata4.disunity.DisUnity;
import info.ata4.disunity.cli.command.DisUnityRoot;
import info.ata4.log.LogUtils;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

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

        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out), true)) {
            JCommander jc = new JCommander();
            DisUnityRoot root = new DisUnityRoot();
            root.init(jc, out);

            jc.setProgramName(DisUnity.getProgramName());
            jc.addObject(root);
            jc.parse(args);

            root.run();
        } catch (ParameterException ex) {
            L.log(Level.WARNING, "Parameter error: {0}", ex.getMessage());
        } catch (Throwable t) {
            L.log(Level.SEVERE, "Fatal error", t);
        }
    }
}
