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

import info.ata4.log.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class FileCommand extends Command {
    
    private static final Logger L = LogUtils.getLogger();
    private Path currentFile;
    
    protected Path getCurrentFile() {
        return currentFile;
    }
    
    protected void processFile(Path file) {        
        L.log(Level.INFO, "Processing {0}", file);
        try {
            currentFile = file;
            handleFile(file);
            currentFile = null;
        } catch (IOException ex) {
            L.log(Level.WARNING, "Can't process file " + file, ex); 
        }
    }
    
    public abstract void handleFile(Path file) throws IOException;
}
