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
import static java.nio.file.StandardOpenOption.READ;
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
    
    private static final int METADATA_PADDING = 4096;
    
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
    private final DataBlock objDataBlock = new DataBlock();

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
            socket = Sockets.forFile(file, READ);
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
        if (socket.getProperties().isStreaming()) {
            throw new IOException("Random access is required");
        }
        
        DataReader in = new DataReader(socket);
        
        loadHeader(in);

        // read as little endian from now on
        in.setSwap(true);
        
        // older formats store the object data before the structure data
        if (header.getVersion() < 9) {
            in.position(header.getFileSize() - header.getMetadataSize() + 1);
        }
        
        loadMetadata(in);
        loadObjects(in);
        checkBlocks();
    }
    
    private void loadHeader(DataReader in) throws IOException {
        headerBlock.setOffset(0);
        in.readStruct(header);
        headerBlock.setEndOffset(in.position());
        
        L.log(Level.FINER, "headerBlock: {0}", headerBlock);
    }
    
    private void loadMetadata(DataReader in) throws IOException {
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
    }
    
    private void loadObjects(DataReader in) throws IOException {
        objects = new ArrayList<>();
        
        long ofsMin = Long.MAX_VALUE;
        long ofsMax = Long.MIN_VALUE;
        
        for (ObjectPath path : objTable.getPaths()) {
            ByteBuffer buf = ByteBufferUtils.allocate((int) path.getLength());
            
            long ofs = header.getDataOffset() + path.getOffset();
            
            ofsMin = Math.min(ofsMin, ofs);
            ofsMax = Math.max(ofsMax, ofs + path.getLength());
            
            in.position(ofs);
            in.readBuffer(buf);
            
            // try to get type node from database if the embedded one is empty
            FieldTypeNode typeNode;
            if (!typeTree.getFields().isEmpty()) {
                typeNode = typeTree.getFields().get(path.getTypeID());
            } else {
                typeNode = FieldTypeDatabase.getInstance().getNode(path.getTypeID(), typeTree.getUnityRevision());
            }
            
            // in some cases, e.g. standalone MonoBehaviours, the type tree is
            // generally not available
            if (typeNode == null && path.getClassID() != 114) {
                L.log(Level.WARNING, "Skipped {0} with no type tree", path);
            }
           
            ObjectData data = new ObjectData();
            data.setPath(path);
            data.setBuffer(buf);
            data.setSoundBuffer(audioBuf);
            data.setTypeTree(typeNode);
            data.setVersionInfo(versionInfo);
            
            objects.add(data);
        }
        
        objDataBlock.setOffset(ofsMin);
        objDataBlock.setEndOffset(ofsMax);
        
        L.log(Level.FINER, "objDataBlock: {0}", objDataBlock);
    }
    
    @Override
    public void save(IOSocket socket) throws IOException {
        if (socket.getProperties().isStreaming()) {
            throw new IOException("Random access is required");
        }
        
        DataWriter out = new DataWriter(socket);
        
        saveHeader(out);
        
        // write as little endian from now on
        out.setSwap(true);
        
        // older formats store the object data before the structure data
        if (header.getVersion() < 9) {
            header.setDataOffset(0);
            
            saveObjects(out);
            out.write(0);
            
            saveMetadata(out);
            out.write(0);
        } else {
            saveMetadata(out);
            
            // original files have a minimum padding of 4096 bytes after the
            // metadata
            if (out.position() < METADATA_PADDING) {
                out.align(METADATA_PADDING);
            }
            
            out.align(16);
            header.setDataOffset(out.position());
            
            saveObjects(out);
            
            // write updated path table
            out.position(objTableBlock.getOffset());
            out.writeStruct(objTable);
        }
        
        // update header
        header.setFileSize(out.size());
        
        // FIXME: the metadata size is slightly off in comparison to original files
        int metadataOffset = header.getVersion() < 9 ? 2 : 1;
        
        header.setMetadataSize(typeTreeBlock.getLength()
                + objTableBlock.getLength()
                + refTableBlock.getLength()
                + metadataOffset);
        
        // write updated header
        out.setSwap(false);
        out.position(headerBlock.getOffset());
        out.writeStruct(header);
             
        checkBlocks();
    }
    
    private void saveHeader(DataWriter out) throws IOException {
        headerBlock.setOffset(0);
        out.writeStruct(header);
        headerBlock.setEndOffset(out.position());
        
        L.log(Level.FINER, "headerBlock: {0}", headerBlock);
    }
    
    private void saveMetadata(DataWriter out) throws IOException {
        typeTreeBlock.setOffset(out.position());
        out.writeStruct(typeTree);
        typeTreeBlock.setEndOffset(out.position());

        L.log(Level.FINER, "typeTreeBlock: {0}", typeTreeBlock);

        objTableBlock.setOffset(out.position());
        out.writeStruct(objTable);
        objTableBlock.setEndOffset(out.position());

        L.log(Level.FINER, "objTableBlock: {0}", objTableBlock);

        refTableBlock.setOffset(out.position());
        out.writeStruct(refTable);
        refTableBlock.setEndOffset(out.position());
        
        L.log(Level.FINER, "refTableBlock: {0}", refTableBlock);
    }
    
    private void saveObjects(DataWriter out) throws IOException {
        long ofsMin = Long.MAX_VALUE;
        long ofsMax = Long.MIN_VALUE;
        
        for (ObjectData data : objects) {            
            ByteBuffer bb = data.getBuffer();
            bb.rewind();
            
            out.align(8);
            
            ofsMin = Math.min(ofsMin, out.position());
            ofsMax = Math.max(ofsMax, out.position() + bb.remaining());
            
            ObjectPath path = data.getPath();            
            path.setOffset(out.position() - header.getDataOffset());
            path.setLength(bb.remaining());

            out.writeBuffer(bb);
        }
        
        objDataBlock.setOffset(ofsMin);
        objDataBlock.setEndOffset(ofsMax);
        
        L.log(Level.FINER, "objDataBlock: {0}", objDataBlock);
    }
    
    private void checkBlocks() {
        // sanity check for the data blocks
        assert !headerBlock.isIntersecting(typeTreeBlock);
        assert !headerBlock.isIntersecting(objTableBlock);
        assert !headerBlock.isIntersecting(refTableBlock);
        assert !headerBlock.isIntersecting(objDataBlock);
        
        assert !typeTreeBlock.isIntersecting(objTableBlock);
        assert !typeTreeBlock.isIntersecting(refTableBlock);
        assert !typeTreeBlock.isIntersecting(objDataBlock);
        
        assert !objTableBlock.isIntersecting(refTableBlock);
        assert !objTableBlock.isIntersecting(objDataBlock);
        
        assert !objDataBlock.isIntersecting(refTableBlock);
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
