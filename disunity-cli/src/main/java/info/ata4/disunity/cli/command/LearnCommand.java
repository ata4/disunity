/*
 ** 2014 November 28
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.command;

import com.beust.jcommander.Parameters;
import info.ata4.log.LogUtils;
import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.util.TypeTreeUtils;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandNames = "learn",
    commandDescription = "Copies new run-time type information to the local database."
)
public class LearnCommand extends AssetFileCommand {
    
    private static final Logger L = LogUtils.getLogger();
    
    private int learned = 0;

    @Override
    public void handleAssetFile(AssetFile asset) {
        learned += TypeTreeUtils.learnTypes(asset);
    }

    @Override
    public void run() {
        super.run();
        if (learned > 0) {
            L.log(Level.INFO, "Adding {0} new type(s) to database", learned);
            TypeTreeUtils.getDatabase().save();
        }
    }
}
