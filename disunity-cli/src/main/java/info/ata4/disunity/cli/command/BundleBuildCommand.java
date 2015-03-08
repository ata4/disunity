/*
 ** 2014 December 16
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
import info.ata4.disunity.cli.converters.PathConverter;
import info.ata4.io.util.PathUtils;
import info.ata4.unity.assetbundle.AssetBundleUtils;
import java.io.IOException;
import java.nio.file.Path;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandNames = "bundle-build",
    commandDescription = "Builds an asset bundle from a .json property file."
)
public class BundleBuildCommand extends SingleFileCommand {
    
    @Parameter(
        names = {"-o", "--output"},
        description = "Asset bundle output file",
        converter = PathConverter.class
    )
    private Path outFile;

    @Override
    public void handleFile(Path file) throws IOException {
        if (outFile == null) {
            String fileName = PathUtils.getBaseName(file);
            outFile = file.getParent().resolve(fileName + ".unity3d");
        }
        AssetBundleUtils.build(file, outFile);
    }
}
