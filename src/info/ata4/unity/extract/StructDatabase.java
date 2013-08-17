/*
 ** 2013 August 10
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.extract;

import info.ata4.unity.asset.Asset;
import info.ata4.unity.struct.FieldNode;
import info.ata4.unity.struct.TypeTree;
import info.ata4.unity.struct.Struct;
import info.ata4.unity.util.ClassID;
import info.ata4.util.collection.Pair;
import info.ata4.util.io.DataInputReader;
import info.ata4.util.io.DataOutputWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class StructDatabase {
    
    private static final Logger L = Logger.getLogger(StructDatabase.class.getName());
    private static StructDatabase instance;

    public static StructDatabase getInstance() {
        if (instance == null) {
            instance = new StructDatabase();
        }
        return instance;
    }
    
    private FieldNodeDatabase fndb = new FieldNodeDatabase();
    private File dbFile = new File("structdb.dat");
    private File dbFileBackup = new File("structdb.dat.1");
    private int learnedTotal;
    
    private StructDatabase() {
        load();
    }
    
    private void load() {
        L.info("Loading struct database");
        
        // read database file if existing
        if (dbFile.exists()) {
            try (FileInputStream fis = new FileInputStream(dbFile)) {
                DataInputReader dir = new DataInputReader(new DataInputStream(fis));
                fndb.read(dir);
            } catch (IOException ex) {
                L.log(Level.SEVERE, "Can't read struct database", ex);
            }
        }
    }
    
    private void save() {
        L.info("Saving struct database");
        
        // create database backup
        if (dbFile.exists()) {
            try {
                FileUtils.deleteQuietly(dbFileBackup);
                FileUtils.moveFile(dbFile, dbFileBackup);
            } catch (IOException ex) {
                L.log(Level.WARNING, "Can't create struct database backup", ex);
            }
        }
        
        // write updated database file
        try (FileOutputStream fos = new FileOutputStream(dbFile)) {
            DataOutputWriter dow = new DataOutputWriter(new DataOutputStream(fos));
            fndb.write(dow);
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't write struct database", ex);
        }
    }
    
    public void fill(Asset asset) {
        TypeTree typeTree = asset.getTypeTree();
        Set<Integer> classIDs = asset.getClassIDs();
        
        if (typeTree.revision == null) {
            L.warning("typeTree.revision = null");
            return;
        }
        
        for (Integer classID : classIDs) {
            FieldNode fieldNode = fndb.get(classID, typeTree.revision, false);
            if (fieldNode != null) {
                typeTree.put(classID, fieldNode);
            }
        }
        
        // don't include the struct when saving
        typeTree.setStandalone(true);
    }
    
    public int learn(Asset asset) {
        TypeTree typeTree = asset.getTypeTree();
        Set<Integer> classIDs = asset.getClassIDs();
        
        if (typeTree.isStandalone()) {
            L.info("No structure data available");
            return 0;
        }
        
        // older file formats don't contain the revision in the header, override
        // it manually here
        if (typeTree.revision == null) {
            //typeTree.revision = "2.6.0f7";
            L.warning("typeTree.revision = null");
            return 0;
        }
        
        int learnedNew = 0;
        
        // merge the TypeTree map with the database field map
        for (Integer classID : classIDs) {
            FieldNode fieldNode = typeTree.get(classID);

            if (fieldNode == null) {
                continue;
            }
            
            FieldNode fieldNodeDB = fndb.get(classID, typeTree.revision);

            if (fieldNodeDB == null) {
                fieldNodeDB = fieldNode;
                fndb.add(classID, typeTree.revision, fieldNodeDB);
                learnedNew++;
            }

            // check the hashes, they must be identical at this point
            int hash1 = fieldNode.hashCode();
            int hash2 = fieldNodeDB.hashCode();

            if (hash1 != hash2) {
                L.log(Level.WARNING, "Database hash mismatch for {0}: {1} != {2}", new Object[] {fieldNodeDB.type, hash1, hash2});
            }

            if (ClassID.getNameForID(classID) == null) {
                L.log(Level.WARNING, "Unknown ClassID {0}, suggested name: {1}", new Object[] {classID, fieldNode.type});
            }
        }
        
        learnedTotal += learnedNew;
        
        return learnedNew;
    }
    
    public void update() {
        if (learnedTotal > 0) {
            L.log(Level.INFO, "Adding {0} new struct(s) to database", learnedTotal);
            save();
            learnedTotal = 0;
        }
    }
    
    public int getLearned() {
        return learnedTotal;
    }
    
    private class FieldNodeDatabase extends HashMap<Pair<Integer, String>, FieldNode> implements Struct {
        
        private static final int VERSION = 1;
        
        public FieldNode get(int classID, String revision) {
            return get(classID, revision, true);
        }
        
        public FieldNode get(int classID, String revision, boolean strict) {
            FieldNode fieldNode = get(new Pair<>(classID, revision));
            
            // if set to strict, only return exact matches or null
            if (fieldNode != null || strict) {
                return fieldNode;
            }
            
            // try a similar revision and ignore the patch number
            String revision2 = revision.substring(0, 3);
            String revision3 = revision.substring(0, 1);
            
            FieldNode fieldNodeB = null;
            String revisionB = null;
            
            FieldNode fieldNodeC = null;
            String revisionC = null;
            
            
            for (Map.Entry<Pair<Integer, String>, FieldNode> entry : entrySet()) {
                Pair<Integer, String> fieldNodeKey = entry.getKey();
                if (fieldNodeKey.getLeft() == classID) {
                    FieldNode fieldNodeEntry = entry.getValue();
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
                L.log(Level.WARNING, "Unprecise match for ClassID {0} (expected: {1}, available: {2})", new Object[] {classID, revision, revisionB});
                return fieldNodeB;
            }
            
            // return field node from any revision as the very last resort
            if (fieldNodeC != null) {
                L.log(Level.WARNING, "Bad match for ClassID {0} (expected: {1}, available: {2})", new Object[] {classID, revision, revisionC});
                return fieldNodeC;
            }
           
            // no matches at all
            return null;
        }
        
        public void add(int classID, String revision, FieldNode fieldNode) {
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
            List<FieldNode> fieldNodes = new ArrayList<>(fieldNodeSize);
            
            for (int i = 0; i < fieldNodeSize; i++) {
                FieldNode fieldNode = new FieldNode();
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
                FieldNode fieldNode = fieldNodes.get(index);
                
                add(classID, revision, fieldNode);
            }
        }

        @Override
        public void write(DataOutputWriter out) throws IOException {
            // write header
            out.writeInt(VERSION);
            
            // write field node table
            Set<FieldNode> fieldNodes = new HashSet<>(values());
            Map<FieldNode, Integer> fieldNodeMap = new HashMap<>();
            
            out.writeInt(fieldNodes.size());
            
            int index = 0;
            for (FieldNode fieldNode : fieldNodes) {
                fieldNodeMap.put(fieldNode, index++);
                fieldNode.write(out);
            }
            
            Set<Pair<Integer, String>> fieldNodeKeys = keySet();
            
            // write revision string table
            Set<String> revisions = new HashSet<>();
            Map<String, Integer> revisionMap = new HashMap<>();
            
            for (Map.Entry<Pair<Integer, String>, FieldNode> entry : entrySet()) {
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
            
            for (Map.Entry<Pair<Integer, String>, FieldNode> entry : entrySet()) {
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
}
