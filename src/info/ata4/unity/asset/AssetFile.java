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

import info.ata4.unity.asset.struct.AssetHeader;
import info.ata4.unity.asset.struct.AssetObjectPath;
import info.ata4.unity.asset.struct.AssetObjectPathTable;
import info.ata4.unity.asset.struct.AssetRefTable;
import info.ata4.unity.asset.struct.AssetTypeTree;
import info.ata4.unity.serdes.db.StructDatabase;
import info.ata4.util.io.ByteBufferUtils;
import info.ata4.util.io.DataInputReader;
import info.ata4.util.io.DataOutputWriter;
import info.ata4.util.io.MappedFileHandler;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
public class AssetFile extends MappedFileHandler {
    
    private static final Logger L = Logger.getLogger(AssetFile.class.getName());
    private static final int HEADER_SIZE = 20;

    private ByteBuffer bbData;
    private AssetHeader header = new AssetHeader();
    private AssetTypeTree typeTree = new AssetTypeTree();
    private AssetObjectPathTable objTable = new AssetObjectPathTable();
    private AssetRefTable refTable = new AssetRefTable();
    
    @Override
    public void load(File file, boolean map) throws IOException {
        String fileName = file.getName();
        
        // join split asset files before loading
        if (fileName.endsWith(".split0")) {
            fileName = FilenameUtils.removeExtension(fileName);
            List<Path> parts = new ArrayList<>();
            int splitIndex = 0;
            Path part = file.toPath();
            
            // collect all files with .split0 to .splitN extension
            while (Files.exists(part)) {
                parts.add(part);
                splitIndex++;
                String splitName = String.format("%s.split%d", fileName, splitIndex);
                part = new File(file.getParentFile(), splitName).toPath();
            }
            
            // load all parts into one byte buffer
            ByteBuffer bb = ByteBufferUtils.load(parts);
            
            load(bb);
        } else {
            super.load(file, map);
        }
    }
    
    @Override
    public void load(ByteBuffer bb) throws IOException {
        DataInputReader in = new DataInputReader(bb);
        
        header.read(in);
        
        in.setSwap(true);
        
        typeTree.clear();        
        objTable.clear();
        refTable.clear();
        
        typeTree.setFormat(header.format);
        
        switch (header.format) {
            case 6:
            case 7:
            case 8:
                // first data, then struct
                int treeOffset = header.fileSize - header.treeSize + 1;
                bbData = ByteBufferUtils.getSlice(bb, 0, treeOffset);
                bb.position(treeOffset);

                typeTree.read(in);
                objTable.read(in);
                refTable.read(in);
                break;
                
            case 9:
                // first struct, then data
                typeTree.read(in);
                objTable.read(in);
                refTable.read(in);
                
                bbData = ByteBufferUtils.getSlice(bb, header.dataOffset);
                break;
                
            default:
                throw new AssetException("Unknown asset format " + header.format);
        }
        
        // try to get struct from database if the embedded one is empty
        if (typeTree.isEmpty()) {
            L.info("Standalone asset file detected, using structure from database");
            StructDatabase.getInstance().fill(this);
        }
    }
    
    @Override
    public void save(File file) throws IOException {
        // TODO: support older formats
        if (header.format != 9) {
            throw new AssetException("Only format 9 is supported right now");
        }
        
        // build struct info
        ByteArrayOutputStream bosStruct = new ByteArrayOutputStream();
        DataOutputWriter outStruct = new DataOutputWriter(bosStruct);
        outStruct.setSwap(true);
        
        typeTree.setFormat(header.format);
        typeTree.write(outStruct);
        objTable.write(outStruct);
        refTable.write(outStruct);
        
        // align block to 16 bytes
        int structSize = bosStruct.size();
        int structOffset = structSize + HEADER_SIZE;
        int structAlign = 16;
        outStruct.align(structOffset, structAlign);
        
        ByteBuffer bbStruct = ByteBuffer.wrap(bosStruct.toByteArray());
        
        // calculate padding
        int minSize = 4096;
        int padding = Math.max(0, minSize - HEADER_SIZE - bbStruct.limit());
        
        // configure header
        header.treeSize = structSize;
        header.dataOffset = HEADER_SIZE + bbStruct.limit() + padding;
        header.fileSize = header.dataOffset + bbData.limit();
        
        // open file
        ByteBuffer bb = ByteBufferUtils.openReadWrite(file.toPath(), 0, header.fileSize);
        DataOutputWriter out = new DataOutputWriter(bb);
        
        // write header
        header.write(out);

        // write struct
        bb.put(bbStruct);
        
        // write padding
        out.skipBytes(padding);
        
        // write data
        bb.put(bbData);
    }

    public ByteBuffer getDataBuffer() {
        return bbData;
    }
    
    public void setDataBuffer(ByteBuffer bbData) {
        this.bbData = bbData;
    }

    public AssetHeader getHeader() {
        return header;
    }

    public AssetTypeTree getTypeTree() {
        return typeTree;
    }

    public AssetObjectPathTable getObjectPaths() {
        return objTable;
    }
    
    public AssetRefTable getReferences() {
        return refTable;
    }
    
    public List<AssetObjectPath> getPathsByID(int cid) {
        List<AssetObjectPath> paths = new ArrayList<>();
        
        for (AssetObjectPath path : objTable) {
            if (path.classID1 == cid) {
                paths.add(path);
            }
        }
        
        return paths;
    }
    
    public Set<Integer> getClassIDs() {
        Set<Integer> classIDs = new TreeSet<>();
        
        for (AssetObjectPath path : objTable) {
            classIDs.add(path.classID2);
        }
        
        return classIDs;
    }
}
