/*
 ** 2014 December 25
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.extract;

import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.log.LogUtils;
import info.ata4.unity.rtti.ObjectData;
import info.ata4.unity.util.UnityClass;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class AbstractAssetExtractor implements AssetExtractor {
    
    private static final Logger L = LogUtils.getLogger();

    private Path outputDirectory;
    
    public abstract UnityClass getUnityClass();

    @Override
    public boolean isEligible(ObjectData objectData) {
        return objectData.info().unityClass().equals(getUnityClass());
    }

    @Override
    public Path getOutputDirectory() {
        return outputDirectory;
    }

    @Override
    public void setOutputDirectory(Path dir) {
        outputDirectory = dir;
    }
    
    protected void writeFile(String name, String ext, ByteBuffer data) throws IOException {
        Path outFile = getOutputDirectory().resolve(name + "." + ext);
        ByteBufferUtils.save(outFile, data);
        
        L.log(Level.INFO, "Extracted {0}", outFile.getFileName());
    }
}
