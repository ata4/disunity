/*
 ** 2014 April 20
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.cmd;

import com.beust.jcommander.Parameters;
import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.io.util.PathUtils;
import info.ata4.log.LogUtils;
import info.ata4.unity.asset.bundle.AssetBundle;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandNames = "bundle-inject",
    commandDescription = "Injects extracted files back into asset bundles."
)
public class BundleInjectCmd extends AssetCommand {
    
    private static final Logger L = LogUtils.getLogger();
    
    public BundleInjectCmd() {
        setProcessAssets(false);
        setProcessBundledAssets(false);
    }
    
    @Override
    public void processAssetBundle(AssetBundle bundle) throws IOException {
        Path bundleFile = bundle.getSourceFile();
        Path bundleDir = PathUtils.removeExtension(bundleFile);
        
        // there's no point in injection if the files haven't been extracted yet
        if (Files.notExists(bundleDir)) {
            L.log(Level.WARNING, "Bundle directory {0} doesn''t exist!", bundleDir);
            return;
        }
        
        for (Map.Entry<String, ByteBuffer> entry : bundle.getEntries().entrySet()) {
            String entryName = entry.getKey();
            Path entryFile = bundleDir.resolve(entryName);

            // replace files in bundle that exist in the directory
            if (Files.exists(entryFile)) {
                L.log(Level.INFO, "Injecting {0}", entryName);
                entry.setValue(ByteBufferUtils.openReadOnly(entryFile));
            }
        }
        
        // create backup by renaming the original file
        Path bundleFileBackup = PathUtils.append(bundleFile, ".bak");
        Files.move(bundleFile, bundleFileBackup, StandardCopyOption.REPLACE_EXISTING);

        // save bundle to original path
        bundle.save(bundleFile);
    }
}
