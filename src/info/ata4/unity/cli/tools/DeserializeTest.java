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

import info.ata4.log.LogUtils;
import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.asset.bundle.AssetBundle;
import info.ata4.unity.asset.struct.ObjectPath;
import info.ata4.unity.serdes.Deserializer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DeserializeTest {
    
    private static final Logger L = LogUtils.getLogger();
    
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
            
            for (Map.Entry<String, ByteBuffer> entry : ab.getEntries().entrySet()) {
                String name = entry.getKey();
                
                // skip libraries
                if (name.endsWith(".dll") || name.endsWith(".mdb")) {
                    continue;
                }
                
                AssetFile asset = new AssetFile();
                asset.load(entry.getValue());
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
