/*
 ** 2015 December 01
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.command.bundle;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import info.ata4.disunity.cli.command.FileCommand;
import info.ata4.disunity.cli.converters.PathConverter;
import info.ata4.io.util.PathUtils;
import info.ata4.junity.bundle.Bundle;
import info.ata4.junity.bundle.BundleWriter;
import info.ata4.log.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandDescription = "Create bundle from a property file."
)
public class BundlePack extends FileCommand {

    private static final Logger L = LogUtils.getLogger();

    @Parameter(
        names = {"-o", "--output"},
        description = "Asset bundle output file",
        converter = PathConverter.class
    )
    private Path outFile;

    @Override
    protected void runFile(Path file) {
        if (outFile == null) {
            String fileName = PathUtils.getBaseName(file);
            outFile = file.getParent().resolve(fileName + ".unity3d");
        }

        Bundle bundle = new Bundle();
        try (BundleWriter bundleWriter = new BundleWriter(outFile)) {
            BundleProps.read(file, bundle);
            bundleWriter.write(bundle, progress);
        } catch (IOException ex) {
            L.log(Level.WARNING, "Can't pack asset bundle " + file, ex);
        }
    }
}
