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

import info.ata4.log.LogUtils;
import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.asset.struct.AssetRef;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FixReferencesAction extends Action {
    
    private static final Logger L = LogUtils.getLogger();

    @Override
    public boolean supportsAssets() {
        return true;
    }

    @Override
    public boolean supportsAssetBundes() {
        return false;
    }

    @Override
    public void processAsset(AssetFile asset) throws IOException {
        Path assetFile = asset.getSourceFile();
        Path assetDir = assetFile.getParent();
        String assetFileName = assetFile.getFileName().toString();
        String assetPath;
        
        if (assetDir == null) {
            assetPath = "";
        } else {
            assetPath = assetDir.toAbsolutePath().toString();
            assetPath = FilenameUtils.separatorsToUnix(assetPath) + "/";
        }

        // fix path for all assets with (shared)assets extension
        boolean changed = false;
        for (AssetRef ref : asset.getReferences()) {
            Path refFile = Paths.get(ref.getFilePath());
            String refExt = FilenameUtils.getExtension(refFile.getFileName().toString());
            if (refExt.endsWith("assets") && Files.notExists(refFile)) {
                String filePathOld = ref.getFilePath();
                String filePathNew = assetPath + FilenameUtils.getName(ref.getFilePath());
                Path refFileNew = Paths.get(filePathNew);
                
                if (Files.exists(refFileNew)) {
                    L.log(Level.FINE, "Fixed reference: {0} -> {1}", new Object[]{filePathOld, filePathNew});
                    ref.setFilePath(filePathNew);
                    changed = true;
                } else {
                    L.log(Level.FINE, "Fixed reference not found: {0}", refFileNew);
                }
            }
        }

        if (!changed) {
            L.fine("No references changed, skipping saving");
            return;
        }
        
        // create backup by renaming the original file
        Path assetFileBackup = assetFile.resolveSibling(assetFileName + ".bak");
        Files.move(assetFile, assetFileBackup, StandardCopyOption.REPLACE_EXISTING);

        // save asset
        asset.save(assetFile);
    }
    
}
