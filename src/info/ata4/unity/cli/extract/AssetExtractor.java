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

import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.asset.AssetFormat;
import info.ata4.unity.asset.struct.AssetHeader;
import info.ata4.unity.asset.struct.AssetObjectPath;
import info.ata4.unity.asset.struct.AssetObjectPathTable;
import info.ata4.unity.asset.struct.AssetTypeTree;
import info.ata4.unity.cli.classfilter.ClassFilter;
import info.ata4.unity.cli.extract.handler.*;
import info.ata4.unity.serdes.DeserializationException;
import info.ata4.unity.serdes.Deserializer;
import info.ata4.unity.serdes.UnityObject;
import info.ata4.unity.util.ClassID;
import info.ata4.util.io.ByteBufferUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 * Extractor for asset files.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetExtractor {
    
    private static final Logger L = Logger.getLogger(AssetExtractor.class.getName());
    
    public static String getAssetName(ByteBuffer bb) {
        try {
            // make sure we have enough bytes to read a string at all
            if (bb.capacity() < 5) {
                L.log(Level.FINEST, "Not enough data for an asset name");
                return null;
            }
            
            int len = bb.getInt();
            if (len > 1024) {
                L.log(Level.FINEST, "Asset name too long: {0}", len);
                return null;
            }
            
            byte[] raw = new byte[len];
            bb.get(raw);
            
            String assetName = new String(raw).trim();
            
            // ignore bad strings
            if (assetName.isEmpty() || !StringUtils.isAsciiPrintable(assetName)) {
                L.log(Level.FINEST, "Invalid/empty asset name");
                return null;
            }
            
            return assetName;
        } catch (Exception ex) {
            return null;
        }
    }
    
    private final AssetFile asset;
    private ClassFilter cf;
    
    private Map<String, AssetExtractHandler> extractHandlerMap = new HashMap<>();
    
    public AssetExtractor(AssetFile asset) {
        this.asset = asset;
        
        addHandler("AudioClip", new AudioClipHandler());
        addHandler("Shader", new TextAssetHandler("shader"));
        addHandler("SubstanceArchive", new SubstanceArchiveHandler());
        addHandler("Texture2D", new Texture2DHandler());
        addHandler("Cubemap", new CubemapHandler());
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
    
    public ClassFilter getClassFilter() {
        return cf;
    }

    public void setClassFilter(ClassFilter cf) {
        this.cf = cf;
    }

    public void extract(File dir, boolean raw) throws IOException {
        AssetHeader header = asset.getHeader();
        AssetTypeTree typeTree = asset.getTypeTree();
        AssetObjectPathTable pathTable = asset.getObjectPaths();
        
        Deserializer deser = new Deserializer(asset);
        AssetFormat format = new AssetFormat(typeTree.version, typeTree.revision, header.format);
        
        for (AssetExtractHandler extractHandler : extractHandlerMap.values()) {
            extractHandler.setExtractDir(dir);
            extractHandler.setAssetFormat(format);
            
            // set external audio buffer for AudioClips
            if (extractHandler instanceof AudioClipHandler) {
                ((AudioClipHandler) extractHandler).setAudioBuffer(asset.getAudioBuffer());
            }
        }
        
        for (AssetObjectPath path : pathTable) {
            // skip filtered classes
            if (cf != null && !cf.accept(path)) {
                continue;
            }
            
            String className = ClassID.getNameForID(path.classID2, true);
            String objectName = String.format("Object #%d (ClassID: %d, Class: %s)", path.pathID, path.classID2, className);

            // write just the serialized object data or parsed and extracted content?
            if (raw) {
                String assetFileName = String.format("%06d.bin", path.pathID);
                File classDir = new File(dir, className);
                File assetFile = new File(classDir, assetFileName);
                
                if (!classDir.exists()) {
                    classDir.mkdir();
                }
                
                L.log(Level.INFO, "Writing {0} {1}", new Object[] {className, assetFileName});
                
                try (FileOutputStream os = new FileOutputStream(assetFile)) {
                    ByteBuffer bbAssets = asset.getDataBuffer();
                    ByteBuffer bbAsset = ByteBufferUtils.getSlice(bbAssets, path.offset, path.length);

                    os.getChannel().write(bbAsset);
                } catch (Exception ex) {
                    L.log(Level.WARNING, "Can't write " + objectName + " to " + assetFile, ex);
                }
            } else {
                AssetExtractHandler handler = getHandler(className);
                
                if (handler != null) {
                    try {
                        UnityObject obj = deser.deserialize(path);
                        handler.extract(path, obj);
                    } catch (DeserializationException ex) {
                        L.log(Level.WARNING, "Can't deserialize " + objectName, ex);
                    } catch (IOException ex) {
                        L.log(Level.WARNING, "Can't read or write " + objectName, ex);
                    } catch (Exception ex) {
                        L.log(Level.WARNING, "Can't extract " + objectName, ex);
                    }
                }
            }
        }

        // delete directory if empty
        if (dir.list().length == 0) {
            dir.delete();
        }
    }

    public void split(File dir) throws IOException {
        AssetObjectPathTable pathTable = asset.getObjectPaths();
        AssetTypeTree typeTree = asset.getTypeTree();
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

            String className = ClassID.getNameForID(path.classID2, true);
            
            AssetFile subAsset = new AssetFile();
            subAsset.getHeader().format = asset.getHeader().format;
            
            AssetObjectPath subFieldPath = new AssetObjectPath();
            subFieldPath.classID1 = path.classID1;
            subFieldPath.classID2 = path.classID2;
            subFieldPath.length = path.length;
            subFieldPath.offset = 0;
            subFieldPath.pathID = 1;
            subAsset.getObjectPaths().add(subFieldPath);
            
            AssetTypeTree subTypeTree = subAsset.getTypeTree();
            subTypeTree.revision = typeTree.revision;
            subTypeTree.version = -2;
            subTypeTree.setFormat(typeTree.getFormat());
            subTypeTree.put(path.classID2, typeTree.get(path.classID2));
            
            // create a byte buffer for the data area
            ByteBuffer bbAsset = ByteBufferUtils.getSlice(bb, path.offset, path.length);
            bbAsset.order(ByteOrder.LITTLE_ENDIAN);
            
            // probe asset name
            String subAssetName = getAssetName(bbAsset);
            bbAsset.rewind();
            
            if (subAssetName == null) {
                continue;
            } else {
                // remove any chars that could cause troubles on various file systems
                subAssetName = subAssetName.replaceAll("[^a-zA-Z0-9\\._]+", "_");
            }
            
            subAsset.setDataBuffer(bbAsset);
            
            File subAssetDir = new File(dir, className);
            if (!subAssetDir.exists()) {
                subAssetDir.mkdir();
            }
            
            File subAssetFile = new File(subAssetDir, subAssetName + ".asset");
            if (!subAssetFile.exists()) {
                L.log(Level.INFO, "Writing {0}", subAssetFile);
                subAsset.save(subAssetFile);
            }
        }
    }
}
