/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.extract.handler;

import info.ata4.unity.asset.AssetFormat;
import info.ata4.unity.asset.struct.AssetObjectPath;
import info.ata4.unity.serdes.UnityObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class ExtractHandler {
    
    private static final Logger L = Logger.getLogger(ExtractHandler.class.getName());

    private File extractDir;
    private Set<File> filesWritten = new HashSet<>();
    private AssetFormat format;
    private boolean usePrefix = false;
    
    public void reset() {
        filesWritten.clear();
    }

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
    
    public String getFileExtension() {
        return "bin";
    }

    public abstract String getClassName();

    public abstract void extract(AssetObjectPath path, UnityObject obj) throws IOException;
    
    protected void writeFile(ByteBuffer bb, int id, String name, String ext) throws IOException {
        String className = getClassName();
        File classDir = new File(extractDir, className);
        
        if (!classDir.exists()) {
            classDir.mkdir();
        }
        
        String fileName;
        String fileExt = ext;
        
        if (fileExt == null) {
            fileExt = getFileExtension();
        }
        
        if (name == null || name.isEmpty()) {
            fileName = String.format("%06d", id);
        } else if (usePrefix) {
            fileName = String.format("%06d_%s", id, name);
        } else {
            fileName = name;
        }

        File assetFile = getUniqueFile(classDir, fileName, fileExt);
        
        L.log(Level.INFO, "Writing {0} {1}", new Object[] {className, fileName});
        
        try (FileOutputStream os = new FileOutputStream(assetFile)) {
            os.getChannel().write(bb);
        } catch (Exception ex) {
            L.log(Level.WARNING, "Failed writing " + assetFile, ex);
        }
    }
    
    protected void writeFile(ByteBuffer bb, int id, String name) throws IOException {
        writeFile(bb, id, name, null);
    }
    
    protected void writeFile(byte[] data, int id, String name, String ext) throws IOException {
        writeFile(ByteBuffer.wrap(data), id, name, ext);
    }
    
    protected void writeFile(byte[] data, int id, String name) throws IOException {
        writeFile(data, id, name, null);
    }
    
    private File getUniqueFile(File parent, String fileName, String ext) {
        File file = new File(parent, String.format("%s.%s", fileName, ext));
        int fileNum = 1;
        
        while (filesWritten.contains(file)) {
            file = new File(parent, String.format("%s_%d.%s", fileName, fileNum, ext));
            fileNum++;
        }
        
        filesWritten.add(file);
        
        return file;
    }
}
