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

import info.ata4.unity.asset.Asset;
import info.ata4.unity.struct.FieldType;
import info.ata4.unity.struct.ObjectPath;
import info.ata4.util.io.ByteBufferInput;
import info.ata4.util.io.DataInputReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Deserializer for asset objects.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class Deserializer {
    
    private final Asset asset;
    private SerializedInput in;
    private ByteBuffer bb;
    
    public Deserializer(Asset asset) {
        this.asset = asset;
    }
    
    public UnityObject deserialize(ObjectPath path) throws DeserializerException {
        // create a byte buffer for the data area
        ByteBuffer bbData = asset.getDataBuffer();
        bbData.position(path.offset);
        bb = bbData.slice();
        bb.limit(path.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        // create asset input
        in = new SerializedInput(new DataInputReader(new ByteBufferInput(bb)));

        FieldType classNode = asset.getTypeTree().get(path.classID2);
        
        if (classNode == null) {
            throw new DeserializerException("Class not found in type tree");
        }
        
        UnityObject ac = new UnityObject();
        ac.setName(classNode.name);
        ac.setType(classNode.type);

        for (FieldType fieldNode : classNode) {
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
    
    private UnityField readField(FieldType field) throws DeserializerException {
        UnityField af = new UnityField();
        af.setName(field.name);
        af.setType(field.type);
        
        try {
            af.setValue(readFieldValue(field));
            if (field.isForceAlign()) {
                in.align();
            }
        } catch (IOException ex) {
            throw new DeserializerException("Can't read value of field " + field.name, ex);
        }

        return af;
    }

    private Object readFieldValue(FieldType field) throws IOException, DeserializerException {
        switch (field.type) {
            case "UInt64":
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
                
            case "string":
                return in.readString();
                
            case "vector":
            case "staticvector":
                return readVector(field);
                
            case "set":
                return readSet(field);
                
            case "map":
                return readMap(field);
            
            case "Array":
                return readArray(field);
                
            case "TypelessData":
                return readTypelessData(field);

            default:
                return readObject(field);
        }
    }
    
    private Object readVector(FieldType field) throws IOException, DeserializerException {
        int size = in.readInt();
        FieldType arrayField = field.get(0).get(1);
        
        // use wrapped ByteBuffers for raw byte arrays, which is much faster and
        // more efficient than a list of Integer objects
        if (arrayField.type.equals("UInt8") || arrayField.type.equals("char")) {
            return ByteBuffer.wrap(in.readByteArray(size));
        } else {
            return doReadArray(arrayField, size);
        }
    }
    
    private List<Object> readArray(FieldType field) throws IOException, DeserializerException {
        int size = in.readInt();
        FieldType arrayField = field.get(1);
        return doReadArray(arrayField, size);
    }
    
    private List<Object> doReadArray(FieldType field, int size) throws IOException, DeserializerException {
        List<Object> objList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            objList.add(readFieldValue(field));
        }
        return objList;
    }
    
    private Map<Object, Object> readMap(FieldType field) throws IOException, DeserializerException {
        List<Object> pairList = readArray(field.get(0));
        Map<Object, Object> map = new HashMap<>();
        
        for (Object pair : pairList) {
            UnityObject pairUnity = (UnityObject) pair;
            Object first = pairUnity.getValue("first");
            Object second = pairUnity.getValue("second");
            map.put(first, second);
        }
        
        return map;
    }
    
    private Set<Object> readSet(FieldType field) throws IOException, DeserializerException {
        Set<Object> set = new HashSet<>();
        set.addAll(readArray(field.get(0)));
        return set;
    }
    
    private ByteBuffer readTypelessData(FieldType field) throws IOException {
        return ByteBuffer.wrap(in.readByteArray());
    }
    
    private UnityObject readObject(FieldType field) throws DeserializerException {
        UnityObject ac = new UnityObject();
        ac.setName(field.name);
        ac.setType(field.type);

        for (FieldType fieldNode : field) {
            ac.addField(readField(fieldNode));
        }
        
        return ac;
    }
}
