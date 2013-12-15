/*
 ** 2013 July 21
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.util.io;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class MappedFileHandler {
    
    private File sourceFile;
    
    public File getSourceFile() {
        return sourceFile;
    }

    public void load(File file, boolean map) throws IOException {
        sourceFile = file;
        load(map ? NIOFileUtils.openReadOnly(file) : NIOFileUtils.load(file));
    }
    
    public void load(File file) throws IOException {
        load(file, true);
    }

    public abstract void load(ByteBuffer bb) throws IOException;
    
    public abstract void save(File file) throws IOException;
}
