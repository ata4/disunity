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
import info.ata4.unity.struct.FieldNode;
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
    
    public UnityObject deserialize(ObjectPath path) throws DeserializationException {
        // create a byte buffer for the data area
        ByteBuffer bbData = asset.getDataBuffer();
        bbData.position(path.offset);
        bb = bbData.slice();
        bb.limit(path.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        // create asset input
        in = new SerializedInput(new DataInputReader(new ByteBufferInput(bb)));

        FieldNode classNode = asset.getTypeTree().get(path.classID2);
        
        if (classNode == null) {
            throw new DeserializationException("ClassID not found in field tree");
        }
        
        UnityObject ac = new UnityObject();
        ac.setName(classNode.name);
        ac.setType(classNode.type);

        for (FieldNode fieldNode : classNode) {
            ac.put(fieldNode.name, readField(fieldNode));
        }
        
        // read remaining bytes
        try {
            in.align();
        } catch (IOException ex) {
            throw new DeserializationException("Alignment failed");
        }
        
        // check if all bytes have been read
        if (bb.hasRemaining()) {
            throw new DeserializationException("Remaining bytes: " + bb.remaining());
        }
         
        return ac;
    }
    
    private UnityField readField(FieldNode field) throws DeserializationException {
        UnityField af = new UnityField();
        af.setName(field.name);
        af.setType(field.type);
        
        try {
            af.setValue(readFieldValue(field));
            if (field.isForceAlign()) {
                in.align();
            }
        } catch (IOException ex) {
            throw new DeserializationException("Can't read value of field " + field.name, ex);
        }

        return af;
    }

    private Object readFieldValue(FieldNode field) throws IOException, DeserializationException {
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
            case "char":
                return in.readByte();

            case "UInt8":
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
                return readArray(field.get(0));
                
            case "set":
                return readSet(field);
                
            case "map":
                return readMap(field);
            
            case "Array":
                return readArray(field);
                
            case "TypelessData":
                return in.readByteArray();

            default:
                return readObject(field);
        }
    }
    
    private List<Object> readArray(FieldNode field) throws IOException, DeserializationException {
        int size = in.readInt();
        FieldNode fieldData = field.get(1);
        List<Object> objList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            objList.add(readFieldValue(fieldData));
        }
        return objList;
    }
    
    private Map<Object, Object> readMap(FieldNode field) throws IOException, DeserializationException {
        List<Object> pairList = readArray(field.get(0));
        Map<Object, Object> map = new HashMap<>();
        
        for (Object pair : pairList) {
            UnityObject pairUnity = (UnityObject) pair;
            Object first = pairUnity.get("first").getValue();
            Object second = pairUnity.get("second").getValue();
            map.put(first, second);
        }
        
        return map;
    }
    
    private Set<Object> readSet(FieldNode field) throws IOException, DeserializationException {
        Set<Object> set = new HashSet<>();
        set.addAll(readArray(field.get(0)));
        return set;
    }
    
    private UnityObject readObject(FieldNode field) throws DeserializationException {
        UnityObject ac = new UnityObject();
        ac.setName(field.name);
        ac.setType(field.type);

        for (FieldNode fieldNode : field) {
            ac.put(fieldNode.name, readField(fieldNode));
        }
        
        return ac;
    }
}
