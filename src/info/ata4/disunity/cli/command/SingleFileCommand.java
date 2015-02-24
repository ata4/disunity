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
import info.ata4.disunity.cli.converters.PathConverter;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class SingleFileCommand extends FileCommand {
    
    @Parameter(
        description = "<file>",
        converter = PathConverter.class,
        required = true
    )
    private List<Path> file;

    @Override
    public void run() {
        processFile(file.get(0));
    }
}
