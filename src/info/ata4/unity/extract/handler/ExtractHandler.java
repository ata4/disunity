/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.extract.handler;

import info.ata4.unity.asset.AssetFormat;
import info.ata4.unity.extract.AssetExtractor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class ExtractHandler {
    
    private static final Logger L = Logger.getLogger(RawHandler.class.getName());
    
    private File extractDir;
    private AssetFormat format;
    private boolean usePrefix = false;
    
    public File getExtractDir() {
        return extractDir;
    }

    public void setExtractDir(File extractDir) {
        this.extractDir = extractDir;
    }
    
    public AssetFormat getAssetFormat() {
        return format;
    }

    public void setAssetFormat(AssetFormat format) {
        this.format = format;
    }

    public abstract String getClassName();

    public abstract void extract(ByteBuffer bb, int id) throws IOException;
    
    protected void extractToFile(ByteBuffer bb, int id, String name, String ext) throws IOException {
        String className = getClassName();
        File classDir = new File(extractDir, className);
        
        if (!classDir.exists()) {
            classDir.mkdir();
        }
        
        String assetFileName;
        
        if (name == null) {
            assetFileName = String.format("%06d.%s", id, ext);
        } else if (usePrefix) {
            assetFileName = String.format("%06d_%s.%s", id, name, ext);
        } else {
            assetFileName = String.format("%s.%s", name, ext);
        }

        File assetFile = new File(classDir, assetFileName);
        
        L.log(Level.INFO, "Writing {0} {1}", new Object[] {className, assetFileName});
        
        try (
            FileOutputStream os = new FileOutputStream(assetFile)
        ) {
            os.getChannel().write(bb);
        } catch (Exception ex) {
            L.log(Level.WARNING, "Failed writing " + assetFile, ex);
        }
    }
    
    protected void extractToFile(byte[] data, int id, String name, String ext) throws IOException {
        extractToFile(ByteBuffer.wrap(data), id, name, ext);
    }
    
    protected String getAssetName(ByteBuffer bb) {
        return AssetExtractor.getAssetName(bb);
    }
}
