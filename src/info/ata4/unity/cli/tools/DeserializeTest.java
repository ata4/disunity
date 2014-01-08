/*
 ** 2013 August 11
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.tools;

import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.asset.struct.AssetObjectPath;
import info.ata4.unity.assetbundle.AssetBundle;
import info.ata4.unity.assetbundle.AssetBundleEntry;
import info.ata4.unity.serdes.Deserializer;
import info.ata4.unity.util.ClassID;
import info.ata4.util.log.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DeserializeTest {
    
    private static final Logger L = Logger.getLogger(DeserializeTest.class.getName());
    
    private int objTested;
    private int objFailed;
    private boolean retryDebug = true;
    
    public static void main(String[] args) {
        LogUtils.configure();
        
        DeserializeTest dt = new DeserializeTest();
        
        for (String arg : args) {
            try {
                dt.testFile(Paths.get(arg));
            } catch (IOException ex) {
                L.log(Level.SEVERE, "Can't test " + arg, ex);
            }
        }
    }
    
    public void testFile(Path file) throws IOException {
        L.log(Level.INFO, "Testing {0}", file);
        
        objTested = 0;
        objFailed = 0;
        
        if (AssetBundle.isAssetBundle(file)) {
            AssetBundle ab = new AssetBundle();
            ab.load(file);
            
            for (AssetBundleEntry entry : ab.getEntries()) {
                AssetFile asset = new AssetFile();
                asset.load(entry.getByteBuffer());
                testAsset(asset);
            }
        } else {
            AssetFile asset = new AssetFile();
            asset.load(file);
            testAsset(asset);
        }
        
        L.log(Level.INFO, "Tested objects: {0}", objTested);
        if (objFailed == 0) {
            L.log(Level.INFO, "All objects successfully deserialized!");
        } else {
            L.log(Level.INFO, "Failed deserializations: {0}", objFailed);
        }
    }
    
    public void testAsset(AssetFile asset) {
        Deserializer deser = new Deserializer(asset);

        for (AssetObjectPath path : asset.getPaths()) {
            // skip MonoBehaviours
            if (path.isScript()) {
                continue;
            }

            try {
                deser.deserialize(path);
            } catch (Exception ex) {
                L.log(Level.INFO, "Deserialization failed for " + path, ex);
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
    }
}
