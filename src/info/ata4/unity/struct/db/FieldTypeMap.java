/*
 ** 2013 August 17
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.struct.db;

import info.ata4.unity.struct.FieldType;
import info.ata4.util.collection.Pair;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FieldTypeMap extends HashMap<Pair<Integer, String>, FieldType> {
    
    private static final Logger L = Logger.getLogger(FieldTypeMap.class.getName());

    public FieldType get(int classID, String revision) {
        return get(classID, revision, true);
    }

    public FieldType get(int classID, String revision, boolean strict) {
        FieldType fieldNode = get(new Pair<>(classID, revision));

        // if set to strict, only return exact matches or null
        if (fieldNode != null || strict) {
            return fieldNode;
        }

        // try a similar revision and ignore the patch number
        String revision2 = revision.substring(0, 3);
        String revision3 = revision.substring(0, 1);

        FieldType fieldNodeB = null;
        String revisionB = null;

        FieldType fieldNodeC = null;
        String revisionC = null;


        for (Map.Entry<Pair<Integer, String>, FieldType> entry : entrySet()) {
            Pair<Integer, String> fieldNodeKey = entry.getKey();
            if (fieldNodeKey.getLeft() == classID) {
                FieldType fieldNodeEntry = entry.getValue();
                String revisionEntry = fieldNodeKey.getRight();

                // if major and minor version matches, it will probably work
                if (fieldNodeKey.getRight().startsWith(revision2)) {
                    return fieldNodeEntry;
                }

                // suboptimal choice
                if (fieldNodeKey.getRight().startsWith(revision3)) {
                    fieldNodeB = fieldNodeEntry;
                    revisionB = revisionEntry;
                }

                // worst choice
                fieldNodeC = fieldNodeEntry;
                revisionC = revisionEntry;
            }
        }

        // return less perfect match
        if (fieldNodeB != null) {
            L.log(Level.WARNING, "Unprecise match for ClassID {0} (expected: {1}, available: {2})", new Object[]{classID, revision, revisionB});
            return fieldNodeB;
        }

        // return field node from any revision as the very last resort
        if (fieldNodeC != null) {
            L.log(Level.WARNING, "Bad match for ClassID {0} (expected: {1}, available: {2})", new Object[]{classID, revision, revisionC});
            return fieldNodeC;
        }

        // no matches at all
        return null;
    }

    public void add(int classID, String revision, FieldType fieldNode) {
        put(new Pair<>(classID, revision), fieldNode);
    }
}
