/*
 ** 2014 September 22
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.gui.util;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FileExtensionFilter extends FileFilter {
    
    private final String desc;
    private final Set<String> exts;

    public FileExtensionFilter(String desc, String... ext) {
        this.desc = desc;
        this.exts = new HashSet<>(Arrays.asList(ext));
    }

    @Override
    public boolean accept(File f) {
        return exts.contains(FilenameUtils.getExtension(f.getName()));
    }

    @Override
    public String getDescription() {
        return desc;
    }
    
}
