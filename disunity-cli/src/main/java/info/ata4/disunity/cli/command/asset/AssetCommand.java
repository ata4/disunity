/*
 ** 2015 December 01
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.command.asset;

import info.ata4.disunity.cli.command.RecursiveFileCommand;
import info.ata4.junity.bundle.Bundle;
import info.ata4.junity.bundle.BundleEntry;
import info.ata4.junity.bundle.BundleReader;
import info.ata4.junity.bundle.BundleUtils;
import info.ata4.junity.serialize.SerializedFile;
import info.ata4.junity.serialize.SerializedFileReader;
import info.ata4.log.LogUtils;
import static info.ata4.util.function.IOConsumer.uncheck;
import static info.ata4.util.function.Predicates.not;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class AssetCommand extends RecursiveFileCommand {

    private static final Logger L = LogUtils.getLogger();

    @Override
    protected void runFileRecursive(Path file) {
        if (BundleUtils.isBundle(file)) {
            // file is a bundle, load serialized files from it
            try (BundleReader bundleReader = new BundleReader(file)) {
                Bundle bundle = bundleReader.read();
                bundle.entries().stream()
                    .filter(not(BundleEntry::isLibrary))
                    .filter(not(BundleEntry::isResource))
                    .forEach(uncheck(entry -> {
                        try (SerializedFileReader reader = new SerializedFileReader(
                                BundleUtils.dataReaderForEntry(entry))) {
                            SerializedFile serialized = reader.read();
                            runSerializedFile(file.resolve(entry.name()), serialized);
                        }
                    })
                );
            } catch (UncheckedIOException | IOException ex) {
                L.log(Level.WARNING, "Can't open asset bundle " + file, ex);
            }
        } else {
            // load file directly
            try (SerializedFileReader reader = new SerializedFileReader(file)) {
                SerializedFile serialized = reader.read();
                runSerializedFile(file, serialized);
            } catch (IOException ex) {
                L.log(Level.WARNING, "Can't open asset file " + file, ex);
            }
        }
    }

    protected abstract void runSerializedFile(Path file, SerializedFile serialized);
}
