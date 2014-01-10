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
import info.ata4.unity.cli.action.Action;
import info.ata4.unity.cli.action.DumpAction;
import info.ata4.unity.cli.action.ExtractAction;
import info.ata4.unity.cli.action.FixReferencesAction;
import info.ata4.unity.cli.action.InfoAction;
import info.ata4.unity.cli.action.LearnAction;
import info.ata4.unity.cli.action.ListAction;
import info.ata4.unity.cli.action.SplitAction;
import info.ata4.unity.cli.action.StatsAction;
import info.ata4.unity.cli.action.UnbundleAction;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DisUnityProcessor implements Runnable {

    private static final Logger L = Logger.getLogger(DisUnityProcessor.class.getName());
    private static final Map<String, Action> COMMANDS;
    
    static {
        PrintStream out = System.out;
        Map<String, Action> commands = new HashMap<>();
        commands.put("dump", new DumpAction(out));
        commands.put("dump-struct", new DumpAction(out).setDumpStructs(true));
        commands.put("extract", new ExtractAction());
        commands.put("extract-raw", new ExtractAction().setRaw(true));
        commands.put("fixrefs", new FixReferencesAction());
        commands.put("info", new InfoAction(out));
        commands.put("info-stats", new StatsAction(out));
        commands.put("learn", new LearnAction());
        commands.put("list", new ListAction(out));
        commands.put("split", new SplitAction());
        commands.put("unbundle", new UnbundleAction());
        COMMANDS = Collections.unmodifiableMap(commands);
    }
    
    public static Set<String> getCommands() {
        return COMMANDS.keySet();
    }
    
    private final DisUnitySettings settings = new DisUnitySettings();
    private Action action;
    
    public DisUnitySettings getSettings() {
        return settings;
    }
    
    @Override
    public void run() {
        action = COMMANDS.get(settings.getCommand());
        if (action == null) {
            L.log(Level.SEVERE, "Invalid command: {0}", settings.getCommand());
            return;
        }
        
        action.setClassFilter(settings.getClassFilter());
        
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
                    if (action.supportsAssets()) {
                        processAsset(file);
                    } else {
                        L.log(Level.WARNING,
                                "Command \"{0}\" doesn''t support asset files, skipping {1}",
                                new Object[]{settings.getCommand(), file.getFileName()});
                    }
                }
            } catch (Exception ex) {
                L.log(Level.SEVERE, "Can't process " + file, ex);
            }
        }
        
        action.finished();
    }
    
    private void processAssetBundle(Path file) throws IOException {
        if (action.requiresOutputDir()) {
            // create target directory based on the asset bundle file name
            String fileName = FilenameUtils.getBaseName(file.getFileName().toString());
            Path outputDir = file.resolveSibling(fileName);

            if (!Files.exists(outputDir)) {
                Files.createDirectory(outputDir);
            }
            
            action.setOutputDir(outputDir);
        }
        
        AssetBundle ab = new AssetBundle();
        
        try {
            ab.load(file);
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't load " + file, ex);
            return;
        }
        
        if (action.supportsAssetBundes()) {
            try {
                action.processAssetBundle(ab);
            } catch (IOException ex) {
                L.log(Level.SEVERE, "Can't process " + file, ex);
                return;
            }
        }
        
        if (!action.supportsAssets()) {
            return;
        }
        
        if (action.requiresWriting()) {
            L.log(Level.WARNING,
                    "Command \"{0}\" doesn''t support assets in asset bundles, skipping {1}",
                    new Object[]{settings.getCommand(), file.getFileName()});
            return;
        }
        
        for (AssetBundleEntry entry : ab) {
            String name = entry.getName();

            // skip libraries
            if (name.endsWith(".dll")) {
                continue;
            }

            // skip dummy asset from Unity3D Obfuscator
            if (name.equals("33Obf")) {
                continue;
            }

            processAssetInBundle(entry);
        }
    }
    
    private void processAsset(Path file) throws IOException {
        if (action.requiresOutputDir()) {
            String fileName = file.getFileName().toString();
            String assetName = FilenameUtils.removeExtension(fileName);

            // if the file has no extension, append a "_" to the output directory
            // name so the file system won't have a file and dir with the same name
            if (FilenameUtils.getExtension(fileName).isEmpty()) {
                assetName += "_";
            }

            Path outputDir = file.resolveSibling(assetName);
            
            if (!Files.exists(outputDir)) {
                Files.createDirectory(outputDir);
            }
            
            action.setOutputDir(outputDir);
        }
        
        AssetFile asset = new AssetFile();

        try {
            // use memory mapping if the files aren't modified
            boolean mmap = !action.requiresWriting();
            asset.load(file, mmap);
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't load " + file, ex);
            return;
        }
        
        L.log(Level.INFO, "Processing {0}", file.getFileName());
        
        try {
            action.processAsset(asset);
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't process " + file, ex);
        }
    }
    
    private void processAssetInBundle(AssetBundleEntry entry) {
        String name = entry.getBundle().getSourceFile().getFileName() + ":" + entry.getName();
        String nameFull = entry.getBundle().getSourceFile() + ":" + entry.getName();
        AssetFile asset = new AssetFile();
        
        try {
            asset.load(entry.getByteBuffer());
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't load " + nameFull, ex);
            return;
        }
        
        asset.setSourceBundle(entry.getBundle());
        
        L.log(Level.INFO, "Processing {0}", name);
        
        try {
            action.processAsset(asset);
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't process " + name, ex);
        }
    }
}