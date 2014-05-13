/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.extract;

import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.log.LogUtils;
import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.asset.struct.AssetClassType;
import info.ata4.unity.asset.struct.AssetObjectPath;
import info.ata4.unity.cli.classfilter.ClassFilter;
import info.ata4.unity.cli.extract.handler.AudioClipHandler;
import info.ata4.unity.cli.extract.handler.FontHandler;
import info.ata4.unity.cli.extract.handler.MeshHandler;
import info.ata4.unity.cli.extract.handler.MovieTextureHandler;
import info.ata4.unity.cli.extract.handler.SubstanceArchiveHandler;
import info.ata4.unity.cli.extract.handler.TextAssetHandler;
import info.ata4.unity.cli.extract.handler.Texture2DHandler;
import info.ata4.unity.serdes.DeserializationException;
import info.ata4.unity.serdes.Deserializer;
import info.ata4.unity.serdes.UnityObject;
import info.ata4.unity.util.ClassID;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Extractor for asset files.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetExtractor {
    
    private static final Logger L = LogUtils.getLogger();
    
    /**
     * Tries to get the name of an object by deserializing it and looking for
     * the field "m_Name". If it exists, return its value.
     * 
     * @param asset asset file
     * @param path object path
     * @return Name string of the object or null if it doesn't have a name or if
     *         the deserialization failed.
     */
    public static String getObjectName(AssetFile asset, AssetObjectPath path) {
        Deserializer deser = new Deserializer(asset);
        String name = null;
        
        try {
            UnityObject obj = deser.deserialize(path);
            name = obj.getValue("m_Name");
        } catch (OutOfMemoryError ex) {
            // Deserializer choked on an array size and clogged the heap, try
            // to clean up this mess
            deser = null;
            System.gc();
        } catch (Throwable ex) {
        }
        
        return name;
    }
    
    private final AssetFile asset;
    private final Map<String, AssetExtractHandler> extractHandlerMap = new HashMap<>();
    private ClassFilter cf;
    private Path outputDir;
    
    public AssetExtractor(AssetFile asset) {
        this.asset = asset;
        
        addHandler("AudioClip", new AudioClipHandler());
        addHandler("Shader", new TextAssetHandler("shader"));
        addHandler("SubstanceArchive", new SubstanceArchiveHandler());
        addHandler("Texture2D", new Texture2DHandler());
        addHandler("Cubemap", new Texture2DHandler());
        addHandler("Font", new FontHandler());
        addHandler("TextAsset", new TextAssetHandler("txt"));
        addHandler("MovieTexture", new MovieTextureHandler());
        addHandler("Mesh", new MeshHandler());
    }
    
    public final void addHandler(String className, AssetExtractHandler handler) {
        handler.setClassName(className);
        extractHandlerMap.put(className, handler);
    }
    
    public final AssetExtractHandler getHandler(String className) {
        return extractHandlerMap.get(className);
    }
    
    public final void clearHandlers() {
        extractHandlerMap.clear();
    }
    
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

    public void extract(boolean raw) throws IOException {
        List<AssetObjectPath> paths = asset.getPaths();
        Deserializer deser = new Deserializer(asset);

        for (AssetExtractHandler extractHandler : extractHandlerMap.values()) {
            extractHandler.setAssetFile(asset);
            extractHandler.setExtractDir(outputDir);
        }
        
        for (AssetObjectPath path : paths) {
            // skip filtered classes
            if (cf != null && !cf.accept(path)) {
                continue;
            }
            
            String className = ClassID.getNameForID(path.getClassID(), true);

            // write just the serialized object data or parsed and extracted content?
            if (raw) {
                String assetFileName = String.format("%06d.bin", path.getPathID());
                Path classDir = outputDir.resolve(className);
                if (!Files.exists(classDir)) {
                    Files.createDirectory(classDir);
                }
                
                Path assetFile = classDir.resolve(assetFileName);
                
                L.log(Level.INFO, "Writing {0} {1}", new Object[] {className, assetFileName});
                
                ByteBuffer bbAsset = asset.getPathBuffer(path);
                
                try {
                    ByteBufferUtils.save(assetFile, bbAsset);
                } catch (Exception ex) {
                    L.log(Level.WARNING, "Can't write " + path + " to " + assetFile, ex);
                }
            } else {
                AssetExtractHandler handler = getHandler(className);
                
                if (handler != null) {
                    try {
                        UnityObject obj = deser.deserialize(path);
                        handler.extract(path, obj);
                    } catch (DeserializationException ex) {
                        L.log(Level.WARNING, "Can't deserialize " + path, ex);
                    } catch (IOException ex) {
                        L.log(Level.WARNING, "Can't read or write " + path, ex);
                    } catch (Exception ex) {
                        L.log(Level.WARNING, "Can't extract " + path, ex);
                    }
                }
            }
        }

        // delete directory if empty
        boolean empty;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(outputDir)) {
            empty = !ds.iterator().hasNext();
        }
        if (empty) {
            Files.delete(outputDir);
        }
    }

    public void split() throws IOException {
        List<AssetObjectPath> pathTable = asset.getPaths();
        AssetClassType classType = asset.getClassType();
        ByteBuffer bb = asset.getDataBuffer();
        
        // assets with just one object can't be split any further
        if (pathTable.size() == 1) {
            L.warning("Asset doesn't contain sub-assets!");
            return;
        }
        
        for (AssetObjectPath path : pathTable) {
            // skip filtered classes
            if (cf != null && !cf.accept(path)) {
                continue;
            }

            String className = ClassID.getNameForID(path.getClassID(), true);
            
            AssetFile subAsset = new AssetFile();
            subAsset.getHeader().setFormat(asset.getHeader().getFormat());
            
            AssetObjectPath subFieldPath = new AssetObjectPath();
            subFieldPath.setClassID1(path.getClassID1());
            subFieldPath.setClassID2(path.getClassID2());
            subFieldPath.setLength(path.getLength());
            subFieldPath.setOffset(0);
            subFieldPath.setPathID(1);
            subAsset.getPaths().add(subFieldPath);
            
            AssetClassType subClassType = subAsset.getClassType();
            subClassType.setEngineVersion(classType.getEngineVersion());
            subClassType.setVersion(-2);
            subClassType.setFormat(classType.getFormat());
            subClassType.getTypeTree().put(path.getClassID(), classType.getTypeTree().get(path.getClassID()));

            // create a byte buffer for the data area
            ByteBuffer bbAsset = ByteBufferUtils.getSlice(bb, path.getOffset(), path.getLength());
            subAsset.setDataBuffer(bbAsset);
            
            Path subAssetDir = outputDir.resolve(className);
            if (!Files.exists(subAssetDir)) {
                Files.createDirectory(subAssetDir);
            }
            
            // probe asset name
            String subAssetName = getObjectName(asset, path);
            if (subAssetName != null) {
                // remove any chars that could cause troubles on various file systems
                subAssetName = subAssetName.replaceAll("[^a-zA-Z0-9\\._]+", "_");
            } else {
                // probably can't exist in a standalone file anyway
                continue;
            }
            subAssetName += ".asset";
            
            Path subAssetFile = subAssetDir.resolve(subAssetName);
            if (Files.exists(subAssetFile)) {
                L.log(Level.INFO, "Writing {0}", subAssetFile);
                subAsset.save(subAssetFile);
            }
        }
    }
}
