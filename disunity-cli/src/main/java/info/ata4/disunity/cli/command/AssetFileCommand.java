/*
 ** 2014 December 01
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.command;

import info.ata4.io.DataReader;
import info.ata4.log.LogUtils;
import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.asset.VersionInfo;
import info.ata4.unity.assetbundle.AssetBundleEntry;
import info.ata4.unity.assetbundle.AssetBundleReader;
import info.ata4.unity.assetbundle.AssetBundleUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class AssetFileCommand extends MultiFileCommand {
    
    private static final Logger L = LogUtils.getLogger();
    
    private AssetBundleReader currentAssetBundle;
    private AssetBundleEntry currentAssetBundleEntry;
    
    protected AssetBundleReader getCurrentAssetBundle() {
        return currentAssetBundle;
    }
    
    protected AssetBundleEntry getCurrentAssetBundleEntry() {
        return currentAssetBundleEntry;
    }

    @Override
    public void handleFile(Path file) throws IOException {
        if (AssetBundleUtils.isAssetBundle(file)) {
            try (AssetBundleReader assetBundle = new AssetBundleReader(file)) {
                currentAssetBundle = assetBundle;
                handleAssetBundleFile(assetBundle);
                currentAssetBundle = null;
            }
        } else {
            AssetFile asset = new AssetFile();
            asset.load(file);
            handleAssetFile(asset);
        }
    }
    
    public void handleAssetBundleFile(AssetBundleReader assetBundle) throws IOException {
        for (AssetBundleEntry assetBundleEntry : assetBundle) {
            currentAssetBundleEntry = assetBundleEntry;
            L.log(Level.INFO, "Processing {0}", assetBundleEntry.name());
            handleAssetBundleEntry(assetBundleEntry);
            currentAssetBundleEntry = null;
        }
    }
    
    public void handleAssetBundleEntry(AssetBundleEntry assetBundleEntry) throws IOException {
        String name = assetBundleEntry.name();

        // skip libraries and resources
        if (assetBundleEntry.isLibrary() || assetBundleEntry.isResource()) {
            return;
        }
        
        // skip dummy asset from Unity3D Obfuscator
        if (name.equals("33Obf")) {
            return;
        }
        
        try (DataReader in = AssetBundleUtils.dataReaderForEntry(assetBundleEntry)) {
            AssetFile asset = new AssetFile();
            asset.load(in);
            
            // old asset files don't contain a Unity version string, so copy it
            // from the bundle header
            VersionInfo versionInfo = asset.versionInfo();
            if (versionInfo.assetVersion() <= 5) {
                versionInfo.unityRevision(getCurrentAssetBundle().header().unityRevision());
            }
            
            handleAssetFile(asset);
        }
    }
    
    public abstract void handleAssetFile(AssetFile asset) throws IOException;
}
