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
import info.ata4.unity.asset.FieldTypeNode;
import info.ata4.unity.rtti.ObjectData;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

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
    
    public static void fill(AssetFile asset) {
        UnityVersion unityRevision = asset.getVersionInfo().getUnityRevision();
        if (unityRevision == null) {
            L.warning("unityRevision = null");
            return;
        }
        
        List<ObjectData> objects = asset.getObjects();
        Iterator<ObjectData> objectIter = objects.iterator();
        Map<Integer, FieldTypeNode> typeTreeMap = asset.getTypeTree();
        
        while (objectIter.hasNext()) {
            ObjectData object = objectIter.next();
            int typeID = object.getInfo().getTypeID();
            
            // skip types that already exist
            if (typeTreeMap.containsKey(typeID)) {
                continue;
            }

            FieldTypeNode typeNode = getNode(object, false);
            
            // remove objects with no type tree, which would crash the editor
            // when loading the file otherwise
            if (typeNode == null) {
                L.log(Level.WARNING, "Removing object {0} with unresolvable type {1}",
                        new Object[]{object, typeID});
                objectIter.remove();
                continue;
            }
           
            typeTreeMap.put(typeID, typeNode);
        }
    }

    public static int learn(AssetFile asset) {
        Map<Integer, FieldTypeNode> typeTreeMap = asset.getTypeTree();
        
        if (typeTreeMap.isEmpty()) {
            L.warning("File doesn't contain type information");
            return 0;
        }
        
        UnityVersion unityRevision = asset.getVersionInfo().getUnityRevision();
        if (unityRevision == null) {
            L.warning("unityRevision = null");
            return 0;
        }
        
        int learned = 0;
        
        // merge the TypeTree map with the database field map
        for (Map.Entry<Integer, FieldTypeNode> typeTreeEntry : typeTreeMap.entrySet()) {
            int typeID = typeTreeEntry.getKey();
            FieldTypeNode fieldTypeLocal = typeTreeEntry.getValue();
            
            // skip MonoBehaviour types
            if (typeID < 1) {
                continue;
            }
            
            UnityClass unityClass = new UnityClass(typeID);
            FieldTypeNode fieldTypeDatabase = getNode(unityClass, unityRevision, true);

            if (fieldTypeDatabase == null) {
                fieldTypeDatabase = fieldTypeLocal;
                L.log(Level.INFO, "New: {0}", unityClass);
                DB.getTypeMap().put(new ImmutablePair<>(unityClass, unityRevision), fieldTypeDatabase);
                learned++;
            }

            // check the hashes, they must be identical at this point
            int hash1 = fieldTypeLocal.hashCode();
            int hash2 = fieldTypeDatabase.hashCode();

            if (hash1 != hash2) {
                L.log(Level.WARNING, "Database hash mismatch for {0}: {1} != {2}",
                        new Object[] {fieldTypeDatabase.getType().getTypeName(), hash1, hash2});
            }

            // check if the class name is known and suggest the type base name if not
            if (unityClass.getName() == null) {
                L.log(Level.WARNING, "Unknown ClassID {0}, suggested name: {1}",
                        new Object[] {unityClass.getID(), fieldTypeLocal.getType().getTypeName()});
            }
        }
        
        return learned;
    }
    
    public static FieldTypeNode getNode(UnityClass unityClass, UnityVersion unityVersion, boolean strict) {
        FieldTypeNode fieldNode = DB.getTypeMap().get(new ImmutablePair<>(unityClass, unityVersion));

        // if set to strict, only return exact matches or null
        if (fieldNode != null || strict) {
            return fieldNode;
        }

        FieldTypeNode fieldNodeB = null;
        UnityVersion versionB = null;

        FieldTypeNode fieldNodeC = null;
        UnityVersion versionC = null;

        for (Map.Entry<Pair<UnityClass, UnityVersion>, FieldTypeNode> entry : DB.getTypeMap().entrySet()) {
            Pair<UnityClass, UnityVersion> fieldNodeKey = entry.getKey();
            if (fieldNodeKey.getLeft().equals(unityClass)) {
                FieldTypeNode fieldNodeEntry = entry.getValue();
                UnityVersion revisionEntry = fieldNodeKey.getRight();

                if (revisionEntry.getMajor() == unityVersion.getMajor()) {
                    if (revisionEntry.getMinor() == unityVersion.getMinor()) {
                        // if major and minor versions match, it will probably work
                        return fieldNodeEntry;
                    } else {
                        // suboptimal choice
                        fieldNodeB = fieldNodeEntry;
                        versionB = revisionEntry;
                    }
                }

                // worst choice
                fieldNodeC = fieldNodeEntry;
                versionC = revisionEntry;
            }
        }

        // return less perfect match
        if (fieldNodeB != null) {
            L.log(Level.WARNING, "Unprecise match for class {0} (required: {1}, available: {2})", new Object[]{unityClass, unityVersion, versionB});
            return fieldNodeB;
        }

        // return field node from any revision as the very last resort
        if (fieldNodeC != null) {
            L.log(Level.WARNING, "Bad match for class {0} (required: {1}, available: {2})", new Object[]{unityClass, unityVersion, versionC});
            return fieldNodeC;
        }

        // no matches at all
        return null;
    }
    
    public static FieldTypeNode getNode(ObjectData object, boolean strict) {
        return getNode(object.getInfo().getUnityClass(), object.getVersionInfo().getUnityRevision(), strict);
    }

    private TypeTreeUtils() {
    }
}
