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
import info.ata4.unity.struct.Struct;
import info.ata4.util.collection.Pair;
import info.ata4.util.io.DataInputReader;
import info.ata4.util.io.DataOutputWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FieldNodeDatabase extends HashMap<Pair<Integer, String>, FieldType> implements Struct {
    
    private static final Logger L = Logger.getLogger(FieldNodeDatabase.class.getName());

    private static final int VERSION = 1;

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

    @Override
    public void read(DataInputReader in) throws IOException {
        // read header
        int version = in.readInt();

        if (version != VERSION) {
            throw new RuntimeException("Wrong database version");
        }

        // read field node table
        int fieldNodeSize = in.readInt();
        List<FieldType> fieldNodes = new ArrayList<>(fieldNodeSize);

        for (int i = 0; i < fieldNodeSize; i++) {
            FieldType fieldNode = new FieldType();
            fieldNode.read(in);
            fieldNodes.add(fieldNode);
        }

        // read revision string table
        int revisionSize = in.readInt();
        List<String> revisions = new ArrayList<>(revisionSize);

        for (int i = 0; i < revisionSize; i++) {
            revisions.add(in.readStringNull());
        }

        // read mapping data
        int fieldNodeKeySize = in.readInt();

        for (int i = 0; i < fieldNodeKeySize; i++) {
            int index = in.readInt();
            int classID = in.readInt();
            int revisionIndex = in.readInt();
            String revision = revisions.get(revisionIndex);
            FieldType fieldNode = fieldNodes.get(index);

            add(classID, revision, fieldNode);
        }
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        // write header
        out.writeInt(VERSION);

        // write field node table
        Set<FieldType> fieldNodes = new HashSet<>(values());
        Map<FieldType, Integer> fieldNodeMap = new HashMap<>();

        out.writeInt(fieldNodes.size());

        int index = 0;
        for (FieldType fieldNode : fieldNodes) {
            fieldNodeMap.put(fieldNode, index++);
            fieldNode.write(out);
        }

        Set<Pair<Integer, String>> fieldNodeKeys = keySet();

        // write revision string table
        Set<String> revisions = new HashSet<>();
        Map<String, Integer> revisionMap = new HashMap<>();

        for (Map.Entry<Pair<Integer, String>, FieldType> entry : entrySet()) {
            revisions.add(entry.getKey().getRight());
        }

        out.writeInt(revisions.size());

        index = 0;
        for (String revision : revisions) {
            revisionMap.put(revision, index++);
            out.writeStringNull(revision);
        }

        // write mapping data
        out.writeInt(fieldNodeKeys.size());

        for (Map.Entry<Pair<Integer, String>, FieldType> entry : entrySet()) {
            index = fieldNodeMap.get(entry.getValue());
            Pair<Integer, String> fieldNodeKey = entry.getKey();

            int classID = fieldNodeKey.getLeft();
            String revision = fieldNodeKey.getRight();

            out.writeInt(index);
            out.writeInt(classID);
            out.writeInt(revisionMap.get(revision));
        }
    }
}
