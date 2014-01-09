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
import info.ata4.unity.assetbundle.AssetBundle;
import info.ata4.unity.cli.classfilter.ClassFilter;
import java.io.IOException;
import java.nio.file.Path;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class Action {
    
    private Path outputDir;
    private ClassFilter cf;
    
    public Path getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(Path outputDir) {
        this.outputDir = outputDir;
    }
    
    public ClassFilter getClassFilter() {
        return cf;
    }

    public void setClassFilter(ClassFilter cf) {
        this.cf = cf;
    }
    
    public abstract boolean supportsAssets();

    public abstract boolean supportsAssetBundes();
    
    public boolean requiresOutputDir() {
        return false;
    }
    
    public boolean requiresWriting() {
        return false;
    }
    
    public void processAsset(AssetFile asset) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void processAssetBundle(AssetBundle bundle) throws IOException {
        throw new UnsupportedOperationException();
    }
    
    public void finished() {
    }
}
