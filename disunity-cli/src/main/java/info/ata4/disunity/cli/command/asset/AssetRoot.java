/*
 ** 2015 December 01
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.command.asset;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;
import info.ata4.disunity.cli.command.Command;
import java.io.PrintWriter;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters
public class AssetRoot extends Command {

    @Override
    public void init(JCommander commander, PrintWriter out) {
        super.init(commander, out);

        addSubCommand("blocks", new AssetBlocks());
        addSubCommand("externals", new AssetExternalRefs());
        addSubCommand("header", new AssetHeader());
        addSubCommand("objectids", new AssetObjectIDs());
        addSubCommand("objects", new AssetObjects());
        addSubCommand("types", new AssetTypes());
        addSubCommand("unpack", new AssetUnpack());
    }
}
