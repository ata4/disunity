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
import info.ata4.unity.asset.struct.ObjectPath;
import info.ata4.unity.serdes.UnityObject;
import info.ata4.io.file.FilenameSanitizer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class AssetExtractHandler {
    
    private static final Logger L = LogUtils.getLogger();

    private AssetFile asset;
    private ObjectPath path;
    private String className;
    private Path outDir;
    private String outFileName;
    private String outFileExt;
    private final Set<Path> uniqueFiles = new HashSet<>();
    
    public AssetFile getAssetFile() {
        return asset;
    }

    public void setAssetFile(AssetFile asset) {
        this.asset = asset;
    }

    public Path getOutputDir() {
        return outDir;
    }

    public void setOutputDir(Path outDir) {
        this.outDir = outDir;
        
        // new output directory, clear list of written files
        uniqueFiles.clear();
    }
    
    public String getOutputFileExtension() {
        return outFileExt;
    }
    
    public void setOutputFileExtension(String outFileExt) {
        this.outFileExt = outFileExt;
    }
    
    public String getOutputFileName() {
        return outFileName;
    }

    public void setOutputFileName(String outFileName) {
        // sanitize file name to avoid file system issues
        this.outFileName = FilenameSanitizer.sanitizeName(outFileName);
    }
    
    public ObjectPath getObjectPath() {
        return path;
    }

    public void setObjectPath(ObjectPath path) {
        this.path = path;
    }

    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }

    public abstract void extract(UnityObject obj) throws IOException;
    
    protected void writeData(ByteBuffer bb) throws IOException {
        Path outFile = getOutputFile();
        
        try {
            ByteBufferUtils.save(outFile, bb);
        } catch (IOException ex) {
            L.log(Level.WARNING, "Failed writing " + outFile, ex);
        }
    }
    
    protected void writeData(byte[] data) throws IOException {
        writeData(ByteBuffer.wrap(data));
    }
    
    protected Path getOutputFile() throws IOException {
        Path classDir = getOutputDir().resolve(getClassName());
        
        if (Files.notExists(classDir)) {
            Files.createDirectories(classDir);
        }
        
        String fileName = getOutputFileName();
        String fileExt = getOutputFileExtension();
        
        // use path ID if no file name is set
        if (StringUtils.isBlank(fileName)) {
            fileName = String.format("%06d", getObjectPath().getPathID());
        }
        
        // use "bin" if no file extension is set
        if (StringUtils.isBlank(fileExt)) {
            fileExt = "bin";
        }
        
        Path assetFile = getUniqueFile(classDir, fileName, fileExt);
        
        L.log(Level.INFO, "Writing {0} {1}",
                new Object[] {getClassName(), assetFile.getFileName()});
        
        return assetFile;
    }
    
    private Path getUniqueFile(Path parent, String name, String ext) {
        Path file = parent.resolve(String.format("%s.%s", name, ext));
        int fileNum = 1;
        
        while (uniqueFiles.contains(file)) {
            file = parent.resolve(String.format("%s_%d.%s", name, fileNum, ext));
            fileNum++;
        }
        
        uniqueFiles.add(file);
        
        return file;
    }
}
