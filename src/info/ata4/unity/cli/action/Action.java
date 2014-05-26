/*
 ** 2014 January 08
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.action;

import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.asset.bundle.AssetBundle;
import info.ata4.unity.cli.DisUnityOptions;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Abstract class for command actions.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class Action {
    
    private Path outputDir;
    private DisUnityOptions opts;
    
    public Path getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(Path outputDir) {
        this.outputDir = outputDir;
    }
    
    public DisUnityOptions getOptions() {
        return opts;
    }

    public void setOptions(DisUnityOptions opts) {
        this.opts = opts;
    }
    
    /**
     * Returns true if the action can process asset files.
     */
    public abstract boolean supportsAssets();

    /**
     * Returns true if the action can process asset bundle files.
     */
    public abstract boolean supportsAssetBundes();
    
    /**
     * Returns true if the action requires an output directory to write files.
     */
    public boolean requiresOutputDir() {
        return false;
    }
    
    /**
     * Returns true if the action may modify assets or asset bundles during
     * processing.
     */
    public boolean requiresWriting() {
        return false;
    }
    
    /**
     * Processes the given asset.
     * 
     * @param asset
     * @throws IOException 
     */
    public void processAsset(AssetFile asset) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Processes the given asset bundle.
     * 
     * @param bundle
     * @throws IOException 
     */
    public void processAssetBundle(AssetBundle bundle) throws IOException {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Called when all availabe files have been processed.
     */
    public void finished() {
    }
}
