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

import info.ata4.log.LogUtils;
import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.assetbundle.AssetBundle;
import info.ata4.unity.cli.action.Action;
import info.ata4.unity.cli.action.DumpAction;
import info.ata4.unity.cli.action.ExtractAction;
import info.ata4.unity.cli.action.FixReferencesAction;
import info.ata4.unity.cli.action.InfoAction;
import info.ata4.unity.cli.action.LearnAction;
import info.ata4.unity.cli.action.ListAction;
import info.ata4.unity.cli.action.SplitAction;
import info.ata4.unity.cli.action.StatsAction;
import info.ata4.unity.cli.action.BundleExtractAction;
import info.ata4.unity.cli.action.BundleListAction;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
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
public class DisUnityProcessor implements Runnable, FileVisitor<Path> {

    private static final Logger L = LogUtils.getLogger();
    private static final Map<String, Action> COMMANDS;
    
    static {
        PrintStream out = System.out;
        Map<String, Action> commands = new HashMap<>();
        commands.put("dump", new DumpAction());
        commands.put("dump-struct", new DumpAction().setDumpStructs(true));
        commands.put("extract", new ExtractAction());
        commands.put("extract-raw", new ExtractAction().setRaw(true));
        commands.put("fixrefs", new FixReferencesAction());
        commands.put("info", new InfoAction(out));
        commands.put("info-stats", new StatsAction(out));
        commands.put("learn", new LearnAction());
        commands.put("list", new ListAction(out));
        commands.put("split", new SplitAction());
        commands.put("bundle-extract", new BundleExtractAction());
        commands.put("bundle-list", new BundleListAction(out));
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
                try {
                    Files.walkFileTree(file, this);
                } catch (Exception ex) {
                    L.log(Level.SEVERE, "Can't search directory " + file, ex);
                }
            } else {
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
        }
        
        action.finished();
    }
    
    private void processAssetBundle(Path file) throws IOException {
        Path outputDir = null;
        if (action.requiresOutputDir()) {
            // create target directory based on the asset bundle file name
            String fileName = FilenameUtils.getBaseName(file.getFileName().toString());
            outputDir = file.resolveSibling(fileName);

            if (!Files.exists(outputDir)) {
                Files.createDirectory(outputDir);
            }
            
            action.setOutputDir(outputDir);
        }
        
        AssetBundle ab = new AssetBundle();
        
        try {
            ab.open(file);
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
            
            ByteBuffer bb = entry.getValue();
            
            // reset output dir so it can be resolved in processAssetInBundle
            if (outputDir != null) {
                action.setOutputDir(outputDir);
            }

            processAssetInBundle(ab, name, bb);
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
            if (action.requiresWriting()) {
                asset.load(file);
            } else {
                asset.open(file);
            }
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
    
    private void processAssetInBundle(AssetBundle ab, String name, ByteBuffer bb) throws IOException {
        // use sub directory based on the asset name
        if (action.requiresOutputDir()) {
            String assetName = FilenameUtils.removeExtension(name);
            Path outputDir = action.getOutputDir().resolve(assetName);
            
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }
            
            action.setOutputDir(outputDir);
        }
        
        String filePath = ab.getSourceFile() + ":" + name;
        String fileName = ab.getSourceFile().getFileName() + ":" + name;
        AssetFile asset = new AssetFile();
        
        try {
            asset.load(bb);
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't load " + filePath, ex);
            return;
        }
        
        asset.setSourceBundle(ab);
        
        L.log(Level.INFO, "Processing {0}", fileName);
        
        try {
            action.processAsset(asset);
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't process " + filePath, ex);
        }
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (AssetBundle.isAssetBundle(file)) {
            processAssetBundle(file);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        L.log(Level.SEVERE, "Can't process " + file, exc);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
}