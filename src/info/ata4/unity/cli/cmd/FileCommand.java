/*
 ** 2014 July 01
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.cmd;

import com.beust.jcommander.Parameter;
import info.ata4.log.LogUtils;
import info.ata4.unity.cli.converters.PathConverter;
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
    
    protected abstract void processFile(Path file) throws IOException;
    
    protected void processDirectory(Path dir) throws IOException {
        boolean allowDirs = getOptions().isRecursive();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dir)) {
            for (Path path : directoryStream) {
                processPath(path, allowDirs);
            }
        }
    }
    
    protected void processPath(Path path, boolean allowDirs) {
        try {
            if (Files.isDirectory(path)) {
                if (allowDirs) {
                    L.info(path.toString());
                    processDirectory(path);
                }
            } else {
                L.info(path.toString());
                processFile(path);
            }
        } catch (IOException ex) {
            L.log(Level.WARNING, "Can't process " + path, ex);
        }
    }
    
    protected void processEnd() {
    }

    @Override
    public void run() {
        for (Path file : files) {
            if (Files.notExists(file)) {
                L.log(Level.WARNING, "File not found: {0}", file);
                continue;
            }

            processPath(file, true);
        }
        
        processEnd();
    }
}
