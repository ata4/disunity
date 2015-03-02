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
import info.ata4.log.LogUtils;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class MultiFileCommand extends FileCommand {
    
    private static final Logger L = LogUtils.getLogger();
    
    @Parameter(
        description = "<file> [file]...",
        required = true
    )
    private List<String> filePaths;

    @Override
    public void run() {
        for (String filePath : filePaths) {
            String name = FilenameUtils.getName(filePath);
            String path = FilenameUtils.getFullPath(filePath);

            Path dir = Paths.get(path);
            
            if (!Files.isDirectory(dir)) {
                L.log(Level.WARNING, "File not found: {0}", filePath);
                continue;
            }

            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir, name)) {
                for (Path subFile : dirStream) {
                    if (Files.isRegularFile(subFile)) {
                        processFile(subFile);
                    }
                }
            } catch (IOException ex2) {
                L.log(Level.WARNING, "Can't open directory " + dir, ex2); 
            }
        }
    }
}
