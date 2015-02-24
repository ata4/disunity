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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import info.ata4.util.progress.LogProgress;
import info.ata4.disunity.cli.converters.PathConverter;
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
public class BundleExtractCommand extends SingleFileCommand {
    
    private static final Logger L = LogUtils.getLogger();
    
    @Parameter(
        names = {"-o", "--output"},
        description = "Output directory",
        converter = PathConverter.class
    )
    private Path outputDir;
    
    @Override
    public void handleFile(Path file) throws IOException {
        if (outputDir == null) {
            String fileName = PathUtils.getBaseName(file);
            outputDir = file.resolveSibling(fileName);
        }
        
        Progress progress = new LogProgress(L);

        AssetBundleUtils.extract(file, outputDir, progress);
    }
}
