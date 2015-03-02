/*
 ** 2014 December 17
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.command;

import com.beust.jcommander.Parameters;
import info.ata4.log.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandNames = "debug-bundle-move",
    commandDescription = "Moves a bunch of asset bundles in directories based on their Unity version."
)
public class DebugBundleMove extends SingleFileCommand {
    
    private static final Logger L = LogUtils.getLogger();

    @Override
    public void handleFile(Path file) throws IOException {
        byte[] header = new byte[24];
        
        try (InputStream is = Files.newInputStream(file)) {
            is.read(header);
        }
        
        if (!new String(header, 0, 5).equals("Unity")) {
            L.log(Level.SEVERE, "{0} is not an asset bundle file", file);
        }

        String version = new String(header, 19, 5, Charset.forName("ASCII"));
        String versionShort = version.substring(0, 3);

        Path subDir = file.resolveSibling(versionShort);

        if (Files.notExists(subDir)) {
            Files.createDirectory(subDir);
        }

        Path newFile = subDir.resolve(file.getFileName());

        Files.move(file, newFile);
    }
}
