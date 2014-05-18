/*
 ** 2013 August 10
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.serdes.db;

import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
import info.ata4.log.LogUtils;
import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.asset.struct.TypeField;
import info.ata4.unity.asset.struct.TypeTree;
import info.ata4.unity.util.ClassID;
import info.ata4.unity.util.UnityVersion;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class StructDatabase {
    
    private static final Logger L = LogUtils.getLogger();
    private static final int VERSION = 1;
    private static final String FILENAME = "structdb.dat";
    
    private static StructDatabase instance;

    public static StructDatabase getInstance() {
        if (instance == null) {
            instance = new StructDatabase();
        }
        return instance;
    }
    
    private FieldTypeMap ftm = new FieldTypeMap();
    private int learned;
    
    private StructDatabase() {
        load();
    }
    
    public int getLearned() {
        return learned;
    }
    
    public FieldTypeMap getFieldTypeMap() {
        return ftm;
    }
    
    private void load() {
        L.info("Loading struct database");
        
        // read database file, external or internal otherwise
        InputStream is;
        try {
            Path dbFile = Paths.get(FILENAME);
            String dbPath = "/resources/" + FILENAME;

            if (Files.exists(dbFile)) {
                is = Files.newInputStream(dbFile);
            } else {
                is = getClass().getResourceAsStream(dbPath);
            }
            
            if (is == null) {
                throw new IOException("Struct database file not found");
            }
        } catch (Exception ex) {
            L.log(Level.SEVERE, "Can't open struct database", ex);
            return;
        }

        try (BufferedInputStream bis = new BufferedInputStream(is)) {
            DataInputReader in = DataInputReader.newReader(bis);

            // read header
            int dbVersion = in.readInt();

            if (dbVersion != VERSION) {
                throw new RuntimeException("Wrong database version");
            }

            // read field node table
            int fieldNodeSize = in.readInt();
            List<TypeField> fieldNodes = new ArrayList<>(fieldNodeSize);

            for (int i = 0; i < fieldNodeSize; i++) {
                TypeField fieldNode = new TypeField();
                fieldNode.read(in);
                fieldNodes.add(fieldNode);
            }

            // read version string table
            int versionSize = in.readInt();
            List<UnityVersion> versions = new ArrayList<>(versionSize);

            for (int i = 0; i < versionSize; i++) {
                versions.add(new UnityVersion(in.readStringNull()));
            }

            // read mapping data
            int fieldNodeKeySize = in.readInt();

            for (int i = 0; i < fieldNodeKeySize; i++) {
                int index = in.readInt();
                int classID = in.readInt();
                int revisionIndex = in.readInt();
                
                UnityVersion version = versions.get(revisionIndex);
                TypeField fieldNode = fieldNodes.get(index);

                ftm.add(classID, version, fieldNode);
            }
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't read struct database", ex);
        }
    }
    
    private void save() {
        L.info("Saving struct database");
        
        // write database file
        File dbFile = new File(FILENAME);
        try (BufferedOutputStream bos = new BufferedOutputStream(FileUtils.openOutputStream(dbFile))) {
            DataOutputWriter out = DataOutputWriter.newWriter(bos);
            
            // write header
            out.writeInt(VERSION);

            // write field node table
            Set<TypeField> fieldNodes = new HashSet<>(ftm.values());
            Map<TypeField, Integer> fieldNodeMap = new HashMap<>();

            out.writeInt(fieldNodes.size());

            int index = 0;
            for (TypeField fieldNode : fieldNodes) {
                fieldNodeMap.put(fieldNode, index++);
                fieldNode.write(out);
            }

            // write version string table
            Set<UnityVersion> versions = new HashSet<>();
            Map<UnityVersion, Integer> versionMap = new HashMap<>();

            for (Map.Entry<Pair<Integer, UnityVersion>, TypeField> entry : ftm.entrySet()) {
                versions.add(entry.getKey().getRight());
            }

            out.writeInt(versions.size());

            index = 0;
            for (UnityVersion version : versions) {
                versionMap.put(version, index++);
                out.writeStringNull(version.toString());
            }

            // write mapping data
            out.writeInt(ftm.entrySet().size());

            for (Map.Entry<Pair<Integer, UnityVersion>, TypeField> entry : ftm.entrySet()) {
                index = fieldNodeMap.get(entry.getValue());
                Pair<Integer, UnityVersion> fieldNodeKey = entry.getKey();

                int classID = fieldNodeKey.getLeft();
                UnityVersion version = fieldNodeKey.getRight();

                out.writeInt(index);
                out.writeInt(classID);
                out.writeInt(versionMap.get(version));
            }
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't write struct database", ex);
        }
    }
    
    public void fill(AssetFile asset) {
        TypeTree typeTree = asset.getTypeTree();
        Set<Integer> classIDs = asset.getClassIDs();
        
        fixRevision(asset, typeTree);
        
        if (typeTree.getEngineVersion() == null) {
            L.warning("Revision = null");
            return;
        }
        
        for (Integer classID : classIDs) {
            TypeField ft = ftm.get(classID, typeTree.getEngineVersion(), false);
            if (ft != null) {
                typeTree.getFields().put(classID, ft);
            }
        }
    }
    
    public int learn(AssetFile asset) {
        TypeTree typeTree = asset.getTypeTree();
        Set<Integer> classIDs = asset.getClassIDs();
        
        if (typeTree.getFields().isEmpty()) {
            L.info("No type tree available");
            return 0;
        }
        
        fixRevision(asset, typeTree);
        
        if (typeTree.getEngineVersion() == null) {
            L.warning("Revision = null");
            return 0;
        }
        
        int learnedNew = 0;
        
        // merge the TypeTree map with the database field map
        for (Integer classID : classIDs) {
            TypeField fieldType = typeTree.getFields().get(classID);
            String fieldClassName = ClassID.getNameForID(classID);

            if (fieldType == null) {
                continue;
            }
            
            TypeField fieldTypeMapped = ftm.get(classID, typeTree.getEngineVersion());

            if (fieldTypeMapped == null) {
                fieldTypeMapped = fieldType;
                L.log(Level.INFO, "New: {0} ({1})", new Object[]{classID, fieldClassName});
                ftm.add(classID, typeTree.getEngineVersion(), fieldTypeMapped);
                learnedNew++;
            }

            // check the hashes, they must be identical at this point
            int hash1 = fieldType.hashCode();
            int hash2 = fieldTypeMapped.hashCode();

            if (hash1 != hash2) {
                L.log(Level.WARNING, "Database hash mismatch for {0}: {1} != {2}", new Object[] {fieldTypeMapped.getType(), hash1, hash2});
            }

            if (fieldClassName == null) {
                L.log(Level.WARNING, "Unknown ClassID {0}, suggested name: {1}", new Object[] {classID, fieldType.getType()});
            }
        }
        
        learned += learnedNew;
        
        return learnedNew;
    }
    
    public void update() {
        if (learned > 0) {
            L.log(Level.INFO, "Adding {0} new struct(s) to database", learned);
            save();
            learned = 0;
        }
    }

    private void fixRevision(AssetFile asset, TypeTree typeTree) {
        // older file formats don't contain the revision in the header, try to
        // get it from the asset bundle header instead
        if (typeTree.getEngineVersion() == null && asset.getSourceBundle() != null) {
            typeTree.setEngineVersion(asset.getSourceBundle().getEngineVersion());
        }
    }
}
