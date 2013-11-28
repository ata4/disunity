/*
 ** 2013 November 18
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli;

import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.asset.AssetFileFilter;
import info.ata4.unity.asset.struct.AssetRef;
import info.ata4.unity.assetbundle.AssetBundle;
import info.ata4.unity.assetbundle.AssetBundleEntry;
import info.ata4.unity.cli.extract.AssetExtractor;
import info.ata4.unity.cli.utils.AssetDumper;
import info.ata4.unity.cli.utils.AssetUtils;
import info.ata4.unity.serdes.struct.StructDatabase;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DisUnityProcessor implements Runnable {

    private static final Logger L = Logger.getLogger(DisUnityProcessor.class.getName());
    
    private final AssetFileFilter assetFilter = new AssetFileFilter();
    private final DisUnitySettings settings = new DisUnitySettings();
    
    public DisUnitySettings getSettings() {
        return settings;
    }

    public boolean isAsset(String fileName) {
        return assetFilter.accept(null, fileName);
    }
    
    public boolean isAsset(File file) {
        return assetFilter.accept(file.getParentFile(), file.getName());
    }
    
    private void processAsset(File file, File dir) {
        try {
            boolean map = settings.getCommand() != DisUnityCommand.FIXREFS;
            
            AssetFile asset = new AssetFile();
            asset.load(file, map);
            
            processAsset(asset, file.getName(), file, dir);
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't open " + file, ex);
        }
    }
    
    private void processAsset(File file) {
        String fileName = FilenameUtils.getBaseName(file.getName());
        if (FilenameUtils.getExtension(file.getName()).isEmpty()) {
            fileName += "_";
        }
        File dir = new File(file.getParentFile(), fileName);
        processAsset(file, dir);
    }
    
    private void processAsset(ByteBuffer bb, String name, File dir) {  
        try {
            AssetFile asset = new AssetFile();
            asset.load(bb);
            
            processAsset(asset, name, null, dir);
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't load " + name, ex);
        }
    }
    
    private void processAsset(AssetFile asset, String name, File sourceFile, File dir) {        
        try {
            DisUnityCommand cmd = settings.getCommand();
            switch (cmd) {
                case UNBUNDLE:
                    L.log(Level.WARNING, "Asset files can''t be unbundled, skipping {0}", name);
                    return;
                    
                case DUMP:
                    L.log(Level.INFO, "Dumping data from {0}", name);
                    new AssetDumper(asset, settings).dumpData(System.out);
                    break;
                
                case DUMP_STRUCT:
                    L.log(Level.INFO, "Dumping structs from {0}", name);
                    new AssetDumper(asset, settings).dumpStruct(System.out);
                    break;
                    
                case LEARN:
                    L.log(Level.INFO, "Learning structs from {0}", name);
                    new AssetUtils(asset).learnStruct();
                    break;
                    
                case INFO:
                    L.log(Level.INFO, "Printing information for {0}", name);
                    new AssetUtils(asset).printInfo(System.out);
                    break;
                    
                case INFO_STATS:
                    L.log(Level.INFO, "Printing class stats for {0}", name);
                    new AssetUtils(asset).printStats(System.out);
                    break;
                    
                case EXTRACT:
                case EXTRACT_RAW:
                case SPLIT:
                    boolean split = cmd == DisUnityCommand.SPLIT;
                    boolean raw = cmd == DisUnityCommand.EXTRACT_RAW;

                    if (split) {
                        L.log(Level.INFO, "Splitting assets from {0}", name);
                    } else {
                        L.log(Level.INFO, "Extracting resources from {0}", name);
                    }

                    FileUtils.forceMkdir(dir);
                    AssetExtractor ae = new AssetExtractor(asset, settings);

                    if (split) {
                        ae.split(dir);
                    } else {
                        ae.extract(dir, raw);
                    }

                    break;
                    
                case FIXREFS:
                    L.log(Level.INFO, "Fixing asset references for {0}", name);
                    
                    // we need a file for this
                    if (sourceFile == null) {
                        L.warning("Can't fix references of assets in asset bundles!");
                        break;
                    }
                    
                    String fixedPath = sourceFile.getParent().replace("\\", "/").toLowerCase();
                    
                    for (AssetRef ref : asset.getReferences()) {
                        if (isAsset(ref.filePath)) {
                            String pathOld = ref.filePath;
                            ref.filePath = fixedPath + "/" + FilenameUtils.getName(ref.filePath);
                            L.log(Level.FINE, "Fixing ref: {0} -> {1}", new Object[]{pathOld, ref.filePath});
                        }
                    }

                    asset.save(sourceFile);
                    
                    break;
            }
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't read " + name, ex);
        }
    }

    private void processAssetBundle(File file, File dir) {
        try {
            AssetBundle ab = new AssetBundle();
            ab.load(file);
            
            DisUnityCommand cmd = settings.getCommand();
            if (cmd == DisUnityCommand.UNBUNDLE) {
                FileUtils.forceMkdir(dir);
                ab.extract(dir);
            } else {
                // may take a while to decompress it in-memory
                if (ab.isCompressed()) {
                    L.log(Level.INFO, "Uncompressing {0}", file.getName());
                }
                
                if (cmd == DisUnityCommand.INFO) {
                    L.log(Level.INFO, "Printing information about {0}", file.getName());
                    System.out.println("File version: " + ab.getFileVersion());
                    System.out.println("Version: " + ab.getVersion());
                    System.out.println("Revision: " + ab.getRevision());
                    System.out.println("Compressed: " + (ab.isCompressed() ? "yes" : "no"));
                    System.out.println("Entries: " + ab.getEntries().size());
                    System.out.println();
                }
                
                for (AssetBundleEntry entry : ab) {
                    // skip non-asset entries
                    if (!isAsset(entry.getName())) {
                        continue;
                    }
                    
                    // skip dummy asset from Unity3D Obfuscator
                    // TODO: random number?
                    if (entry.getName().equals("33Obf")) {
                        continue;
                    }

                    String assetName = file.getName() + ":" + entry.getName();
                    processAsset(entry.getByteBuffer(), assetName, dir);
                }
            }
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't open " + file, ex);
        }
    }
    
    private void processAssetBundle(File file) {
        // create target directory based on the asset bundle file name
        String fileName = FilenameUtils.getBaseName(file.getName());
        File dir = new File(file.getParentFile(), fileName);
        processAssetBundle(file, dir);
    }
    
    
    @Override
    public void run() {
        for (File file : settings.getFiles()) {
            if (!file.exists()) {
                L.log(Level.WARNING, "File {0} doesn''t exist", file);
                continue;
            }
            
            if (file.isDirectory()) {
                L.log(Level.WARNING, "File {0} exists, but is a directory", file);
                continue;
            }
            
            if (!file.canRead()) {
                L.log(Level.WARNING, "File {0} isn''t readable", file);
                continue;
            }
            
            try {
                if (AssetBundle.isAssetBundle(file)) {
                    processAssetBundle(file);
                } else {
                    processAsset(file);
                }
            } catch (Exception ex) {
                L.log(Level.SEVERE, "Can't process " + file, ex);
            }
        }
        
        // update database after learning
        if (settings.getCommand() == DisUnityCommand.LEARN) {
            StructDatabase.getInstance().update();
        }
    }
}