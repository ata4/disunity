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
import info.ata4.unity.assetbundle.BundleEntryBuffered;
import info.ata4.unity.rtti.FieldTypeDatabase;
import info.ata4.unity.rtti.FieldTypeTree;
import info.ata4.unity.rtti.ObjectData;
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

    private final AssetHeader header = new AssetHeader();
    private final FieldTypeTree typeTree = new FieldTypeTree();
    private final ObjectPathTable objTable = new ObjectPathTable();
    private final ReferenceTable refTable = new ReferenceTable();
    
    private List<ObjectData> objects;
    private ByteBuffer audioBuf;
    private boolean standalone;
    private BundleEntryBuffered sourceBundleEntry;

    @Override
    public void load(Path file) throws IOException {
        sourceFile = file;
        
        String fileName = file.getFileName().toString();
        String fileExt = FilenameUtils.getExtension(fileName);
        
        DataInputReader in;
        
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
            
            // load all parts to one byte buffer
            in = DataInputReader.newReader(ByteBufferUtils.load(parts));
        } else {
            // map single file to memory
            in = DataInputReader.newMappedReader(file);
        }
        
        // load audio buffer if existing
        Path audioStreamFile = file.resolveSibling(fileName + ".resS");
        if (Files.exists(audioStreamFile)) {
            audioBuf = ByteBufferUtils.openReadOnly(audioStreamFile);
        }
        
        load(in);
    }
    
    public void load(BundleEntryBuffered entry) throws IOException {
        sourceBundleEntry = entry;
        load(entry.getReader());
    }
    
    @Override
    public void load(DataInputReader in) throws IOException {
        // read header    
        in.readStruct(header);
        in.setSwap(true);

        // older formats store the object data before the structure data
        if (header.getVersion() < 9) {
            in.position(header.getFileSize() - header.getMetadataSize() + 1);
        }
        
        // read structure data
        typeTree.setFormat(header.getVersion());
        in.readStruct(typeTree);
        in.readStruct(objTable);
        in.readStruct(refTable);
        
        // try to get struct from database if the embedded one is empty
        if (typeTree.getFields().isEmpty()) {
            L.info("Standalone asset file detected, using structure from database");
            FieldTypeDatabase.getInstance().fill(this);
            standalone = true;
        }
        
        // read object data
        objects = new ArrayList<>();
        
        for (ObjectPath path : objTable.getPaths()) {
            if (path.getTypeID() < 0) {
                continue;
            }
            
            ByteBuffer buf = ByteBufferUtils.allocate(path.getLength());

            in.position(header.getDataOffset() + path.getOffset());
            in.readBuffer(buf);
           
            ObjectData data = new ObjectData();
            data.setPath(path);
            data.setBuffer(buf);
            data.setSoundBuffer(audioBuf);
            data.setTypeTree(typeTree.getFields().get(path.getTypeID()));
            
            objects.add(data);
        }
    }
    
    @Override
    public void save(DataOutputWriter in) throws IOException {
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
        return standalone;
    }

    public void setStandalone(boolean standalone) {
        this.standalone = standalone;
    }

    public BundleEntryBuffered getSourceBundleEntry() {
        return sourceBundleEntry;
    }
    
    public Set<Integer> getClassIDs() {
        Set<Integer> classIDs = new TreeSet<>();
        
        for (ObjectPath path : objTable.getPaths()) {
            classIDs.add(path.getClassID());
        }
        
        return classIDs;
    }

    public List<ObjectData> getObjects() {
        return objects;
    }
}
