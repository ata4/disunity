/*
 ** 2013 August 17
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.serdes.db;

import info.ata4.log.LogUtils;
import info.ata4.unity.asset.struct.FieldType;
import info.ata4.unity.util.UnityVersion;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FieldTypeMap extends HashMap<Pair<Integer, UnityVersion>, FieldType> {
    
    private static final Logger L = LogUtils.getLogger();

    public FieldType get(int classID, UnityVersion revision) {
        return get(classID, revision, true);
    }

    public FieldType get(int classID, UnityVersion version, boolean strict) {
        FieldType fieldNode = get(new ImmutablePair<>(classID, version));

        // if set to strict, only return exact matches or null
        if (fieldNode != null || strict) {
            return fieldNode;
        }

        FieldType fieldNodeB = null;
        UnityVersion versionB = null;

        FieldType fieldNodeC = null;
        UnityVersion versionC = null;

        for (Map.Entry<Pair<Integer, UnityVersion>, FieldType> entry : entrySet()) {
            Pair<Integer, UnityVersion> fieldNodeKey = entry.getKey();
            if (fieldNodeKey.getLeft() == classID) {
                FieldType fieldNodeEntry = entry.getValue();
                UnityVersion revisionEntry = fieldNodeKey.getRight();

                if (revisionEntry.getMajor() == version.getMajor()) {
                    if (revisionEntry.getMinor() == version.getMinor()) {
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
            L.log(Level.WARNING, "Unprecise match for ClassID {0} (required: {1}, available: {2})", new Object[]{classID, version, versionB});
            return fieldNodeB;
        }

        // return field node from any revision as the very last resort
        if (fieldNodeC != null) {
            L.log(Level.WARNING, "Bad match for ClassID {0} (required: {1}, available: {2})", new Object[]{classID, version, versionC});
            return fieldNodeC;
        }

        // no matches at all
        return null;
    }

    public void add(int classID, UnityVersion revision, FieldType fieldNode) {
        put(new ImmutablePair<>(classID, revision), fieldNode);
    }
}
