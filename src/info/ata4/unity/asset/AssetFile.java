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
import info.ata4.unity.asset.struct.AssetClassType;
import info.ata4.unity.asset.struct.AssetHeader;
import info.ata4.unity.asset.struct.AssetObjectPath;
import info.ata4.unity.asset.struct.AssetObjectPathTable;
import info.ata4.unity.asset.struct.AssetRef;
import info.ata4.unity.asset.struct.AssetRefTable;
import info.ata4.unity.assetbundle.AssetBundle;
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
    private AssetClassType classType = new AssetClassType();
    private AssetObjectPathTable objTable = new AssetObjectPathTable();
    private AssetRefTable refTable = new AssetRefTable();
    private AssetBundle sourceBundle;
    
    private ByteBuffer bbData;
    private ByteBuffer bbAudio;
    
    @Override
    public void open(Path file) throws IOException {
        // split asset files can't be opened conveniently using memory mapping
        String fileName = file.getFileName().toString();  
        if (fileName.endsWith(".split0")) {
            load(file);
        } else {
            super.open(file);
        }
    }
    
    @Override
    public void load(Path file) throws IOException {
        String fileName = file.getFileName().toString();
        
        // join split asset files before loading
        if (fileName.endsWith(".split0")) {
            fileName = FilenameUtils.removeExtension(fileName);
            List<Path> parts = new ArrayList<>();
            int splitIndex = 0;
            Path part = file;
            
            // collect all files with .split0 to .splitN extension
            while (Files.exists(part)) {
                parts.add(part);
                splitIndex++;
                String splitName = String.format("%s.split%d", fileName, splitIndex);
                part = file.resolveSibling(splitName);
            }
            
            // load all parts into one byte buffer
            ByteBuffer bb = ByteBufferUtils.load(parts);
            
            load(bb);
        } else {
            super.load(file);
        }
        
        // load audio stream if existing
        Path audioStreamFile = file.resolveSibling(fileName + ".resS");
        if (Files.exists(audioStreamFile)) {
            bbAudio = ByteBufferUtils.load(audioStreamFile);
        }
    }
    
    @Override
    public void load(ByteBuffer bb) throws IOException {
        DataInputReader in = DataInputReader.newReader(bb);
        
        header.read(in);
        
        in.setSwap(true);
        
        classType = new AssetClassType();
        objTable = new AssetObjectPathTable();
        refTable = new AssetRefTable();
        
        classType.setFormat(header.getFormat());
        
        switch (header.getFormat()) {
            case 5:
            case 6:
            case 7:
            case 8:
                // first data, then struct
                int treeOffset = header.getFileSize() - header.getTreeSize() + 1;
                bbData = ByteBufferUtils.getSlice(bb, 0, treeOffset);
                bb.position(treeOffset);

                classType.read(in);
                objTable.read(in);
                refTable.read(in);
                break;
                
            case 9:
                // first struct, then data
                classType.read(in);
                objTable.read(in);
                refTable.read(in);
                
                bbData = ByteBufferUtils.getSlice(bb, header.getDataOffset());
                break;
                
            default:
                throw new AssetException("Unknown asset format " + header.getFormat());
        }
        
        // try to get struct from database if the embedded one is empty
        if (classType.getTypeTree().isEmpty()) {
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
        
        classType.setFormat(header.getFormat());
        classType.write(outStruct);
        objTable.write(outStruct);
        refTable.write(outStruct);
        
        // align block to 16 bytes
        int structSize = bosStruct.size();
        int structOffset = structSize + AssetHeader.SIZE;
        int structAlign = 16;
        outStruct.align(structOffset, structAlign);
        
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

    public AssetClassType getClassType() {
        return classType;
    }

    public List<AssetObjectPath> getPaths() {
        return objTable.getPaths();
    }
    
    public List<AssetRef> getReferences() {
        return refTable.getReferences();
    }
    
    public Set<Integer> getClassIDs() {
        Set<Integer> classIDs = new TreeSet<>();
        
        for (AssetObjectPath path : objTable) {
            classIDs.add(path.getClassID());
        }
        
        return classIDs;
    }
    
    public ByteBuffer getPathBuffer(AssetObjectPath path) {
        return ByteBufferUtils.getSlice(getDataBuffer(), path.getOffset(), path.getLength());
    }
}
