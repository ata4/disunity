/*
 ** 2014 July 17
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
import info.ata4.unity.asset.struct.ObjectPath;
import info.ata4.unity.serdes.Deserializer;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandNames = "debug-deserializer",
    commandDescription = "Performs an asset deserialization test."
)
public class DebugDeserializerCmd extends AssetCommand {
    
    private static final Logger L = LogUtils.getLogger();
    
    private int objTested;
    private int objFailed;

    @Override
    protected void processAsset(AssetFile asset) throws IOException {
        Deserializer deser = new Deserializer(asset);
        
        int objTestedAsset = 0;

        for (ObjectPath path : asset.getPaths()) {
            // skip MonoBehaviours
            if (path.isScript()) {
                continue;
            }

            try {
                deser.deserialize(path);
            } catch (Exception ex) {
                L.log(Level.INFO, "Deserialization failed for " + path, ex);
                objFailed++;

                if (getOptions().isVerbose()) {
                    // try again in debug mode
                    deser.setDebug(true);
                    try {
                        deser.deserialize(path);
                    } catch (Exception ex2) {
                    }
                    deser.setDebug(false);
                }
            }

            objTestedAsset++;
            objTested++;
        }
        
        L.log(Level.INFO, "Tested objects: {0}", objTestedAsset);
    }

    @Override
    protected void processEnd() {
        L.log(Level.INFO, "Total tested objects: {0}", objTested);
        if (objFailed == 0) {
            L.log(Level.INFO, "All objects successfully deserialized!");
        } else {
            L.log(Level.INFO, "Failed deserializations: {0}", objFailed);
        }
    }
}
