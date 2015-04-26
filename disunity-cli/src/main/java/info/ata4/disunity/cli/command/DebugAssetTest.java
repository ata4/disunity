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
import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.rtti.ObjectData;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 * Debug command that deserializes and writes copies of asset files for validation.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandNames = "debug-asset-test",
    commandDescription = "Test asset file deserialization and writing."
)
public class DebugAssetTest extends MultiFileCommand {
    
    private static final Logger L = LogUtils.getLogger();
    
    @Override
    public void handleFile(Path file) throws IOException {
        AssetFile asset = new AssetFile();
        asset.load(file);
        
        try {
            testDeserialize(asset);
        } catch (Throwable t) {
            L.log(Level.WARNING, "{0} failed deserialization test: {1}",
                    new Object[]{asset.getSourceFile(), t.getMessage()});
        }
        
        try {
            testWrite(asset);
        } catch (Throwable t) {
            L.log(Level.WARNING, "{0} failed write test: {1}",
                    new Object[]{asset.getSourceFile(), t.getMessage()});
        }
    }

    private void testDeserialize(AssetFile asset) throws IOException {
        for (ObjectData obj : asset.objects()) {
            obj.instance();
        }
    }
    
    private void testWrite(AssetFile asset) throws IOException {
        Path tmpFile = Files.createTempFile("disunity", null);
        
        try {
            asset.save(tmpFile);

            if (!FileUtils.contentEquals(asset.getSourceFile().toFile(), tmpFile.toFile())) {
                throw new IOException("Files are not equal");
            }
        } finally {
            Files.deleteIfExists(tmpFile);
        }
    }
}
