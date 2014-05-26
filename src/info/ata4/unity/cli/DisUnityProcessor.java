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
import info.ata4.unity.asset.bundle.AssetBundle;
import info.ata4.unity.cli.action.Action;
import info.ata4.unity.cli.action.BundleExtractAction;
import info.ata4.unity.cli.action.BundleInjectAction;
import info.ata4.unity.cli.action.BundleListAction;
import info.ata4.unity.cli.action.DumpAction;
import info.ata4.unity.cli.action.ExtractAction;
import info.ata4.unity.cli.action.FixReferencesAction;
import info.ata4.unity.cli.action.InfoAction;
import info.ata4.unity.cli.action.LearnAction;
import info.ata4.unity.cli.action.ListAction;
import info.ata4.unity.cli.action.SplitAction;
import info.ata4.unity.cli.action.StatsAction;
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
 * DisUnity file processor.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DisUnityProcessor implements Runnable, FileVisitor<Path> {

    private static final Logger L = LogUtils.getLogger();
    private static final Map<String, Action> COMMANDS;
    
    static {
        PrintStream out = System.out;
        Map<String, Action> commands = new HashMap<>();
        commands.put("dump", new DumpAction().setDumpToFiles(false));
        commands.put("dump-struct", new DumpAction().setDumpToFiles(false).setDumpStructs(true));
        commands.put("extract", new ExtractAction());
        commands.put("extract-raw", new ExtractAction().setRaw(true));
        commands.put("extract-txt", new DumpAction());
        commands.put("extract-struct", new DumpAction().setDumpStructs(true));
        commands.put("fixrefs", new FixReferencesAction());
        commands.put("info", new InfoAction(out));
        commands.put("info-stats", new StatsAction(out));
        commands.put("learn", new LearnAction());
        commands.put("list", new ListAction(out));
        commands.put("split", new SplitAction());
        commands.put("bundle-extract", new BundleExtractAction());
        commands.put("bundle-inject", new BundleInjectAction());
        commands.put("bundle-list", new BundleListAction(out));
        COMMANDS = Collections.unmodifiableMap(commands);
    }
    
    public static Set<String> getCommands() {
        return COMMANDS.keySet();
    }
    
    private final DisUnityOptions opts;
    private Action action;

    public DisUnityProcessor(DisUnityOptions opts) {
        this.opts = opts;
    }
    
    @Override
    public void run() {
        // parse command name
        action = COMMANDS.get(opts.getCommand());
        if (action == null) {
            L.log(Level.SEVERE, "Invalid command: {0}", opts.getCommand());
            return;
        }
        
        // set options for action
        action.setOptions(opts);
        
        // process submitted files
        for (Path file : opts.getFiles()) {
            // skip non-existent files
            if (Files.notExists(file)) {
                L.log(Level.WARNING, "File {0} doesn''t exist", file);
                continue;
            }
            
            if (Files.isDirectory(file)) {
                // search directory recursively for asset and asset bundle files
                try {
                    Files.walkFileTree(file, this);
                } catch (Exception ex) {
                    L.log(Level.SEVERE, "Can't search directory " + file, ex);
                }
            } else {
                // process file as asset or asset bundle
                try {
                    if (AssetBundle.isAssetBundle(file)) {
                        processAssetBundle(file);
                    } else {
                        if (action.supportsAssets()) {
                            processAsset(file);
                        } else {
                            L.log(Level.WARNING,
                                    "Command \"{0}\" doesn''t support asset files, skipping {1}",
                                    new Object[]{opts.getCommand(), file.getFileName()});
                        }
                    }
                } catch (Exception ex) {
                    L.log(Level.SEVERE, "Can't process " + file, ex);
                }
            }
        }
        
        // signal end of processing
        action.finished();
    }
    
    private void processAssetBundle(Path file) throws IOException {
        L.log(Level.INFO, "Processing {0}", file.getFileName());
        
        Path outputDir = null;
        if (action.requiresOutputDir()) {
            // create target directory based on the asset bundle file name
            String fileName = FilenameUtils.getBaseName(file.getFileName().toString());
            outputDir = file.resolveSibling(fileName);

            if (Files.notExists(outputDir)) {
                Files.createDirectory(outputDir);
            }
            
            action.setOutputDir(outputDir);
        }
        
        // load asset bundle
        AssetBundle ab = new AssetBundle();
        if (action.requiresWriting()) {
            ab.load(file);
        } else {
            ab.open(file);
        }
        
        // process asset bundle
        if (action.supportsAssetBundes()) {
            action.processAssetBundle(ab);
        }
        
        // skip processing of asset files if not supported
        if (!action.supportsAssets()) {
            return;
        }
        
        if (action.requiresWriting()) {
            L.log(Level.WARNING,
                    "Command \"{0}\" can't edit assets in asset bundles, skipping {1}",
                    new Object[]{opts.getCommand(), file.getFileName()});
            return;
        }
        
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
            
            ByteBuffer bb = entry.getValue();
            
            // reset output dir so it can be resolved in processAssetInBundle
            if (outputDir != null) {
                action.setOutputDir(outputDir);
            }
            
            try {
                processAssetInBundle(ab, name, bb);
            } catch (IOException ex) {
                L.log(Level.SEVERE, "Can't process " + file + ":" + name, ex);
            }
        }
    }
    
    private void processAsset(Path file) throws IOException {
        L.log(Level.INFO, "Processing {0}", file.getFileName());
        
        if (action.requiresOutputDir()) {
            String fileName = file.getFileName().toString();
            String assetName = FilenameUtils.removeExtension(fileName);
            
            // remove extension twice if it's a .assets.splitN file
            if (FilenameUtils.getExtension(fileName).startsWith("split")) {
                assetName = FilenameUtils.removeExtension(assetName);
            }

            // if the file has no extension, append a "_" to the output directory
            // name so the file system won't have a file and dir with the same name
            if (FilenameUtils.getExtension(fileName).isEmpty()) {
                assetName += "_";
            }

            Path outputDir = file.resolveSibling(assetName);
            
            if (Files.notExists(outputDir)) {
                Files.createDirectory(outputDir);
            }
            
            action.setOutputDir(outputDir);
        }
        
        AssetFile asset = new AssetFile();

        // use memory mapping if the files aren't modified
        if (action.requiresWriting()) {
            asset.load(file);
        } else {
            asset.open(file);
        }

        action.processAsset(asset);
    }
    
    private void processAssetInBundle(AssetBundle ab, String name, ByteBuffer bb) throws IOException {
        L.log(Level.INFO, "Processing {0}:{1}", new Object[] {ab.getSourceFile().getFileName(), name});
        
        // use sub directory based on the asset name
        if (action.requiresOutputDir()) {
            String assetName = FilenameUtils.removeExtension(name);
            Path outputDir = action.getOutputDir().resolve(assetName);
            
            if (Files.notExists(outputDir)) {
                Files.createDirectories(outputDir);
            }
            
            action.setOutputDir(outputDir);
        }
        
        AssetFile asset = new AssetFile();
        asset.load(bb);
        asset.setSourceBundle(ab);

        action.processAsset(asset);
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