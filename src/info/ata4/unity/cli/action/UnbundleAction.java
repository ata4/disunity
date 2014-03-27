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

import info.ata4.unity.assetbundle.AssetBundle;
import info.ata4.unity.assetbundle.AssetBundleEntry;
import info.ata4.io.buffer.ByteBufferUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class UnbundleAction extends Action {
    
    private static final Logger L = Logger.getLogger(UnbundleAction.class.getName());

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
        for (AssetBundleEntry entry : bundle.getEntries()) {
            String entryName = entry.getName();
            ByteBuffer entryBuffer = entry.getByteBuffer();
            
            L.log(Level.INFO, "Extracting {0}", entryName);
            
            Path entryFile = getOutputDir().resolve(entryName);
            Path entryDir = entryFile.getParent();
            
            if (!Files.exists(entryDir)) {
                Files.createDirectories(entryDir);
            }
            
            ByteBufferUtils.save(entryFile, entryBuffer);
        }
    }
}
