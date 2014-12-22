/*
 ** 2014 December 22
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.util;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import info.ata4.io.socket.IOSocket;
import info.ata4.io.socket.Sockets;
import info.ata4.io.util.PathUtils;
import info.ata4.log.LogUtils;
import info.ata4.unity.asset.FieldTypeNode;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TypeTreeDatabase {
    
    private static final Logger L = LogUtils.getLogger();
    
    public static final int VERSION = 1;
    public static final String FILENAME = "structdb.dat";
    
    private final Map<Pair<UnityClass, UnityVersion>, FieldTypeNode> nodeMap = new HashMap<>();
    private Path dbFile;
    
    public TypeTreeDatabase() {
        // get database path based on the path to the current .jar file
        dbFile = PathUtils.getCodeSourceLocation(getClass());
        
        if (dbFile != null) {
            dbFile = dbFile.resolveSibling(FILENAME);
        }
    }
    
    private InputStream getDatabaseInputStream() throws IOException {
        // read database file, external or internal otherwise
        InputStream is;
        
        if (dbFile != null && Files.exists(dbFile)) {
            is = Files.newInputStream(dbFile);
        } else {
            is = getClass().getResourceAsStream("/resources/" + FILENAME);
        }
        
        if (is == null) {
            throw new IOException("Type database file not found");
        }
        
        return is;
    }
    
    public void load() {
        L.fine("Loading type database");
        
        try (InputStream is = getDatabaseInputStream()) {
            load(is);
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't open type database", ex);
        }
    }
    
    public void load(InputStream is) {
        try (IOSocket socket = Sockets.forInputStream(new BufferedInputStream(is))) {
            DataReader in = new DataReader(socket);

            // read header
            int dbVersion = in.readInt();

            if (dbVersion != VERSION) {
                throw new RuntimeException("Wrong database version");
            }

            // read field node table
            int fieldNodeSize = in.readInt();
            List<FieldTypeNode> fieldNodes = new ArrayList<>(fieldNodeSize);

            for (int i = 0; i < fieldNodeSize; i++) {
                FieldTypeNode fieldNode = new FieldTypeNode();
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
                int versionIndex = in.readInt();
                
                UnityVersion version = versions.get(versionIndex);
                UnityClass uclass = new UnityClass(classID);
                FieldTypeNode fieldNode = fieldNodes.get(index);

                nodeMap.put(new ImmutablePair<>(uclass, version), fieldNode);
            }
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't read type database", ex);
        }
    }
    
    public void save() {
        L.fine("Saving type database");
        
        try (
            OutputStream os = Files.newOutputStream(dbFile, WRITE, CREATE, TRUNCATE_EXISTING)
        ) {
            save(os);
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't open type database", ex);
        }
    }
    
    public void save(OutputStream os) {
        // write database file
        try (IOSocket socket = Sockets.forOutputStream(new BufferedOutputStream(os))) {
            DataWriter out = new DataWriter(socket);
            
            // write header
            out.writeInt(VERSION);

            // write field node table
            Set<FieldTypeNode> fieldNodes = new HashSet<>(nodeMap.values());
            Map<FieldTypeNode, Integer> fieldNodeMap = new HashMap<>();

            out.writeInt(fieldNodes.size());

            int index = 0;
            for (FieldTypeNode fieldNode : fieldNodes) {
                fieldNodeMap.put(fieldNode, index++);
                fieldNode.write(out);
            }

            // write version string table
            Set<UnityVersion> versions = new HashSet<>();
            Map<UnityVersion, Integer> versionMap = new HashMap<>();

            for (Map.Entry<Pair<UnityClass, UnityVersion>, FieldTypeNode> entry : nodeMap.entrySet()) {
                versions.add(entry.getKey().getRight());
            }

            out.writeInt(versions.size());

            index = 0;
            for (UnityVersion version : versions) {
                versionMap.put(version, index++);
                out.writeStringNull(version.toString());
            }

            // write mapping data
            out.writeInt(nodeMap.entrySet().size());

            for (Map.Entry<Pair<UnityClass, UnityVersion>, FieldTypeNode> entry : nodeMap.entrySet()) {
                index = fieldNodeMap.get(entry.getValue());
                Pair<UnityClass, UnityVersion> fieldNodeKey = entry.getKey();

                int classID = fieldNodeKey.getLeft().getID();
                UnityVersion version = fieldNodeKey.getRight();

                out.writeInt(index);
                out.writeInt(classID);
                out.writeInt(versionMap.get(version));
            }
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't write type database", ex);
        }
    }
    
    public Map<Pair<UnityClass, UnityVersion>, FieldTypeNode> getTypeMap() {
        return nodeMap;
    }
}
