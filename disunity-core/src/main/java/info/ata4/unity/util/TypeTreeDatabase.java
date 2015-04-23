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
import info.ata4.io.DataReaders;
import info.ata4.io.DataWriter;
import info.ata4.io.DataWriters;
import info.ata4.io.util.PathUtils;
import info.ata4.log.LogUtils;
import info.ata4.unity.asset.TypeNode;
import info.ata4.unity.asset.TypeNodeReader;
import info.ata4.unity.asset.TypeNodeWriter;
import info.ata4.unity.asset.VersionInfo;
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
import java.util.Collections;
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
    
    public static final String FILE_NAME = "types.dat";
    public static final int FILE_FORMAT = 1;
    public static final int NODE_FORMAT = 9;
    
    private final List<TypeTreeDatabaseEntry> entries = new ArrayList<>();
    private final Map<Pair<UnityClass, UnityVersion>, TypeTreeDatabaseEntry> entryMap = new HashMap<>();
    private Path dbFile;
    
    public TypeTreeDatabase() {
        // get database path based on the path to the current .jar file
        dbFile = PathUtils.getCodeSourceLocation(getClass());
        
        if (dbFile != null) {
            dbFile = dbFile.resolveSibling(FILE_NAME);
        }
    }
    
    private InputStream getDatabaseInputStream() throws IOException {
        // read database file, external or internal otherwise
        InputStream is;
        
        if (dbFile != null && Files.exists(dbFile)) {
            is = Files.newInputStream(dbFile);
        } else {
            is = getClass().getResourceAsStream("/resources/" + FILE_NAME);
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
        try (DataReader in = DataReaders.forInputStream(is)) {
            // read header
            int dbVersion = in.readInt();

            if (dbVersion != FILE_FORMAT) {
                throw new RuntimeException("Wrong database version");
            }
            
            // create node reader
            VersionInfo versionInfo = new VersionInfo();
            versionInfo.assetVersion(NODE_FORMAT);
            TypeNodeReader nodeReader = new TypeNodeReader(versionInfo);

            // read field node table
            int fieldNodeSize = in.readInt();
            List<TypeNode> typeNodes = new ArrayList<>(fieldNodeSize);
            
            for (int i = 0; i < fieldNodeSize; i++) {
                TypeNode node = new TypeNode();
                nodeReader.read(in, node);
                typeNodes.add(node);
            }

            // read version string table
            int versionSize = in.readInt();
            List<UnityVersion> versionsTmp = new ArrayList<>(versionSize);

            for (int i = 0; i < versionSize; i++) {
                versionsTmp.add(new UnityVersion(in.readStringNull()));
            }

            // read mapping data
            int numEntries = in.readInt();

            for (int i = 0; i < numEntries; i++) {
                int nodeIndex = in.readInt();
                int classID = in.readInt();
                int versionIndex = in.readInt();
                
                UnityVersion unityVersion = versionsTmp.get(versionIndex);
                UnityClass unityClass = new UnityClass(classID);
                TypeNode typeNode = typeNodes.get(nodeIndex);
                
                addEntry(unityClass, unityVersion, typeNode);
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
        try (DataWriter out = DataWriters.forOutputStream(new BufferedOutputStream(os))) {
            // write header
            out.writeInt(FILE_FORMAT);
            
            // create node writer
            VersionInfo versionInfo = new VersionInfo();
            versionInfo.assetVersion(NODE_FORMAT);
            TypeNodeWriter nodeWriter = new TypeNodeWriter(versionInfo);

            // write field node table
            List<TypeNode> nodesTmp = new ArrayList<>(new HashSet<>(getNodeSet()));
            out.writeInt(nodesTmp.size());

            for (TypeNode typeNode : nodesTmp) {
                nodeWriter.write(out, typeNode);
            }

            // write version string table
            List<UnityVersion> versionsTmp = new ArrayList<>(new HashSet<>(getVersionSet()));
            out.writeInt(versionsTmp.size());

            for (UnityVersion version : versionsTmp) {
                out.writeStringNull(version.toString());
            }

            // write mapping data
            out.writeInt(entries.size());

            for (TypeTreeDatabaseEntry entry : entries) {
                UnityClass unityClass = entry.unityClass();
                UnityVersion unityVersion = entry.unityVersion();
                TypeNode node = entry.typeNode();
                
                int nodeIndex = nodesTmp.indexOf(node);
                int classID = unityClass.ID();
                int versionIndex = versionsTmp.indexOf(unityVersion);

                out.writeInt(nodeIndex);
                out.writeInt(classID);
                out.writeInt(versionIndex);
            }
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't write type database", ex);
        }
    }
    
    public List<TypeTreeDatabaseEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }
    
    public Set<UnityVersion> getVersionSet() {
        Set<UnityVersion> versions = new HashSet<>();
        for (TypeTreeDatabaseEntry entry : entries) {
            versions.add(entry.unityVersion());
        }
        return versions;
    }
    
    public Set<UnityClass> getClassSet() {
        Set<UnityClass> classes = new HashSet<>();
        for (TypeTreeDatabaseEntry entry : entries) {
            classes.add(entry.unityClass());
        }
        return classes;
    }
    
    public Set<TypeNode> getNodeSet() {
        Set<TypeNode> nodes = new HashSet<>();
        for (TypeTreeDatabaseEntry entry : entries) {
            nodes.add(entry.typeNode());
        }
        return nodes;
    }
    
    public TypeTreeDatabaseEntry getEntry(UnityClass unityClass, UnityVersion unityVersion, boolean exact) {
        // search for exact matches
        TypeTreeDatabaseEntry entryA = entryMap.get(new ImmutablePair<>(unityClass, unityVersion));
        if (entryA != null) {
            return entryA;
        }
        
        // cancel if exact matches are required
        if (exact) {
            return null;
        }
        
        TypeTreeDatabaseEntry entryB = null;
        UnityVersion versionB = null;

        TypeTreeDatabaseEntry entryC = null;
        UnityVersion versionC = null;
        
        for (TypeTreeDatabaseEntry entry : entries) {
            UnityClass uclass = entry.unityClass();
            UnityVersion version = entry.unityVersion();
                    
            if (uclass.equals(unityClass)) {
                if (version.major() == unityVersion.major()) {
                    if (version.minor() == unityVersion.minor()) {
                        // if major and minor versions match, it will probably work
                        return entry;
                    } else {
                        // suboptimal choice
                        entryB = entry;
                        versionB = version;
                    }
                }

                // worst choice
                entryC = entry;
                versionC = version;
            }
        }

        // return less perfect match
        if (entryB != null) {
            L.log(Level.WARNING, "Unprecise match for class {0} (required: {1}, available: {2})", new Object[]{unityClass, unityVersion, versionB});
            return entryB;
        }

        // return field node from any revision as the very last resort
        if (entryC != null) {
            L.log(Level.WARNING, "Bad match for class {0} (required: {1}, available: {2})", new Object[]{unityClass, unityVersion, versionC});
            return entryC;
        }

        // no matches at all
        return null;
    }
    
    public TypeNode getTypeNode(UnityClass unityClass, UnityVersion unityVersion, boolean exact) {
        TypeTreeDatabaseEntry entry = getEntry(unityClass, unityVersion, exact);
        if (entry != null) {
            return entry.typeNode();
        } else {
            return null;
        }
    }
    
    public void addEntry(TypeTreeDatabaseEntry entry) {
        // don't add duplicates
        if (entries.contains(entry)) {
            return;
        }
        
        entries.add(entry);
        entryMap.put(new ImmutablePair<>(entry.unityClass(), entry.unityVersion()), entry);
    }
    
    public void addEntry(UnityClass unityClass, UnityVersion unityVersion, TypeNode node) {
        addEntry(new TypeTreeDatabaseEntry(unityClass, unityVersion, node));
    }
}
