/*
 ** 2014 November 28
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
import info.ata4.log.LogUtils;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class FileCommand extends Command {
    
    private static final Logger L = LogUtils.getLogger();
    
    @Parameter(
        description = "<file> [file]...",
        converter = PathConverter.class,
        required = true
    )
    private List<Path> files;

    @Override
    public void run() {
        for (Path file : files) {
            processFile(file, 0);
        }
    }
    
    private void processFile(Path file, int level) {        
        L.log(Level.INFO, "Processing {0}", file);
        if (Files.isDirectory(file)) {
            // cancel if the recursive option is disabled and we're deeper than
            // one level in the current directory
            if (!getOptions().isRecursive() && level > 0) {
                return;
            }
            
            try (DirectoryStream<Path> dir = Files.newDirectoryStream(file)) {
                for (Path subfile : dir) {
                    processFile(subfile, level + 1);
                }
            } catch (IOException ex) {
                L.log(Level.WARNING, "Can't open directory " + file, ex); 
            }
        } else {
            try {
                handleFile(file);
            } catch (IOException ex) {
                L.log(Level.WARNING, "Can't process file " + file, ex); 
            }
        }
    }

    public abstract void handleFile(Path file) throws IOException;
}
