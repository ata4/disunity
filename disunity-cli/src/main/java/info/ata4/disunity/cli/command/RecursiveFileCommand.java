/*
 ** 2015 December 03
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.command;

import com.beust.jcommander.Parameter;
import info.ata4.log.LogUtils;
import java.io.IOException;
import static java.nio.file.FileVisitOption.FOLLOW_LINKS;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class RecursiveFileCommand extends FileCommand {

    private static final Logger L = LogUtils.getLogger();

    @Parameter(
        names = { "-r", "--recursive" },
        description = "Scan directories and sub-directories for files as well."
    )
    private boolean recursive = defaultRecursive();

    @Parameter(
        names = { "-d", "--recursive-depth" },
        description = "The maximum number of directory levels to visit."
    )
    private int maxDepth = defaultMaxDepth();

    @Override
    protected void runFile(Path file) {
        if (Files.isDirectory(file) && recursive) {
            try {
                Files.walk(file, maxDepth, FOLLOW_LINKS)
                    .filter(this::fileFilter)
                    .forEach(this::runFileRecursive);
            } catch (IOException ex) {
                L.log(Level.WARNING, "Can't walk directory " + file, ex);
            }
        } else {
            runFileRecursive(file);
        }
    }

    protected abstract void runFileRecursive(Path file);

    protected boolean fileFilter(Path file) {
        return Files.isRegularFile(file);
    }

    protected boolean defaultRecursive() {
        return false;
    }

    protected int defaultMaxDepth() {
        return 1024;
    }
}
