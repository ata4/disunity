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

import info.ata4.io.file.FilenameSanitizer;
import info.ata4.io.util.PathUtils;
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
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class AssetCommand extends FileCommand {
    
    private static final Logger L = LogUtils.getLogger();
    
    private Path outputDir;
    private boolean processAssets = true;
    private boolean processBundledAssets = true;
    private boolean processBundles = true;
    
    protected Path getOutputDir() {
        return outputDir;
    }
    
    protected void setOutputDir(Path path) {
        // delete previous output dir if empty
        if (outputDir != null && PathUtils.isDirectoryEmpty(outputDir)) {
            PathUtils.deleteQuietly(outputDir);
        }
        
        // if the directory path points to an existing file, append a "_" to
        // avoid clashes
        if (Files.isRegularFile(path)) {
            path = PathUtils.append(path, "_");
        }
        
        this.outputDir = path;
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

        setOutputDir(PathUtils.removeExtension(file));
        processAsset(asset);
    }

    protected void processAssetBundleFile(Path file) throws IOException {
        // load asset bundle
        AssetBundle ab = new AssetBundle();
        ab.open(file);
        
        // process bundle
        Path outDir = PathUtils.removeExtension(file);
        setOutputDir(outDir);
        processAssetBundle(ab);
        
        if (processAssets && processBundledAssets) {
            // process bundle entries
            for (Map.Entry<String, ByteBuffer> entry : ab.getEntries().entrySet()) {
                String pathString = entry.getKey();

                // skip libraries
                if (pathString.endsWith(".dll") || pathString.endsWith(".mdb")) {
                    continue;
                }

                // skip dummy asset from Unity3D Obfuscator
                if (pathString.equals("33Obf")) {
                    continue;
                }
                
                Path path = outDir;
                
                // split path string and assemble path
                String[] names = StringUtils.split(pathString, '/');
                for (String name : names) {
                    path = path.resolve(FilenameSanitizer.sanitizeName(name));
                }
                
                L.log(Level.INFO, "{0}{1}{2}", new Object[]{file.toString(), file.getFileSystem().getSeparator(), pathString});

                // load asset
                ByteBuffer bb = entry.getValue();
                AssetFile asset = new AssetFile();
                asset.load(bb);
                asset.setSourceBundle(ab);

                // process asset
                setOutputDir(PathUtils.removeExtension(path));
                processAsset(asset);
            }
        }
    }
    
    protected void processAsset(AssetFile asset) throws IOException {
    }
    
    protected void processAssetBundle(AssetBundle bundle) throws IOException {
    }
}
