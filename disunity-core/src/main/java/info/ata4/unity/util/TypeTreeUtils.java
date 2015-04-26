/*
 ** 2013 August 10
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.util;

import info.ata4.log.LogUtils;
import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.asset.BaseClass;
import info.ata4.unity.asset.TypeNode;
import info.ata4.unity.rtti.ObjectData;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TypeTreeUtils {
    
    private static final Logger L = LogUtils.getLogger();
    private static final TypeTreeDatabase DB = new TypeTreeDatabase();
    
    static {
        DB.load();
    }
    
    public static TypeTreeDatabase getDatabase() {
        return DB;
    }
    
    public static void embedTypes(AssetFile asset) {
        UnityVersion unityRevision = asset.versionInfo().unityRevision();
        if (unityRevision == null) {
            L.warning("unityRevision = null");
            return;
        }
        
        List<ObjectData> objects = asset.objects();
        Iterator<ObjectData> objectIter = objects.iterator();
        Map<Integer, BaseClass> typeTree = asset.typeTree();
        
        while (objectIter.hasNext()) {
            ObjectData object = objectIter.next();
            int typeID = object.info().typeID();
            
            // skip types that already exist
            if (typeTree.containsKey(typeID)) {
                continue;
            }

            TypeNode typeNode = getTypeNode(object, false);
            
            // remove objects with no type tree, which would crash the editor
            // when loading the file otherwise
            if (typeNode == null) {
                L.log(Level.WARNING, "Removing object {0} with unresolvable type {1}",
                        new Object[]{object, typeID});
                objectIter.remove();
                continue;
            }
           
            typeTree.get(typeID).typeTree(typeNode);
        }
    }

    public static int learnTypes(AssetFile asset) {
        if (asset.isStandalone()) {
            L.warning("File doesn't contain type information");
            return 0;
        }
        
        Map<Integer, BaseClass> typeTree = asset.typeTree();
        
        UnityVersion unityRevision = asset.versionInfo().unityRevision();
        if (unityRevision == null) {
            L.warning("unityRevision = null");
            return 0;
        }
        
        int learned = 0;
        
        // merge the TypeTree map with the database field map
        for (Map.Entry<Integer, BaseClass> typeTreeEntry : typeTree.entrySet()) {
            int typeID = typeTreeEntry.getKey();
            TypeNode typeNode = typeTreeEntry.getValue().typeTree();
            
            // skip MonoBehaviour types
            if (typeID < 1) {
                continue;
            }
            
            UnityClass unityClass = new UnityClass(typeID);
            TypeNode typeNodeDB = TypeTreeUtils.getTypeNode(unityClass, unityRevision, true);

            if (typeNodeDB == null) {
                L.log(Level.INFO, "New: {0}", unityClass);
                DB.addEntry(unityClass, unityRevision, typeNode);
                typeNodeDB = typeNode;
                learned++;
            }

            // check the hashes, they must be identical at this point
            int hash1 = typeNode.hashCode();
            int hash2 = typeNodeDB.hashCode();

            if (hash1 != hash2) {
                L.log(Level.WARNING, "Database hash mismatch for {0}: {1} != {2}",
                        new Object[] {typeNodeDB.type().typeName(), hash1, hash2});
            }

            // check if the class name is known and suggest the type base name if not
            if (unityClass.name() == null) {
                L.log(Level.WARNING, "Unknown ClassID {0}, suggested name: {1}",
                        new Object[] {unityClass.ID(), typeNode.type().typeName()});
            }
        }
        
        return learned;
    }
    
    public static TypeNode getTypeNode(UnityClass unityClass, UnityVersion unityVersion, boolean exact) {
        return DB.getTypeNode(unityClass, unityVersion, exact);
    }
    
    public static TypeNode getTypeNode(ObjectData object, boolean strict) {
        return TypeTreeUtils.getTypeNode(object.info().unityClass(), object.versionInfo().unityRevision(), strict);
    }

    private TypeTreeUtils() {
    }
}
