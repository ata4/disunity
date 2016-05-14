/*
 ** 2015 November 30
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.command;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import info.ata4.disunity.cli.command.asset.AssetRoot;
import info.ata4.disunity.cli.command.bundle.BundleRoot;
import info.ata4.log.LogUtils;
import java.io.PrintWriter;
import java.util.logging.Level;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters
public class DisUnityRoot extends Command {

    @Parameter(
        names = {"-h", "--help"},
        description = "Print this help.",
        help = true
    )
    private boolean help;

    @Parameter(
        names = { "-v", "--verbose" },
        description = "Show more verbose log output."
    )
    private boolean verbose;

    @Override
    public void init(JCommander commander, PrintWriter out) {
        super.init(commander, out);

        addSubCommand("bundle", new BundleRoot());
        addSubCommand("asset", new AssetRoot());
    }

    @Override
    public void run() {
        // increase logging level if requested
        if (verbose) {
            LogUtils.configure(Level.ALL);
        }

        // display usage
        if (help) {
            usage();
            return;
        }

        super.run();
    }
}
