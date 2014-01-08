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
import info.ata4.unity.assetbundle.AssetBundle;
import info.ata4.unity.assetbundle.AssetBundleEntry;
import static info.ata4.unity.cli.DisUnityCommand.DUMP;
import static info.ata4.unity.cli.DisUnityCommand.DUMP_STRUCT;
import info.ata4.unity.cli.extract.AssetExtractor;
import info.ata4.unity.cli.utils.AssetBundleUtils;
import info.ata4.unity.cli.utils.AssetDumper;
import info.ata4.unity.cli.utils.AssetUtils;
import info.ata4.unity.serdes.db.StructDatabase;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DisUnityProcessor implements Runnable {

    private static final Logger L = Logger.getLogger(DisUnityProcessor.class.getName());
    
    private final DisUnitySettings settings = new DisUnitySettings();
    
    public DisUnitySettings getSettings() {
        return settings;
    }
    
    private void processAsset(Path file, Path outputDir) {
        try {
            boolean map = settings.getCommand() != DisUnityCommand.FIXREFS;

            AssetFile asset = new AssetFile();
            asset.load(file, map);

            processAsset(asset, file.getFileName().toString(), outputDir);
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't open " + file, ex);
        }
    }
    
    private void processAsset(Path file) {
        String fileName = file.getFileName().toString();
        String assetName = FilenameUtils.removeExtension(fileName);
        
        // if the file has no extension, append a "_" to the output directory
        // name so the file system won't have a file and dir with the same name
        if (FilenameUtils.getExtension(fileName).isEmpty()) {
            assetName += "_";
        }
        
        Path outputDir = file.resolveSibling(assetName);
        processAsset(file, outputDir);
    }
    
    private void processAsset(ByteBuffer bb, String name, Path dir) {  
        try {
            AssetFile asset = new AssetFile();
            asset.load(bb);
            
            processAsset(asset, name, dir);
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't load " + name, ex);
        }
    }
    
    private void processAsset(AssetFile asset, String name, Path outputDir) {        
        try {
            DisUnityCommand cmd = settings.getCommand();
            switch (cmd) {
                case UNBUNDLE:
                    L.log(Level.WARNING, "Asset files can''t be unbundled, skipping {0}", name);
                    return;
                    
                case DUMP:
                case DUMP_STRUCT:
                    AssetDumper ad = new AssetDumper(System.out);
                    ad.setClassFilter(settings.getClassFilter());
                    if (cmd == DUMP) {
                        L.log(Level.INFO, "Dumping data from {0}", name);
                        ad.printData(asset);
                    } else {
                        L.log(Level.INFO, "Dumping structs from {0}", name);
                        ad.printStruct(asset);
                    }
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
                    
                case LIST:
                    L.log(Level.INFO, "Listing objects in {0}", name);
                    new AssetUtils(asset).list(System.out);
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

                    if (!Files.exists(outputDir)) {
                        Files.createDirectory(outputDir);
                    }
                    
                    AssetExtractor ae = new AssetExtractor(asset);
                    ae.setClassFilter(settings.getClassFilter());

                    if (split) {
                        ae.split(outputDir);
                    } else {
                        ae.extract(outputDir, raw);
                    }

                    break;
                    
                case FIXREFS:
                    L.log(Level.INFO, "Fixing asset references for {0}", name);
                    
                    // we need a file for this
                    if (asset.getSourceFile() == null) {
                        L.warning("Can't fix references of assets in asset bundles!");
                        break;
                    }
                    
                    new AssetUtils(asset).fixRefs();
                    break;
            }
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't process " + name, ex);
        }
    }

    private void processAssetBundle(Path file, Path dir) {
        AssetBundle ab = new AssetBundle();
        
        try {
            ab.load(file);
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't open " + file, ex);
            return;
        }
            
        try {
            DisUnityCommand cmd = settings.getCommand();
            switch (cmd) {
                case UNBUNDLE:
                    L.log(Level.INFO, "Extracting entries to {0}", dir);
                    new AssetBundleUtils(ab).extract(dir);
                    break;
                    
                case LIST:
                    L.log(Level.INFO, "Listing files in {0}", file.getFileName());
                    new AssetBundleUtils(ab).list(System.out);
                    break;
                    
                case INFO:
                    L.log(Level.INFO, "Printing information about {0}", file.getFileName());
                    new AssetBundleUtils(ab).printInfo(System.out);
                    
                default:
                    for (AssetBundleEntry entry : ab) {
                        String name = entry.getName();
                        
                        // skip libraries
                        if (name.endsWith(".dll")) {
                            continue;
                        }

                        // skip dummy asset from Unity3D Obfuscator
                        // TODO: random number?
                        if (name.equals("33Obf")) {
                            continue;
                        }

                        String assetName = file.getFileName() + ":" + entry.getName();
                        processAsset(entry.getByteBuffer(), assetName, dir);
                    }
            }
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't process " + file, ex);
        }
    }
    
    private void processAssetBundle(Path file) {
        // create target directory based on the asset bundle file name
        String fileName = FilenameUtils.getBaseName(file.getFileName().toString());
        Path dir = file.resolveSibling(fileName);
        processAssetBundle(file, dir);
    }
    
    @Override
    public void run() {
        for (Path file : settings.getFiles()) {
            if (!Files.exists(file)) {
                L.log(Level.WARNING, "File {0} doesn''t exist", file);
                continue;
            }
            
            if (Files.isDirectory(file)) {
                L.log(Level.WARNING, "File {0} is a directory", file);
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