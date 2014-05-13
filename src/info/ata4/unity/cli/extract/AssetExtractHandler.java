/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.extract;

import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.log.LogUtils;
import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.asset.struct.AssetObjectPath;
import info.ata4.unity.serdes.UnityObject;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class AssetExtractHandler {
    
    private static final Logger L = LogUtils.getLogger();

    private AssetFile asset;
    private Path extractDir;
    private String className;
    private String fileExtension = "bin";
    private final Set<Path> assignedFiles = new HashSet<>();
    
    public AssetFile getAssetFile() {
        return asset;
    }

    public void setAssetFile(AssetFile asset) {
        this.asset = asset;
    }

    public Path getExtractDir() {
        return extractDir;
    }

    public void setExtractDir(Path extractDir) {
        this.extractDir = extractDir;
        
        // new output directory, clear list of written files
        assignedFiles.clear();
    }
    
    public String getFileExtension() {
        return fileExtension;
    }
    
    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public abstract void extract(AssetObjectPath path, UnityObject obj) throws IOException;
    
    protected void writeFile(ByteBuffer bb, int id, String name) throws IOException {
        Path assetFile = getAssetFile(id, name);
        
        try {
            ByteBufferUtils.save(assetFile, bb);
        } catch (Exception ex) {
            L.log(Level.WARNING, "Failed writing " + assetFile, ex);
        }
    }
    
    protected void writeFile(byte[] data, int id, String name) throws IOException {
        writeFile(ByteBuffer.wrap(data), id, name);
    }
    
    protected Path getAssetFile(int id, String name) throws IOException {
        Path classDir = extractDir.resolve(className);
        
        if (!Files.exists(classDir)) {
            Files.createDirectories(classDir);
        }
        
        // remove any chars that could cause troubles on various file systems
        if (name != null && !name.isEmpty()) {
            name = name.replaceAll("[^a-zA-Z0-9\\._]+", "_");
        }
        
        String fileName = name;
        String fileExt = getFileExtension();
        
        if (fileName == null || fileName.isEmpty()) {
            fileName = String.format("%06d", id);
        }
        
        Path assetFile = getUniqueFile(classDir, fileName, fileExt);
        
        L.log(Level.INFO, "Writing {0} {1}",
                new Object[] {getClassName(), assetFile.getFileName()});
        
        return assetFile;
    }
    
    private Path getUniqueFile(Path parent, String name, String ext) {
        Path file = parent.resolve(String.format("%s.%s", name, ext));
        int fileNum = 1;
        
        while (assignedFiles.contains(file)) {
            file = parent.resolve(String.format("%s_%d.%s", name, fileNum, ext));
            fileNum++;
        }
        
        assignedFiles.add(file);
        
        return file;
    }
}
