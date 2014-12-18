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

import info.ata4.io.socket.IOSocket;
import info.ata4.log.LogUtils;
import info.ata4.unity.asset.AssetFile;
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

    @Override
    public void handleFile(Path file) throws IOException {
        if (AssetBundleUtils.isAssetBundle(file)) {
            try (AssetBundleReader assetBundle = new AssetBundleReader(file)) {
                handleAssetBundleFile(assetBundle);
            }
        } else {
            AssetFile asset = new AssetFile();
            asset.load(file);
            handleAssetFile(asset);
        }
    }
    
    public void handleAssetBundleFile(AssetBundleReader assetBundle) throws IOException {
        for (AssetBundleEntry assetBundleEntry : assetBundle) {
            L.log(Level.INFO, "Processing {0}", assetBundleEntry.getName());
            handleAssetBundleEntry(assetBundleEntry);
        }
    }
    
    public void handleAssetBundleEntry(AssetBundleEntry assetBundleEntry) throws IOException {
        String name = assetBundleEntry.getName();

        // skip libraries
        if (assetBundleEntry.isLibrary()) {
            return;
        }
        
        // skip dummy asset from Unity3D Obfuscator
        if (name.equals("33Obf")) {
            return;
        }

        try (IOSocket socket = AssetBundleUtils.getSocketForEntry(assetBundleEntry)) {
            AssetFile asset = new AssetFile();
            asset.load(socket);
            
            handleAssetFile(asset);
        }
    }
    
    public abstract void handleAssetFile(AssetFile asset) throws IOException;
}
