/*
 ** 2015 November 30
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.command.bundle;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;
import info.ata4.disunity.cli.command.Command;
import java.io.PrintWriter;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters
public class BundleRoot extends Command {

    @Override
    public void init(JCommander commander, PrintWriter out) {
        super.init(commander, out);

        addSubCommand("list", new BundleList());
        addSubCommand("info", new BundleInfo());
        addSubCommand("pack", new BundlePack());
        addSubCommand("unpack", new BundleUnpack());
    }
}
