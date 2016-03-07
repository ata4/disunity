/*
 ** 2015 November 30
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.command.bundle;

import info.ata4.disunity.cli.command.RecursiveFileCommand;
import info.ata4.junity.bundle.Bundle;
import info.ata4.junity.bundle.BundleReader;
import info.ata4.junity.bundle.BundleUtils;
import info.ata4.log.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class BundleCommand extends RecursiveFileCommand {

    private static final Logger L = LogUtils.getLogger();

    @Override
    protected void runFileRecursive(Path file) {
        try (BundleReader reader = new BundleReader(file)) {
            Bundle bundle = reader.read();
            runBundle(file, bundle);
        } catch (IOException ex) {
            L.log(Level.WARNING, "Can't open asset bundle " + file, ex);
        }
    }

    @Override
    protected boolean fileFilter(Path file) {
        return BundleUtils.isBundle(file);
    }

    protected abstract void runBundle(Path file, Bundle bundle);
}
