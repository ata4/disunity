/*
 ** 2013 June 15
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.asset;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.io.file.FileHandler;
import info.ata4.io.socket.IOSocket;
import info.ata4.io.socket.Sockets;
import info.ata4.log.LogUtils;
import info.ata4.unity.rtti.FieldTypeDatabase;
import info.ata4.unity.rtti.ObjectData;
import info.ata4.util.io.DataBlock;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;


/**
 * Reader for Unity asset files.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetFile extends FileHandler {
    
    private static final Logger L = LogUtils.getLogger();
    
    private final AssetVersionInfo versionInfo = new AssetVersionInfo();
    private final AssetHeader header = new AssetHeader(versionInfo);
    private final FieldTypeTree typeTree = new FieldTypeTree(versionInfo);
    private final ObjectPathTable objTable = new ObjectPathTable();
    private final ReferenceTable refTable = new ReferenceTable(versionInfo);
    
    private List<ObjectData> objects;
    private ByteBuffer audioBuf;
    
    private final DataBlock headerBlock = new DataBlock();
    private final DataBlock typeTreeBlock = new DataBlock();
    private final DataBlock objTableBlock = new DataBlock();
    private final DataBlock refTableBlock = new DataBlock();

    @Override
    public void load(Path file) throws IOException {
        sourceFile = file;
        
        String fileName = file.getFileName().toString();
        String fileExt = FilenameUtils.getExtension(fileName);
        
        IOSocket socket;
        
        // join split asset files before loading
        if (fileExt.startsWith("split")) {
            L.fine("Found split asset file");
            
            fileName = FilenameUtils.removeExtension(fileName);
            List<Path> parts = new ArrayList<>();
            int splitIndex = 0;

            // collect all files with .split0 to .splitN extension
            while (true) {
                String splitName = String.format("%s.split%d", fileName, splitIndex);
                Path part = file.resolveSibling(splitName);
                if (Files.notExists(part)) {
                    break;
                }
                
                L.log(Level.FINE, "Adding splinter {0}", part.getFileName());
                
                splitIndex++;
                parts.add(part);
            }
            
            // load all parts to one byte buffer
            socket = Sockets.forByteBuffer(ByteBufferUtils.load(parts));
        } else {
            // map single file to memory
            socket = Sockets.forFileMemoryMapped(file);
        }
        
        // load audio buffer if existing
        Path audioStreamFile = file.resolveSibling(fileName + ".resS");
        if (Files.exists(audioStreamFile)) {
            L.log(Level.FINE, "Found sound stream file {0}", audioStreamFile.getFileName());
            audioBuf = ByteBufferUtils.openReadOnly(audioStreamFile);
        }
        
        load(socket);
    }
    
    @Override
    public void load(IOSocket socket) throws IOException {
        DataReader in = new DataReader(socket);
        
        // read header
        headerBlock.setOffset(0);
        in.readStruct(header);
        in.setSwap(true);
        headerBlock.setEndOffset(in.position());
        
        L.log(Level.FINER, "headerBlock: {0}", headerBlock);
        
        // older formats store the object data before the structure data
        if (header.getVersion() < 9) {
            in.position(header.getFileSize() - header.getMetadataSize() + 1);
        }
        
        // read structure data
        typeTreeBlock.setOffset(in.position());
        in.readStruct(typeTree);
        typeTreeBlock.setEndOffset(in.position());
        
        L.log(Level.FINER, "typeTreeBlock: {0}", typeTreeBlock);

        objTableBlock.setOffset(in.position());
        in.readStruct(objTable);
        objTableBlock.setEndOffset(in.position());
        
        L.log(Level.FINER, "objTableBlock: {0}", objTableBlock);

        refTableBlock.setOffset(in.position());
        in.readStruct(refTable);
        refTableBlock.setEndOffset(in.position());
        
        L.log(Level.FINER, "refTableBlock: {0}", refTableBlock);
        
        // sanity check for the data blocks
        assert !headerBlock.isIntersecting(typeTreeBlock);
        assert !headerBlock.isIntersecting(objTableBlock);
        assert !headerBlock.isIntersecting(refTableBlock);
        
        assert !typeTreeBlock.isIntersecting(objTableBlock);
        assert !typeTreeBlock.isIntersecting(refTableBlock);
        
        assert !objTableBlock.isIntersecting(refTableBlock);

        // read object data
        objects = new ArrayList<>();
        
        for (ObjectPath path : objTable.getPaths()) {
            ByteBuffer buf = ByteBufferUtils.allocate(path.getLength());

            in.position(header.getDataOffset() + path.getOffset());
            in.readBuffer(buf);
            
            // try to get type node from database if the embedded one is empty
            FieldTypeNode typeNode;
            if (!typeTree.getFields().isEmpty()) {
                typeNode = typeTree.getFields().get(path.getTypeID());
            } else {
                typeNode = FieldTypeDatabase.getInstance().getNode(path.getTypeID(), typeTree.getUnityRevision());
            }
            
            // in some cases, e.g. standalone MonoBehaviours, the type tree is not
            // available
            if (typeNode == null) {
                // log a warning if it's not a MonoBehaviour
                if (path.getClassID() != 114) {
                    L.log(Level.WARNING, "Skipped {0} with no type tree", path);
                }
                continue;
            }
           
            ObjectData data = new ObjectData();
            data.setPath(path);
            data.setBuffer(buf);
            data.setSoundBuffer(audioBuf);
            data.setTypeTree(typeNode);
            data.setVersionInfo(versionInfo);
            
            objects.add(data);
        }
    }
    
    @Override
    public void save(IOSocket socket) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public AssetHeader getHeader() {
        return header;
    }

    public FieldTypeTree getTypeTree() {
        return typeTree;
    }
    
    public List<Reference> getReferences() {
        return refTable.getReferences();
    }

    public boolean isStandalone() {
        return typeTree.getFields().isEmpty();
    }
    
    public void setStandalone() {
        typeTree.getFields().clear();
    }

    public List<ObjectData> getObjects() {
        return objects;
    }

    public List<ObjectPath> getObjectPaths() {
        return objTable.getPaths();
    }

    public AssetVersionInfo getVersionInfo() {
        return versionInfo;
    }
}
