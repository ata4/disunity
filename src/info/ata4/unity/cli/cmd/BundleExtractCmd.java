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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
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
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandNames = "bundle-extract",
    commandDescription = "Extracts files from asset bundles."
)
public class BundleExtractCmd extends AssetCommand {
    
    @Parameter(
        names = "--editor",
        description = "Prepare extracted files for the Unity editor"
    )
    private boolean editor;
    
    private static final Logger L = LogUtils.getLogger();
    
    public BundleExtractCmd() {
        setProcessAssets(false);
        setProcessBundledAssets(false);
    }

    @Override
    public void processAssetBundle(AssetBundle bundle) throws IOException {
        for (Map.Entry<String, ByteBuffer> entry : bundle.getEntries().entrySet()) {
            String entryName = entry.getKey();
            ByteBuffer entryBuffer = entry.getValue();
            
            // add .unity extension to scene files
            if (editor && FilenameUtils.getExtension(entryName).isEmpty()) {
                entryName += ".unity";
            }
            
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
