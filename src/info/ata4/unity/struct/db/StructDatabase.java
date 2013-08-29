/*
 ** 2013 August 10
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.struct.db;

import static java.nio.file.StandardOpenOption.*;
import static java.nio.file.StandardCopyOption.*;
import info.ata4.unity.asset.Asset;
import info.ata4.unity.struct.FieldType;
import info.ata4.unity.struct.TypeTree;
import info.ata4.unity.util.ClassID;
import info.ata4.util.io.DataInputReader;
import info.ata4.util.io.DataOutputWriter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private Path dbFile = Paths.get("resources", "structdb.dat");
    private Path dbFileBackup = Paths.get("resources", "structdb.dat.1");
    private int learnedTotal;
    
    private StructDatabase() {
        load();
    }
    
    public int getLearned() {
        return learnedTotal;
    }
    
    public FieldNodeDatabase getFieldNodes() {
        return fndb;
    }
    
    private void load() {
        L.info("Loading struct database");
        
        // read database file if existing
        if (Files.exists(dbFile)) {
            try (InputStream fis = Files.newInputStream(dbFile, READ)) {
                DataInputReader dir = new DataInputReader(new DataInputStream(new BufferedInputStream(fis)));
                fndb.read(dir);
            } catch (IOException ex) {
                L.log(Level.SEVERE, "Can't read struct database", ex);
            }
        }
    }
    
    private void save() {
        L.info("Saving struct database");
        
        // create database backup
        if (Files.exists(dbFile)) {
            try {
                Files.move(dbFile, dbFileBackup, REPLACE_EXISTING);
            } catch (IOException ex) {
                L.log(Level.WARNING, "Can't create struct database backup", ex);
            }
        }
        
        // write updated database file
        try (OutputStream fos = Files.newOutputStream(dbFile, CREATE, WRITE)) {
            DataOutputWriter dow = new DataOutputWriter(new DataOutputStream(new BufferedOutputStream(fos)));
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
            FieldType fieldNode = fndb.get(classID, typeTree.revision, false);
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
            FieldType fieldNode = typeTree.get(classID);

            if (fieldNode == null) {
                continue;
            }
            
            FieldType fieldNodeDB = fndb.get(classID, typeTree.revision);

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

            if (ClassID.getInstance().getNameForID(classID) == null) {
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
}
