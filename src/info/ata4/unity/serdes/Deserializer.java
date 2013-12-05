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
import info.ata4.util.io.ByteBufferInput;
import info.ata4.util.io.ByteBufferUtils;
import info.ata4.util.io.DataInputReader;
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
    private ByteBuffer bb;
    
    public Deserializer(AssetFile asset) {
        this.asset = asset;
    }
    
    public UnityObject deserialize(AssetObjectPath path) throws DeserializerException {
        // create a byte buffer for the data area
        ByteBuffer bbData = asset.getDataBuffer();
        bb = ByteBufferUtils.getSlice(bbData, path.offset, path.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        
        // create asset input
        in = new SerializedInput(new DataInputReader(bb));

        AssetFieldType classNode = asset.getTypeTree().get(path.classID2);
        
        if (classNode == null) {
            throw new DeserializerException("Class not found in type tree");
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
            throw new DeserializerException("Alignment failed");
        }
        
        // check if all bytes have been read
        if (bb.hasRemaining()) {
            throw new DeserializerException("Remaining bytes: " + bb.remaining());
        }
         
        return ac;
    }
    
    private UnityObject readObject(AssetFieldType field) throws DeserializerException {
        UnityObject ac = new UnityObject();
        ac.setName(field.name);
        ac.setType(field.type);
        
        for (AssetFieldType fieldNode : field) {
            ac.addField(readField(fieldNode));
        }
        
        return ac;
    }
    
    private UnityField readField(AssetFieldType field) throws DeserializerException {
        UnityField af = new UnityField();
        af.setName(field.name);
        af.setType(field.type);
        
        try {
            af.setValue(readFieldValue(field));
        } catch (IOException ex) {
            throw new DeserializerException("Can't read value of field " + field.name, ex);
        }
        
//        System.out.printf("%s %s (%s) @ %d\n", af.getName(), af.getValue(), af.getType(), bb.position());

        return af;
    }

    private Object readFieldValue(AssetFieldType field) throws IOException, DeserializerException {
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
    
    private Object readPrimitive(AssetFieldType field) throws IOException, DeserializerException {
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
                throw new DeserializerException("Unknown primitive type: " + field.type);
        }
    }
    
    private Object readComplex(AssetFieldType field) throws IOException, DeserializerException {
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
    
    private UnityArray readArray(AssetFieldType field) throws IOException, DeserializerException {
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
}
