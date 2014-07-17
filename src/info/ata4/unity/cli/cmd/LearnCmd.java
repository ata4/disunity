/*
 ** 2014 January 08
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.cmd;

import com.beust.jcommander.Parameters;
import info.ata4.log.LogUtils;
import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.serdes.db.StructDatabase;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandNames = "learn",
    commandDescription = "Copies new structure information to database."
)
public class LearnCmd extends AssetCommand {

    private static final Logger L = LogUtils.getLogger();
    
    @Override
    public void processAsset(AssetFile asset) throws IOException {
        int learned = StructDatabase.getInstance().learn(asset);
        if (learned > 0) {
            L.log(Level.INFO, "Learned {0} new structs", learned);
        } else {
            L.log(Level.INFO, "Nothing new learned");
        }
    }

    @Override
    public void processEnd() {
        StructDatabase.getInstance().update();
    }
}
