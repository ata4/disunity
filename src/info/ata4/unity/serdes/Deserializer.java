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

import info.ata4.io.DataInputReader;
import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.asset.struct.ObjectPath;
import info.ata4.unity.asset.struct.TypeField;
import info.ata4.unity.cli.classfilter.ClassFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Deserializer for Unity objects.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class Deserializer {
    
    private static final int ALIGN = 4;
    
    private final AssetFile asset;
    private DataInputReader in;
    private ByteBuffer bb;
    private boolean debug = false;
    
    public Deserializer(AssetFile asset) {
        this.asset = asset;
    }
    
    public List<UnityObject> deserialize(ClassFilter cf) throws IOException {
        List<UnityObject> objList = new ArrayList<>();
        
        for (ObjectPath path : asset.getPaths()) {
            // skip filtered classes
            if (cf != null && !cf.accept(path)) {
                continue;
            }
            
            objList.add(deserialize(path));
        }
        
        return objList;
    }
    
    public UnityObject deserialize(ObjectPath path) throws IOException {
        bb = asset.getPathBuffer(path);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        in = DataInputReader.newReader(bb);
        
        Map<Integer, TypeField> typeFields = asset.getTypeTree().getFields();
        TypeField type = typeFields.get(path.getClassID());
        
        // check if the type information is available
        if (type == null) {
            throw new IllegalArgumentException("Class not found in type tree");
        }
        
        // read object
        final UnityObject obj = readObject(type);
        
        // check if all bytes have been read
        if (bb.hasRemaining()) {
            throw new IOException("Remaining bytes: " + bb.remaining());
        }

        return obj;
    }
    
    private UnityObject readObject(TypeField type) throws IOException {
        UnityObject obj = new UnityObject();
        obj.setName(type.getName());
        obj.setType(type.getType());
        
        for (TypeField subType : type.getChildren()) {
            int pos = 0;
            if (debug) {
                pos = bb.position();
            }
            
            UnityValue value = new UnityValue();
            value.setType(subType.getType());
            value.set(readValue(subType));

            if (subType.isForceAlign()) {
                in.align(ALIGN);
            }

            obj.get().put(subType.getName(), value);
            
            if (debug) {
                int bytes = bb.position() - pos;
                System.out.printf("0x%x: %s %s = %s, bytes: %d, flags: 0x%x 0x%x\n",
                        pos, value.getType(), subType.getName(), value.get(), bytes, subType.getFlags1(), subType.getFlags2());
            }
        }

        return obj;
    }
    
    private Object readValue(TypeField type) throws IOException {
        // check for complex objects with children
        if (!type.getChildren().isEmpty()) {
            switch (type.getType()) {
                // convert char arrays to string objects
                case "string":
                    return readString(type);
                
                // wrap collections in UnityValues
                case "map":
                case "vector":
                case "staticvector":
                case "set":
                    return readCollection(type);
                    
                // arrays need a special treatment
                case "Array":
                case "TypelessData":
                    return readArray(type);
                    
                default:
                    return readObject(type);
            }
        } else {
            // read primitive object
            switch (type.getType()) {
                // 1 byte
                case "bool":
                    return in.readBoolean();
                    
                case "SInt8":
                    return in.readByte();
                    
                case "UInt8":
                case "char":
                    return in.readUnsignedByte();
                
                // 2 bytes
                case "SInt16":
                case "short":
                    return in.readShort();

                case "UInt16":
                case "unsigned short":
                    return in.readUnsignedShort();

                // 4 bytes
                case "SInt32":
                case "int":
                    return in.readInt();

                case "UInt32":
                case "unsigned int":
                    return in.readUnsignedInt();
                
                case "float":
                    return in.readFloat();
                    
                // 8 bytes
                case "SInt64":
                case "long":
                    return in.readLong();
                
                case "UInt64":
                case "unsigned long":
                    return in.readUnsignedLong();

                case "double":
                    return in.readDouble();

                default:
                    throw new IOException("Unknown primitive type: " + type.getType());
            }
        }
    }
    
    private UnityValue readArray(TypeField type) throws IOException {
        List<TypeField> subTypes = type.getChildren();
        
        // there should be exactly two fields in an array object: size and data
        if (subTypes.size() != 2) {
            throw new IOException("Unexpected number of array fields: " + subTypes.size());
        }
        
        TypeField typeSize = subTypes.get(0);
        TypeField typeData = subTypes.get(1);
        
        // check name of the two array fields
        if (!typeSize.getName().equals("size")) {
            throw new IOException("Unexpected array size field: " + typeSize);
        }
        
        if (!typeData.getName().equals("data")) {
            throw new IOException("Unexpected array data field: " + typeData);
        }
        
        // read the size field
        int size = (int) readValue(typeSize);
        
        UnityValue value = new UnityValue();
        value.setType(typeData.getType());
        
        switch (typeData.getType()) {
            // read byte arrays natively and wrap them as ByteBuffers,
            // which is much faster and more efficient than a list of wrappped
            // Byte/Integer objects
            case "SInt8":
            case "UInt8":
            case "char":
                byte[] data = new byte[size];

                // NOTE: AudioClips "fake" the size of m_AudioData when the stream is
                // stored in a separate file. The array contains just an offset integer
                // in that case, so pay attention to the bytes remaining in the buffer
                // as well to avoid EOFExceptions.
                // TODO: is there a flag for this behavior?
                int bufsize = Math.min(size, (int) in.remaining());

                in.readFully(data, 0, bufsize);
                in.align(ALIGN);
                value.set(ByteBuffer.wrap(data));
                break;
                
            // read a list of objects
            default:
                List list = new ArrayList(size);
                for (int i = 0; i < size; i++) {
                    list.add(readValue(typeData));
                }
                value.set(list);
        }
        
        return value;
    }
    
    private UnityValue readCollection(TypeField type) throws IOException {
        // get Array field of the collection object
        return readArray(type.getChildren().get(0));
    }
    
    private UnityString readString(TypeField type) throws IOException {
        UnityValue array = readCollection(type);
        
        // strings use "char" arrays, so it should be wrapped in a ByteBuffer
        ByteBuffer buf = (ByteBuffer) array.get();
        
        return new UnityString(buf.array());
    }
    
    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
