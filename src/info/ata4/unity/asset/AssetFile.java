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

import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.io.file.FileHandler;
import info.ata4.log.LogUtils;
import info.ata4.unity.asset.bundle.AssetBundle;
import info.ata4.unity.asset.struct.AssetHeader;
import info.ata4.unity.asset.struct.AssetRef;
import info.ata4.unity.asset.struct.AssetRefTable;
import info.ata4.unity.asset.struct.ObjectPath;
import info.ata4.unity.asset.struct.ObjectPathTable;
import info.ata4.unity.asset.struct.TypeTree;
import info.ata4.unity.serdes.db.StructDatabase;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;


/**
 * Reader for Unity asset files.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetFile extends FileHandler {
    
    private static final Logger L = LogUtils.getLogger();

    private AssetHeader header = new AssetHeader();
    private TypeTree typeTree = new TypeTree();
    private ObjectPathTable objTable = new ObjectPathTable();
    private AssetRefTable refTable = new AssetRefTable();
    private AssetBundle sourceBundle;
    
    private ByteBuffer bbData;
    private ByteBuffer bbAudio;
    
    @Override
    public void open(Path file) throws IOException {
        load(file, true);
    }
    
    @Override
    public void load(Path file) throws IOException {
        load(file, false);
    }
    
    private void load(Path file, boolean map) throws IOException {
        setSourceFile(file);
        
        String fileName = file.getFileName().toString();
        String fileExt = FilenameUtils.getExtension(fileName);
        
        ByteBuffer bb;
        
        // join split asset files before loading
        if (fileExt.startsWith("split")) {
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
                splitIndex++;
                parts.add(part);
            }
            
            // load all parts into one byte buffer
            bb = ByteBufferUtils.load(parts);
        } else if (map) {
            bb = ByteBufferUtils.openReadOnly(file);
        } else {
            bb = ByteBufferUtils.load(file);
        }
        
        // load audio stream if existing
        Path audioStreamFile = file.resolveSibling(fileName + ".resS");
        if (Files.exists(audioStreamFile)) {
            bbAudio = ByteBufferUtils.openReadOnly(audioStreamFile);
        }
        
        load(bb);
    }
    
    @Override
    public void load(ByteBuffer bb) throws IOException {
        DataInputReader in = DataInputReader.newReader(bb);
        in.readStruct(header);
        in.setSwap(true);
        
        typeTree = new TypeTree();
        objTable = new ObjectPathTable();
        refTable = new AssetRefTable();
        
        typeTree.setFormat(header.getFormat());
        
        switch (header.getFormat()) {
            case 5:
            case 6:
            case 7:
            case 8:
                // first data, then struct
                int treeOffset = header.getFileSize() - header.getTreeSize() + 1;
                bbData = ByteBufferUtils.getSlice(bb, 0, treeOffset);
                bb.position(treeOffset);

                in.readStruct(typeTree);
                in.readStruct(objTable);
                in.readStruct(refTable);
                break;
                
            case 9:
                // first struct, then data
                in.readStruct(typeTree);
                in.readStruct(objTable);
                in.readStruct(refTable);
                
                bbData = ByteBufferUtils.getSlice(bb, header.getDataOffset());
                break;
                
            default:
                throw new AssetException("Unknown asset format " + header.getFormat());
        }
        
        // try to get struct from database if the embedded one is empty
        if (typeTree.getFields().isEmpty()) {
            L.info("Standalone asset file detected, using structure from database");
            StructDatabase.getInstance().fill(this);
        }
    }
    
    @Override
    public void save(Path file) throws IOException {
        // TODO: support older formats
        if (header.getFormat() != 9) {
            throw new AssetException("Only format 9 is supported right now");
        }
        
        // build struct info
        ByteArrayOutputStream bosStruct = new ByteArrayOutputStream();
        DataOutputWriter outStruct = DataOutputWriter.newWriter(bosStruct);
        outStruct.setSwap(true);
        
        typeTree.setFormat(header.getFormat());
        typeTree.write(outStruct);
        objTable.write(outStruct);
        refTable.write(outStruct);
        
        // align block to 16 bytes
        int structSize = bosStruct.size();
        int structAlign = 16;
        outStruct.align(structAlign);
        
        ByteBuffer bbStruct = ByteBuffer.wrap(bosStruct.toByteArray());
        
        // calculate padding
        int minSize = 4096;
        int padding = Math.max(0, minSize - AssetHeader.SIZE - bbStruct.limit());
        
        // configure header
        header.setTreeSize(structSize);
        header.setDataOffset(AssetHeader.SIZE + bbStruct.limit() + padding);
        header.setFileSize(header.getDataOffset() + bbData.limit());
        
        // open file
        ByteBuffer bb = ByteBufferUtils.openReadWrite(file, 0, header.getFileSize());
        DataOutputWriter out = DataOutputWriter.newWriter(bb);
        
        // write header
        header.write(out);

        // write struct
        bb.put(bbStruct);
        
        // write padding
        out.skipBytes(padding);
        
        // write data
        bb.put(bbData);
    }
    
    public AssetBundle getSourceBundle() {
        return sourceBundle;
    }

    public void setSourceBundle(AssetBundle sourceBundle) {
        this.sourceBundle = sourceBundle;
    }

    public ByteBuffer getDataBuffer() {
        return bbData;
    }
    
    public void setDataBuffer(ByteBuffer bbData) {
        this.bbData = bbData;
    }
    
    public ByteBuffer getAudioBuffer() {
        return bbAudio;
    }

    public void setAudioBuffer(ByteBuffer bbAudio) {
        this.bbAudio = bbAudio;
    }

    public AssetHeader getHeader() {
        return header;
    }

    public TypeTree getTypeTree() {
        return typeTree;
    }

    public List<ObjectPath> getPaths() {
        return objTable.getPaths();
    }
    
    public List<AssetRef> getReferences() {
        return refTable.getReferences();
    }
    
    public Set<Integer> getClassIDs() {
        Set<Integer> classIDs = new TreeSet<>();
        
        for (ObjectPath path : objTable.getPaths()) {
            classIDs.add(path.getClassID());
        }
        
        return classIDs;
    }
    
    public ByteBuffer getPathBuffer(ObjectPath path) {
        return ByteBufferUtils.getSlice(getDataBuffer(), path.getOffset(), path.getLength());
    }
}
