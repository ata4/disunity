/*
 ** 2014 September 22
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.gui.util;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
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
        return f.isDirectory() || exts.contains(FilenameUtils.getExtension(f.getName()));
    }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder(desc);
        sb.append(" (");
        
        Iterator<String> iter = exts.iterator();
        while (iter.hasNext()) {
            sb.append("*.");
            sb.append(iter.next());
            if (iter.hasNext()) {
                sb.append(";");
            }
        }
        
        sb.append(")");
        
        return sb.toString();
    }
    
}
