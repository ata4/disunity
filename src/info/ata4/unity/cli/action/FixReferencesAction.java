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
    public boolean requiresWriting() {
        return true;
    }

    @Override
    public void processAsset(AssetFile asset) throws IOException {
        Path sourceFile = asset.getSourceFile();
        Path sourceParent = sourceFile.getParent();
        String assetPath;
        
        if (sourceParent == null) {
            assetPath = "";
        } else {
            assetPath = sourceParent.toAbsolutePath().toString();
            assetPath = FilenameUtils.separatorsToUnix(assetPath) + "/";
        }

        // fix path for all assets with (shared)assets extension
        boolean changed = false;
        for (AssetRef ref : asset.getReferences()) {
            Path refFile = Paths.get(ref.getFilePath());
            String refExt = FilenameUtils.getExtension(refFile.getFileName().toString());
            if (refExt.endsWith("assets") && !Files.exists(refFile)) {
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

        if (changed) {
            // create backup first
            try {
                String backupFileName = sourceFile.getFileName().toString() + ".bak";
                Path backupFile = sourceFile.resolveSibling(backupFileName);
                Files.copy(sourceFile, backupFile);
            } catch (IOException ex) {
                // backup is mandatory, don't risk any loss of data 
                throw new IOException("Can't create backup copy", ex);
            }
            
            asset.save(sourceFile);
        } else {
            L.fine("No references changed, skipping saving");
        }
    }
    
}
