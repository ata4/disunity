/*
 ** 2014 September 20
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.rtti;

import info.ata4.io.DataReader;
import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.io.socket.Sockets;
import info.ata4.unity.asset.AssetVersionInfo;
import info.ata4.unity.asset.FieldType;
import info.ata4.unity.asset.FieldTypeNode;
import info.ata4.unity.asset.ObjectPath;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ObjectData {
    
    private static final boolean DEBUG = false;
    private static final int ALIGNMENT = 4;
    
    private AssetVersionInfo versionInfo;
    private ObjectPath path;
    private ByteBuffer buffer;
    private ByteBuffer soundBuffer;
    private FieldTypeNode typeTree;
    private FieldNode instance;

    public ObjectPath getPath() {
        return path;
    }

    public void setPath(ObjectPath path) {
        this.path = path;
    }
    
    public AssetVersionInfo getVersionInfo() {
        return versionInfo;
    }

    public void setVersionInfo(AssetVersionInfo versionInfo) {
        this.versionInfo = versionInfo;
    }

    public ByteBuffer getBuffer() {
        if (buffer == null && instance != null && typeTree != null) {
            try {
                serialize();
            } catch (IOException ex) {
                throw new RuntimeTypeException(ex);
            }
        }
        return buffer;
    }
    
    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }
    
    public ByteBuffer getSoundBuffer() {
        return soundBuffer;
    }
    
    public void setSoundBuffer(ByteBuffer soundBuffer) {
        this.soundBuffer = soundBuffer;
    }
    
    public FieldTypeNode getTypeTree() {
        return typeTree;
    }

    public void setTypeTree(FieldTypeNode typeTree) {
        this.typeTree = typeTree;
    }
    
    public String getName() {
        String name = getInstance().getChildValue("m_Name");

        if (name == null || name.isEmpty()) {
            name = String.format("Object %d", getPath().getPathID());
        }
        
        return name;
    }

    public FieldNode getInstance() {
        if (instance == null && buffer != null && typeTree != null) {
            try {
                deserialize();
            } catch (IOException ex) {
                throw new RuntimeTypeException(ex);
            }
        }
        return instance;
    }

    public void setInstance(FieldNode instance) {
        this.instance = instance;
    }
    
    private void serialize() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private void deserialize() throws IOException {
        DataReader in = new DataReader(Sockets.forByteBuffer(buffer));
        in.setSwap(versionInfo.swapRequired());
        in.position(0);
        
        // read object
        instance = readObject(in, typeTree);
        
        // check if all bytes have been read
        if (in.hasRemaining()) {
            throw new RuntimeTypeException("Remaining bytes: " + in.remaining());
        }
    }
    
    private FieldNode readObject(DataReader in, FieldTypeNode typeNode) throws IOException {
        FieldNode fieldNode = new FieldNode(typeNode);
        
        FieldType type = typeNode.getType();
        
        // if the type has no children, it has a primitve value
        if (typeNode.isEmpty() && type.getSize() > 0) {
            fieldNode.setValue(readPrimitiveValue(in, type, -1));
        }
        
        // read object fields
        for (FieldTypeNode childTypeNode : typeNode) {
            FieldType childType = childTypeNode.getType();
            
            // Check if the current node is an array and if the current field is
            // "data". In that case, "data" needs to be read "size" times.
            if (type.getIsArray() && childType.getFieldName().equals("data")) {
                int size = fieldNode.getChildValue("size");
                
                FieldNode childFieldNode = new FieldNode(childTypeNode);

                // if the child type has no children, it has a primitve array
                if (childTypeNode.isEmpty()) {
                    childFieldNode.setValue(readPrimitiveValue(in, childType, size));
                } else {
                    // read list of object nodes
                    List<FieldNode> childFieldNodes = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        childFieldNodes.add(readObject(in, childTypeNode));
                    }
                    childFieldNode.setValue(childFieldNodes);
                }
                
                fieldNode.add(childFieldNode);
            } else {
                fieldNode.add(readObject(in, childTypeNode));
            }
        }
        
        // convert byte buffers to string instances in "string" fields for convenience
        if (fieldNode.getType().getTypeName().equals("string")) {
            // strings use "char" arrays, so it should be wrapped in a ByteBuffer
            ByteBuffer buf = fieldNode.getChild("Array").getChildValue("data");
            fieldNode.setValue(new String(buf.array(), "UTF-8"));
        }
        
        return fieldNode;
    }
    
    private Object readPrimitiveValue(DataReader in, FieldType type, int size) throws IOException, RuntimeTypeException {
        long pos = 0;
        if (DEBUG) {
            pos = in.position();
        }
        
        Object value;
        if (size == -1) {
            value = readPrimitive(in, type);
            if (type.isForceAlign()) {
                 in.align(ALIGNMENT);
            }
        } else {
            value = readPrimitiveArray(in, type, size);
            if (versionInfo.getAssetVersion() > 5) {
                // arrays always need to be aligned in newer versions
                in.align(ALIGNMENT);
            }
        }
        
        if (DEBUG) {
            long bytes = in.position() - pos;
            System.out.printf("0x%x: %s %s = %s, b: %d, v: %d, f: 0x%x, s: %d\n",
                    pos, type.getTypeName(), type.getFieldName(), value, bytes,
                    type.getVersion(), type.getMetaFlag(), size);
        }
        
        return value;
    }
    
    private Object readPrimitive(DataReader in, FieldType type) throws IOException, RuntimeTypeException {
        switch (type.getTypeName()) {
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
                throw new RuntimeTypeException("Unknown primitive type: " + type.getTypeName());
        }
    }
    
    private Object readPrimitiveArray(DataReader in, FieldType type, int size) throws IOException, RuntimeTypeException {
        switch (type.getTypeName()) {
            // read byte arrays natively and wrap them as ByteBuffers,
            // which is much faster and more efficient than a list of wrappped
            // Byte/Integer objects
            case "SInt8":
            case "UInt8":
                ByteBuffer buf;
                
                // NOTE: AudioClips "fake" the size of m_AudioData when the stream is
                // stored in a separate file. The array contains just an offset integer
                // in that case, so pay attention to the bytes remaining in the buffer
                // as well to avoid EOFExceptions.
                long remaining = in.remaining();
                if (size > remaining && remaining == 4) {
                    int offset = in.readInt();
                    // create empty sound buffer in case the .resS file doesn't
                    // exist
                    if (soundBuffer != null) {
                        buf = ByteBufferUtils.getSlice(soundBuffer, offset, size);
                    } else {
                        buf = ByteBufferUtils.allocate(size);
                    }
                } else {
                    buf = ByteBufferUtils.allocate(size);
                    in.readBuffer(buf);
                }

                buf.clear();
                return buf;

            // always wrap char arrays so array() is available on the buffer, which
            // is required to convert them to Java strings in readString()
            case "char":
                byte[] raw = new byte[size];
                in.readFully(raw, 0, size);
                return ByteBuffer.wrap(raw);

            // read a list of primitive objects
            default:
                List list = new ArrayList(size);
                for (int i = 0; i < size; i++) {
                    list.add(readPrimitive(in, type));
                }
                return list;
        }
    }
}
