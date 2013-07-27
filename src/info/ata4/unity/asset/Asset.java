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

import info.ata4.unity.struct.AssetHeader;
import info.ata4.unity.struct.FieldTree;
import info.ata4.unity.struct.ObjectPath;
import info.ata4.unity.struct.ObjectTable;
import info.ata4.util.io.ByteBufferInput;
import info.ata4.util.io.ByteBufferOutput;
import info.ata4.util.io.DataInputReader;
import info.ata4.util.io.DataOutputWriter;
import info.ata4.util.io.MappedFileHandler;
import info.ata4.util.io.NIOFileUtils;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


/**
 * Reader for Unity asset files.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class Asset extends MappedFileHandler {
    
    private static final int HEADER_SIZE = 20;

    private ByteBuffer bbData;
    private AssetHeader header = new AssetHeader();
    private FieldTree fieldTree = new FieldTree();
    private ObjectTable pathTable = new ObjectTable();
    
    @Override
    public void load(ByteBuffer bb) throws IOException {
        DataInputReader in = new DataInputReader(new ByteBufferInput(bb));
        
        header.read(in);

        in.setSwap(true);
        
        fieldTree.clear();
        fieldTree.setFormat(header.format);
        
        pathTable.getPaths().clear();
        pathTable.getRefs().clear();

        switch (header.format) {
            case 6:
            case 7:
            case 8:
                // first data, then struct
                int treeOffset = header.fileSize - header.treeSize + 1;
                
                bb.position(0);
                bbData = bb.slice();
                bbData.limit(treeOffset);
                
                bb.position(treeOffset);

                fieldTree.read(in);
                pathTable.read(in);
                break;
                
            case 9:
                // first struct, then data
                fieldTree.read(in);
                pathTable.read(in);
                
                bb.position(header.dataOffset);
                bbData = bb.slice();
                break;
                
            default:
                throw new AssetException("Unknown asset format " + header.format);
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
        DataOutputWriter outStruct = new DataOutputWriter(new DataOutputStream(bosStruct));
        outStruct.setSwap(true);
        
        fieldTree.setFormat(header.format);
        fieldTree.write(outStruct);
        pathTable.write(outStruct);
        
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
        ByteBuffer bb = NIOFileUtils.openReadWrite(file, 0, header.fileSize);
        DataOutputWriter out = new DataOutputWriter(new ByteBufferOutput(bb));
        
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

    public FieldTree getFieldTree() {
        return fieldTree;
    }

    public ObjectTable getObjectTable() {
        return pathTable;
    }
    
    public List<ObjectPath> getPathsByID(int cid) {
        List<ObjectPath> paths = new ArrayList<>();
        
        for (ObjectPath path : pathTable.getPaths()) {
            if (path.classID1 == cid) {
                paths.add(path);
            }
        }
        
        return paths;
    }
}
