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
import info.ata4.unity.rtti.ObjectSerializer;
import info.ata4.util.io.DataBlock;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.READ;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    
    // collection fields
    private final Map<Integer, ObjectInfo> objectInfoMap = new LinkedHashMap<>();
    private final Map<Integer, ObjectData> objectDataMap = new LinkedHashMap<>();
    private final Map<Integer, FieldTypeNode> typeTreeMap = new LinkedHashMap<>();
    private final List<FileIdentifier> externals = new ArrayList<>();
    
    // struct fields
    private final VersionInfo versionInfo = new VersionInfo();
    private final AssetHeader header = new AssetHeader(versionInfo);
    private final ObjectInfoTable objectInfoStruct = new ObjectInfoTable(objectInfoMap);
    private final FieldTypeTree typeTreeStruct = new FieldTypeTree(typeTreeMap, versionInfo);
    private final FileIdentifierTable externalsStruct = new FileIdentifierTable(externals, versionInfo);
    
    // data block fields
    private final DataBlock headerBlock = new DataBlock();
    private final DataBlock objectInfoBlock = new DataBlock();
    private final DataBlock objectDataBlock = new DataBlock();
    private final DataBlock typeTreeBlock = new DataBlock();
    private final DataBlock externalsBlock = new DataBlock();
    
    // misc fields
    private ByteBuffer audioBuffer;
    
    @Override
    public void load(Path file) throws IOException {
        load(file, null);
    }

    private void load(Path file, Map<Path, AssetFile> childAssets) throws IOException {
        sourceFile = file;
        
        if (childAssets == null) {
            childAssets = new HashMap<>();
        }
        childAssets.put(file, this);
        
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
            audioBuffer = ByteBufferUtils.openReadOnly(audioStreamFile);
        }
        
        load(socket);
        
        for (FileIdentifier external : externals) {
            String filePath = external.getFilePath();
            
            if (filePath == null || filePath.isEmpty()) {
                continue;
            }
            
            filePath = filePath.replace("library/", "resources/");
            
            Path refFile = file.resolveSibling(filePath);
            if (Files.exists(refFile)) {
                AssetFile childAsset = childAssets.get(refFile);
                
                if (childAsset == null) {
                    childAsset = new AssetFile();
                    childAsset.load(refFile, childAssets);
                }
                
                external.setAssetFile(childAsset);
            }
        }
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
        in.setSwap(versionInfo.swapRequired());
        
        // read structure data
        typeTreeBlock.setOffset(in.position());
        in.readStruct(typeTreeStruct);
        typeTreeBlock.setEndOffset(in.position());
        L.log(Level.FINER, "typeTreeBlock: {0}", typeTreeBlock);
        
        objectInfoBlock.setOffset(in.position());
        in.readStruct(objectInfoStruct);
        objectInfoBlock.setEndOffset(in.position());
        L.log(Level.FINER, "objectInfoBlock: {0}", objectInfoBlock);

        externalsBlock.setOffset(in.position());
        in.readStruct(externalsStruct);
        externalsBlock.setEndOffset(in.position());
        L.log(Level.FINER, "externalsBlock: {0}", externalsBlock);
    }
    
    private void loadObjects(DataReader in) throws IOException {
        long ofsMin = Long.MAX_VALUE;
        long ofsMax = Long.MIN_VALUE;
        
        for (Map.Entry<Integer, ObjectInfo> infoEntry : objectInfoMap.entrySet()) {
            ObjectInfo info = infoEntry.getValue();
            int id = infoEntry.getKey();
            
            ByteBuffer buf = ByteBufferUtils.allocate((int) info.getLength());
            
            long ofs = header.getDataOffset() + info.getOffset();
            
            ofsMin = Math.min(ofsMin, ofs);
            ofsMax = Math.max(ofsMax, ofs + info.getLength());
            
            in.position(ofs);
            in.readBuffer(buf);
            
            // try to get type node from database if the embedded one is empty
            FieldTypeNode typeNode;
            if (!typeTreeMap.isEmpty()) {
                typeNode = typeTreeMap.get(info.getTypeID());
            } else {
                typeNode = FieldTypeDatabase.getInstance()
                        .getNode(info.getTypeID(), versionInfo.getUnityRevision(), false);
            }
            
            // in some cases, e.g. standalone MonoBehaviours, the type tree is
            // generally not available
            if (typeNode == null && info.getClassID() != 114) {
                L.log(Level.WARNING, "Skipped {0} with no type tree", info);
            }
                       
            ObjectData data = new ObjectData(id);
            data.setInfo(info);
            data.setBuffer(buf);
            data.setTypeTree(typeNode);
            
            ObjectSerializer serializer = new ObjectSerializer();
            serializer.setSoundData(audioBuffer);
            data.setSerializer(serializer);
            
            objectDataMap.put(id, data);
        }
        
        objectDataBlock.setOffset(ofsMin);
        objectDataBlock.setEndOffset(ofsMax);
        
        L.log(Level.FINER, "objDataBlock: {0}", objectDataBlock);
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
            out.position(objectInfoBlock.getOffset());
            out.writeStruct(objectInfoStruct);
        }
        
        // update header
        header.setFileSize(out.size());
        
        // FIXME: the metadata size is slightly off in comparison to original files
        int metadataOffset = header.getVersion() < 9 ? 2 : 1;
        
        header.setMetadataSize(typeTreeBlock.getLength()
                + objectInfoBlock.getLength()
                + externalsBlock.getLength()
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
        out.setSwap(versionInfo.swapRequired());
        
        typeTreeBlock.setOffset(out.position());
        out.writeStruct(typeTreeStruct);
        typeTreeBlock.setEndOffset(out.position());
        L.log(Level.FINER, "typeTreeBlock: {0}", typeTreeBlock);

        objectInfoBlock.setOffset(out.position());
        out.writeStruct(objectInfoStruct);
        objectInfoBlock.setEndOffset(out.position());
        L.log(Level.FINER, "objectInfoBlock: {0}", objectInfoBlock);

        externalsBlock.setOffset(out.position());
        out.writeStruct(externalsStruct);
        externalsBlock.setEndOffset(out.position());
        L.log(Level.FINER, "externalsBlock: {0}", externalsBlock);
    }
    
    private void saveObjects(DataWriter out) throws IOException {
        long ofsMin = Long.MAX_VALUE;
        long ofsMax = Long.MIN_VALUE;
        
        for (ObjectData data : objectDataMap.values()) {            
            ByteBuffer bb = data.getBuffer();
            bb.rewind();
            
            out.align(8);
            
            ofsMin = Math.min(ofsMin, out.position());
            ofsMax = Math.max(ofsMax, out.position() + bb.remaining());
            
            ObjectInfo path = data.getPath();            
            path.setOffset(out.position() - header.getDataOffset());
            path.setLength(bb.remaining());

            out.writeBuffer(bb);
        }
        
        objectDataBlock.setOffset(ofsMin);
        objectDataBlock.setEndOffset(ofsMax);
        
        L.log(Level.FINER, "objDataBlock: {0}", objectDataBlock);
    }
    
    private void checkBlocks() {
        // sanity check for the data blocks
        assert !headerBlock.isIntersecting(typeTreeBlock);
        assert !headerBlock.isIntersecting(objectInfoBlock);
        assert !headerBlock.isIntersecting(externalsBlock);
        assert !headerBlock.isIntersecting(objectDataBlock);
        
        assert !typeTreeBlock.isIntersecting(objectInfoBlock);
        assert !typeTreeBlock.isIntersecting(externalsBlock);
        assert !typeTreeBlock.isIntersecting(objectDataBlock);
        
        assert !objectInfoBlock.isIntersecting(externalsBlock);
        assert !objectInfoBlock.isIntersecting(objectDataBlock);
        
        assert !objectDataBlock.isIntersecting(externalsBlock);
    }

    public VersionInfo getVersionInfo() {
        return versionInfo;
    }

    public AssetHeader getHeader() {
        return header;
    }

    public Map<Integer, FieldTypeNode> getTypeTree() {
        return typeTreeMap;
    }
    
    public Map<Integer, ObjectInfo> getObjectInfoMap() {
        return objectInfoMap;
    }
    
    public Map<Integer, ObjectData> getObjectDataMap() {
        return objectDataMap;
    }
    
    public List<ObjectData> getObjects() {
        List<ObjectData> objectsCopy = new ArrayList<>();
        
        for (ObjectData object : objectDataMap.values()) {
            // objects without a type tree are unreadabe in most cases, so skip
            // these
            if (object.getTypeTree() != null) {
                objectsCopy.add(object);
            }
        }
        
        return objectsCopy;
    }
    
    public List<FileIdentifier> getExternals() {
        return externals;
    }

    public boolean isStandalone() {
        return typeTreeMap.isEmpty();
    }
    
    public void setStandalone() {
        typeTreeMap.clear();
    }
}
