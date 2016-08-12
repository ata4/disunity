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
import info.ata4.junity.bundle.BundleReader;
import info.ata4.log.LogUtils;
import static info.ata4.util.function.IOConsumer.uncheck;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandDescription = "Extract files from bundles."
)
public class BundleUnpack extends FileCommand {

    private static final Logger L = LogUtils.getLogger();

    @Parameter(
        names = {"-o", "--output"},
        description = "Output directory",
        converter = PathConverter.class
    )
    private Path outputDir;

    @Parameter(
        names = {"-f", "--filename"},
        description = "Extract file with this name only."
    )
    private String filename;

    @Parameter(
        names = {"-p", "--prop"},
        description = "Write bundle property file."
    )
    private boolean writeProp;

    @Override
    protected void runFile(Path file) {
        try (BundleReader bundleReader = new BundleReader(file)) {
            Bundle bundle = bundleReader.read();

            AtomicInteger done = new AtomicInteger();
            long total = bundle.entryInfos().size();

            // define output directory, if not yet defined
            if (outputDir == null) {
                // if there's only one file inside the bundle, then don't bother
                // with sub-directories
                if (bundle.entryInfos().size() == 1) {
                    outputDir = file.getParent();
                    if (outputDir == null) {
                        // Passed a filename only. Use the current directory.
                        outputDir = Paths.get(".");
                    }
                } else {
                    String fileName = PathUtils.getBaseName(file);
                    outputDir = file.resolveSibling(fileName);
                }
            }

            try {
                bundle.entries()
                    .stream()
                    .filter(entry -> filename == null || entry.name().equals(filename))
                    .forEach(uncheck(entry -> {
                        progress.update(Optional.of(entry.name()), done.getAndIncrement() / (double) total);
                        Path entryFile = outputDir.resolve(entry.name());

                        Files.createDirectories(entryFile.getParent());
                        Files.copy(entry.inputStream(), entryFile, REPLACE_EXISTING);

                        if (done.get() == total) {
                            progress.update(Optional.empty(), 1);
                        }
                }));
            } catch (UncheckedIOException ex) {
                throw ex.getCause();
            }

            if (writeProp && filename == null) {
                String bundleName = outputDir.getFileName().toString();
                Path propsFile = outputDir.getParent().resolve(bundleName + ".json");
                BundleProps.write(propsFile, bundle);
            }
        } catch (IOException ex) {
            L.log(Level.WARNING, "Can't unpack asset bundle " + file, ex);
        }
    }
}
