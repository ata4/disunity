/*
 ** 2014 January 08
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.action;

import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.log.LogUtils;
import info.ata4.unity.asset.bundle.AssetBundle;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BundleExtractAction extends Action {
    
    private static final Logger L = LogUtils.getLogger();

    @Override
    public boolean supportsAssets() {
        return false;
    }

    @Override
    public boolean supportsAssetBundes() {
        return true;
    }
    
    @Override
    public boolean requiresOutputDir() {
        return true;
    }

    @Override
    public void processAssetBundle(AssetBundle bundle) throws IOException {
        for (Map.Entry<String, ByteBuffer> entry : bundle.getEntries().entrySet()) {
            String entryName = entry.getKey();
            ByteBuffer entryBuffer = entry.getValue();
            
            L.log(Level.INFO, "Extracting {0}", entryName);
            
            Path entryFile = getOutputDir().resolve(entryName);
            Path entryDir = entryFile.getParent();
            
            if (Files.notExists(entryDir)) {
                Files.createDirectories(entryDir);
            }
            
            ByteBufferUtils.save(entryFile, entryBuffer);
        }
    }
}
