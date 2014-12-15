/*
 ** 2014 December 02
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.command;

import com.beust.jcommander.Parameters;
import info.ata4.disunity.cli.LogProgress;
import info.ata4.io.util.PathUtils;
import info.ata4.log.LogUtils;
import info.ata4.unity.assetbundle.AssetBundleUtils;
import info.ata4.util.progress.Progress;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandNames = "bundle-extract",
    commandDescription = "Extracts files from asset bundles."
)
public class BundleExtractCommand extends BundleFileCommand {
    
    private static final Logger L = LogUtils.getLogger();
    
    @Override
    public void handleBundleFile(Path file) throws IOException {
        String fileName = PathUtils.getBaseName(file);
        Path outputDir = file.resolveSibling(fileName);
        Progress progress = new LogProgress(L);
        
        AssetBundleUtils.extract(file, outputDir, progress);
    }
}
