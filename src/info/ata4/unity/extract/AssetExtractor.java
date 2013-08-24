/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.extract;

import info.ata4.unity.DisUnitySettings;
import info.ata4.unity.asset.Asset;
import info.ata4.unity.asset.AssetFormat;
import info.ata4.unity.extract.handler.AudioClipHandler;
import info.ata4.unity.extract.handler.CubemapHandler;
import info.ata4.unity.extract.handler.ExtractHandler;
import info.ata4.unity.extract.handler.FontHandler;
import info.ata4.unity.extract.handler.MovieTextureHandler;
import info.ata4.unity.extract.handler.ShaderHandler;
import info.ata4.unity.extract.handler.SubstanceArchiveHandler;
import info.ata4.unity.extract.handler.TextAssetHandler;
import info.ata4.unity.extract.handler.Texture2DHandler;
import info.ata4.unity.serdes.DeserializationException;
import info.ata4.unity.serdes.Deserializer;
import info.ata4.unity.serdes.UnityObject;
import info.ata4.unity.struct.AssetHeader;
import info.ata4.unity.struct.TypeTree;
import info.ata4.unity.struct.ObjectPath;
import info.ata4.unity.struct.ObjectPathTable;
import info.ata4.unity.util.ClassID;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
    
    private final Asset asset;
    private final DisUnitySettings settings;
    
    private Map<String, ExtractHandler> extractHandlerMap = new HashMap<>();
    private Set<ExtractHandler> extractHandlerSet = new HashSet<>();
    
    public AssetExtractor(Asset asset, DisUnitySettings settings) {
        this.asset = asset;
        this.settings = settings;
        
        addExtractHandler(new AudioClipHandler());
        addExtractHandler(new ShaderHandler());
        addExtractHandler(new SubstanceArchiveHandler());
        addExtractHandler(new Texture2DHandler());
        addExtractHandler(new CubemapHandler());
        addExtractHandler(new FontHandler());
        addExtractHandler(new TextAssetHandler());
        addExtractHandler(new MovieTextureHandler());
    }
    
    public final void addExtractHandler(ExtractHandler handler) {
        extractHandlerMap.put(handler.getClassName(), handler);
        extractHandlerSet.add(handler);
    }
    
    public final ExtractHandler getExtractHandler(String className) {
        return extractHandlerMap.get(className);
    }
    
    public final void clearExtractHandlers() {
        extractHandlerMap.clear();
        extractHandlerSet.clear();
    }

    public void extract(File dir, boolean raw) throws IOException {
        AssetHeader header = asset.getHeader();
        TypeTree typeTree = asset.getTypeTree();
        ObjectPathTable pathTable = asset.getObjectPaths();
        
        
        Deserializer deser = new Deserializer(asset);
        AssetFormat format = new AssetFormat(typeTree.version, typeTree.revision, header.format);
        
        for (ExtractHandler extractHandler : extractHandlerSet) {
            extractHandler.setExtractDir(dir);
            extractHandler.setAssetFormat(format);
        }
        
        for (ObjectPath path : pathTable) {
            // skip filtered classes
            if (settings.isClassFiltered(path.classID2)) {
                continue;
            }
            
            String className = ClassID.getInstance().getNameForID(path.classID2, true);
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
                
                try (
                    FileOutputStream os = new FileOutputStream(assetFile)
                ) {
                    ByteBuffer bb = asset.getDataBuffer();
                    bb.position(path.offset);
                    
                    ByteBuffer bbAsset = bb.slice();
                    bbAsset.limit(path.length);

                    os.getChannel().write(bbAsset);
                } catch (Exception ex) {
                    L.log(Level.WARNING, "Can't write " + objectName + " to " + assetFile, ex);
                }
            } else {
                ExtractHandler handler = getExtractHandler(className);
                
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
    }

    public void split(File dir) throws IOException {
        ObjectPathTable pathTable = asset.getObjectPaths();
        TypeTree typeTree = asset.getTypeTree();
        ByteBuffer bb = asset.getDataBuffer();
        
        // assets with just one object can't be split any further
        if (pathTable.size() == 1) {
            L.warning("Asset doesn't contain sub-assets!");
            return;
        }
        
        for (ObjectPath path : pathTable) {
            // skip filtered classes
            if (settings.isClassFiltered(path.classID2)) {
                continue;
            }

            String className = ClassID.getInstance().getNameForID(path.classID2, true);
            
            Asset subAsset = new Asset();
            subAsset.getHeader().format = asset.getHeader().format;
            
            ObjectPath subFieldPath = new ObjectPath();
            subFieldPath.classID1 = path.classID1;
            subFieldPath.classID2 = path.classID2;
            subFieldPath.length = path.length;
            subFieldPath.offset = 0;
            subFieldPath.pathID = 1;
            subAsset.getObjectPaths().add(subFieldPath);
            
            TypeTree subTypeTree = subAsset.getTypeTree();
            subTypeTree.revision = typeTree.revision;
            subTypeTree.version = -2;
            subTypeTree.setFormat(typeTree.getFormat());
            subTypeTree.put(path.classID2, typeTree.get(path.classID2));
            
            // create a byte buffer for the data area
            bb.position(path.offset);
            ByteBuffer bbAsset = bb.slice();
            bbAsset.limit(path.length);
            bbAsset.order(ByteOrder.LITTLE_ENDIAN);
            
            // probe asset name
            String subAssetName = getAssetName(bbAsset);
            bbAsset.rewind();
            
            if (subAssetName == null) {
                continue;
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
