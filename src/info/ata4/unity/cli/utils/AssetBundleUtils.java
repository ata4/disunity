/*
 ** 2013 December 28
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.utils;

import info.ata4.unity.assetbundle.AssetBundle;
import info.ata4.unity.assetbundle.AssetBundleEntry;
import info.ata4.util.io.ByteBufferUtils;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetBundleUtils {
    
    private static final Logger L = Logger.getLogger(AssetUtils.class.getName());

    private final AssetBundle ab;

    public AssetBundleUtils(AssetBundle ab) {
        this.ab = ab;
    }
    
    public void extract(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            Files.createDirectory(dir);
        }

        for (AssetBundleEntry entry : ab.getEntries()) {
            String entryName = entry.getName();
            ByteBuffer entryBuffer = entry.getByteBuffer();
            
            L.log(Level.INFO, "Extracting {0}", entryName);
            
            Path entryFile = dir.resolve(entryName);
            Path entryDir = entryFile.getParent();
            
            if (!Files.exists(entryDir)) {
                Files.createDirectories(entryDir);
            }
            
            ByteBufferUtils.save(entryFile, entryBuffer);
        }
    }

    public void printInfo(PrintStream ps) {
        ps.println("File version: " + ab.getFileVersion());
        ps.println("Version: " + ab.getVersion());
        ps.println("Revision: " + ab.getRevision());
        ps.println("Compressed: " + (ab.isCompressed() ? "yes" : "no"));
        ps.println("Entries: " + ab.getEntries().size());
        ps.println();
    }

    public void list(PrintStream ps) {
        int p1 = 64;
        int p2 = 10;
        int p3 = 10;
        
        ps.print(StringUtils.rightPad("Path", p1));
        ps.print(" | ");
        ps.print(StringUtils.rightPad("Offset", p2));
        ps.print(" | ");
        ps.print(StringUtils.leftPad("Length", p3));
        ps.println();
        
        ps.print(StringUtils.repeat("-", p1));
        ps.print(" | ");
        ps.print(StringUtils.repeat("-", p2));
        ps.print(" | ");
        ps.print(StringUtils.repeat("-", p3));
        ps.println();
        
        for (AssetBundleEntry entry : ab) {
            ps.print(StringUtils.rightPad(entry.getName(), p1));
            ps.print(" | ");
            ps.print(StringUtils.rightPad(String.format("0x%x", entry.getOffset()), p2));
            ps.print(" | ");
            ps.print(StringUtils.leftPad(String.valueOf(entry.getSize()), p3));
            ps.println();
        }
    }
}
