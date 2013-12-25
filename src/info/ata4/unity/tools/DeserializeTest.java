/*
 ** 2013 August 11
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.tools;

import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.asset.struct.AssetObjectPath;
import info.ata4.unity.serdes.Deserializer;
import info.ata4.unity.util.ClassID;
import info.ata4.util.log.LogUtils;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DeserializeTest {
    
    private static final Logger L = Logger.getLogger(DeserializeTest.class.getName());
    
    public static void main(String[] args) {
        LogUtils.configure();
        
        int objTested = 0;
        int objFailed = 0;
        boolean retryDebug = true;
        
        for (String arg : args) {
            try {
                File file = new File(arg);

                AssetFile asset = new AssetFile();
                asset.load(file);

                Deserializer deser = new Deserializer(asset);

                for (AssetObjectPath path : asset.getObjectPaths()) {
                    if (path.classID1 < 0) {
                        continue;
                    }
                    
                    try {
                        deser.deserialize(path);
                    } catch (Exception ex) {
                        L.log(Level.INFO, "Deserialization failed for " + path.pathID + " (" + ClassID.getNameForID(path.classID2) + ")", ex);
                        objFailed++;
                        
                        if (retryDebug) {
                            // try again in debug mode
                            deser.setDebug(true);
                            try {
                                deser.deserialize(path);
                            } catch (Exception ex2) {
                            }
                            deser.setDebug(false);
                        }
                    }
                    
                    objTested++;
                }
            } catch (IOException ex) {
                L.log(Level.SEVERE, "Can't read asset file", ex);
            }
        }
        
        L.log(Level.INFO, "Tested objects: {0}", objTested);
        if (objFailed == 0) {
            L.info("All objects successfully deserialized!");
        } else {
            L.log(Level.INFO, "Failed deserializations: {0}", objFailed);
        }
    }
}
