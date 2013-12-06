/*
 ** 2013 July 24
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.serdes;

import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.asset.struct.AssetFieldType;
import info.ata4.unity.asset.struct.AssetObjectPath;
import info.ata4.util.io.ByteBufferUtils;
import info.ata4.util.io.DataInputReader;
import info.ata4.util.io.NIOFileUtils;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Deserializer for asset objects.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class Deserializer {
    
    private final AssetFile asset;
    private SerializedInput in;
    private ByteBuffer bbAsset;
    private boolean debug = false;
    
    public Deserializer(AssetFile asset) {
        this.asset = asset;
    }
    
    public UnityObject deserialize(AssetObjectPath path) throws DeserializationException {
        // create a byte buffer containing the object's data
        ByteBuffer bbAssets = asset.getDataBuffer();
        
        bbAsset = ByteBufferUtils.getSlice(bbAssets, path.offset, path.length);
        bbAsset.order(ByteOrder.LITTLE_ENDIAN);
        
        if (debug) {
            try {
                File dumpFile = new File(String.format("0x%x.bin", path.offset));
                NIOFileUtils.save(dumpFile, bbAsset);
                bbAsset.rewind();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        in = new SerializedInput(new DataInputReader(bbAsset));

        AssetFieldType classNode = asset.getTypeTree().get(path.classID2);
        
        if (classNode.isEmpty()) {
            classNode = asset.getTypeTree().get(path.classID1);
        }
        
        if (classNode == null) {
            throw new DeserializationException("Class not found in type tree");
        }
        
        UnityObject ac = new UnityObject();
        ac.setName(classNode.name);
        ac.setType(classNode.type);

        for (AssetFieldType fieldNode : classNode) {
            ac.addField(readField(fieldNode));
        }

        // read remaining bytes
        try {
            in.align();
        } catch (IOException ex) {
            throw new DeserializationException("Alignment failed");
        }
        
        // check if all bytes have been read
        if (bbAsset.hasRemaining()) {
            throw new DeserializationException("Remaining bytes: " + bbAsset.remaining());
        }
         
        return ac;
    }
    
    private UnityObject readObject(AssetFieldType field) throws DeserializationException {
        UnityObject ac = new UnityObject();
        ac.setName(field.name);
        ac.setType(field.type);
        
        for (AssetFieldType fieldNode : field) {
            ac.addField(readField(fieldNode));
        }
        
        return ac;
    }
    
    private UnityField readField(AssetFieldType field) throws DeserializationException {
        UnityField af = new UnityField();
        af.setName(field.name);
        af.setType(field.type);
        
        int pos = 0;
        
        if (debug) {
            pos = bbAsset.position();
        }
        
        try {
            af.setValue(readFieldValue(field));
        } catch (IOException ex) {
            throw new DeserializationException("Can't read value of field " + field.name, ex);
        }
        
        if (debug) {
            int bytes = bbAsset.position() - pos;
            System.out.printf("0x%x: %s %s = %s, bytes: %d, flags: 0x%x 0x%x\n",
                    pos, af.getType(), af.getName(), af.getValue(), bytes, field.flags1, field.flags2);
        }

        return af;
    }

    private Object readFieldValue(AssetFieldType field) throws IOException, DeserializationException {
        Object value;
        
        if (field.isEmpty()) {
            value = readPrimitive(field);
        } else {
            value = readComplex(field);
        }
        
        if (field.isForceAlign()) {
            in.align();
        }
        
        return value;
    }
    
    private Object readPrimitive(AssetFieldType field) throws IOException, DeserializationException {
        switch (field.type) {
            case "UInt64":
                return in.readUnsignedLong();

            case "SInt64":
                return in.readLong();

            case "SInt32":
            case "int":
                return in.readInt();

            case "UInt32":
            case "unsigned int":
                return in.readUnsignedInt();

            case "SInt16":
            case "short":
                return in.readShort();

            case "UInt16":
            case "unsigned short":
                return in.readUnsignedShort();

            case "SInt8":
                return in.readByte();

            case "UInt8":
            case "char":
                return in.readUnsignedByte();

            case "float":
                return in.readFloat();

            case "double":
                return in.readDouble();

            case "bool":
                return in.readBoolean();
                
            default:
                throw new DeserializationException("Unknown primitive type: " + field.type);
        }
    }
    
    private Object readComplex(AssetFieldType field) throws IOException, DeserializationException {
        switch (field.type) {
            case "string":
                return in.readString();
            
            case "map":
            case "vector":
            case "staticvector":
            case "set":
                return readArray(field.get(0));
            
            case "Array":
            case "TypelessData":
                return readArray(field);
                
            default:
                return readObject(field);
        }
    }
    
    private UnityArray readArray(AssetFieldType field) throws IOException, DeserializationException {
        AssetFieldType sizeField = field.get(0);
        AssetFieldType dataField = field.get(1);
        int size = (int) readFieldValue(sizeField);
        UnityArray uarray = new UnityArray(dataField.type);
        
        // use wrapped ByteBuffers for raw byte arrays, which is much faster and
        // more efficient than a list of Integer objects
        if (dataField.type.equals("UInt8") || dataField.type.equals("char")) {
            ByteBuffer raw = ByteBuffer.wrap(in.readByteArray(size));
            uarray.setRaw(raw);
        } else {
            List<Object> objList = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                objList.add(readFieldValue(dataField));
            }
            uarray.setList(objList);
        }
        
        return uarray;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
