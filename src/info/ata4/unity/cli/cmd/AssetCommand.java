/*
 ** 2014 July 01
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.cmd;

import info.ata4.log.LogUtils;
import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.asset.bundle.AssetBundle;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class AssetCommand extends FileCommand {
    
    private static final Logger L = LogUtils.getLogger();
    
    private Path outputDir;
    private String outputDirName;
    private boolean processAssets = true;
    private boolean processBundledAssets = true;
    private boolean processBundles = true;
    
    protected Path getOutputDir() {
        return outputDir;
    }
    
    protected void setOutputDir(Path outputDir) {
        // try to delete previous output dir in case it's empty and don't mind
        // if it doesn't work
        try {
            if (outputDir == null && this.outputDir != null) {
                Files.delete(this.outputDir);
            }
        } catch (IOException ex) {
        }
        
        this.outputDir = outputDir;
    }
    
    protected boolean isProcessAssets() {
        return processAssets;
    }

    protected void setProcessAssets(boolean processAssets) {
        this.processAssets = processAssets;
    }

    protected boolean isProcessBundledAssets() {
        return processBundledAssets;
    }

    protected void setProcessBundledAssets(boolean processBundledAssets) {
        this.processBundledAssets = processBundledAssets;
    }

    protected boolean isProcessBundles() {
        return processBundles;
    }

    protected void setProcessBundles(boolean processBundles) {
        this.processBundles = processBundles;
    }
    
    @Override
    protected void processFile(Path file) throws IOException {
        // file name minus extension
        outputDirName = file.getFileName().toString();
        
        // if the file has no extension, append a "_" to the output directory
        // name so the file system won't have a file and dir with the same name
        if (FilenameUtils.getExtension(outputDirName).isEmpty()) {
            outputDirName += "_";
        } else {
            outputDirName = FilenameUtils.removeExtension(outputDirName);
        }
        
        if (AssetBundle.isAssetBundle(file)) {
            if (processBundles) {
                processAssetBundleFile(file);
            }
        } else {
            if (processAssets) {
                processAssetFile(file);
            }
        }
    }
    
    protected void processAssetFile(Path file) throws IOException {
        AssetFile asset = new AssetFile();
        asset.open(file);

        setOutputDir(file.resolveSibling(outputDirName));
        processAsset(asset);
        setOutputDir(null);
    }

    protected void processAssetBundleFile(Path file) throws IOException {
        // load asset bundle
        AssetBundle ab = new AssetBundle();
        ab.open(file);

        processAssetBundle(ab);
        
        if (processAssets && processBundledAssets) {
            // process bundle entries
            for (Map.Entry<String, ByteBuffer> entry : ab.getEntries().entrySet()) {
                String name = entry.getKey();

                // skip libraries
                if (name.endsWith(".dll") || name.endsWith(".mdb")) {
                    continue;
                }

                // skip dummy asset from Unity3D Obfuscator
                if (name.equals("33Obf")) {
                    continue;
                }

                L.log(Level.INFO, "{0}{1}{2}", new Object[]{file.toString(), file.getFileSystem().getSeparator(), name});

                ByteBuffer bb = entry.getValue();

                AssetFile asset = new AssetFile();
                asset.load(bb);
                asset.setSourceBundle(ab);

                setOutputDir(file.resolveSibling(outputDirName).resolve(name));
                processAsset(asset);
                setOutputDir(null);
            }
        }
    }
    
    protected void processAsset(AssetFile asset) throws IOException {
    }
    
    protected void processAssetBundle(AssetBundle bundle) throws IOException {
    }
}
