/*
 ** 2015 December 01
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.command;

import com.beust.jcommander.Parameter;
import info.ata4.disunity.cli.converters.PathConverter;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class FileCommand extends Command {

    @Parameter(
        description = "<file> [file]...",
        converter = PathConverter.class,
        required = true
    )
    private List<Path> filePaths;

    @Override
    public void run() {
        filePaths.forEach(this::runFile);
    }

    protected abstract void runFile(Path file);
}
