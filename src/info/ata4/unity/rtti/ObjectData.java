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

import info.ata4.io.DataInputReader;
import info.ata4.io.buffer.ByteBufferUtils;
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
    
    private ObjectPath path;
    private ByteBuffer buffer;
    private FieldTypeNode typeTree;
    private FieldNode instance;

    public ObjectPath getPath() {
        return path;
    }

    public void setPath(ObjectPath path) {
        this.path = path;
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
    
    public FieldTypeNode getTypeTree() {
        return typeTree;
    }

    public void setTypeTree(FieldTypeNode typeTree) {
        this.typeTree = typeTree;
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
        DataInputReader in = DataInputReader.newReader(buffer);
        in.setSwap(true);
        in.position(0);
        
        // read object
        instance = readObject(in, typeTree);
        
        // check if all bytes have been read
        if (in.hasRemaining()) {
            throw new RuntimeTypeException("Remaining bytes: " + in.remaining());
        }
    }
    
    private FieldNode readObject(DataInputReader in, FieldTypeNode typeNode) throws IOException {
        FieldNode fieldNode = new FieldNode(typeNode);
        
        for (FieldTypeNode childTypeNode : typeNode.getChildren()) {
            FieldNode childFieldNode = new FieldNode(childTypeNode);
            
            long pos = 0;
            if (DEBUG) {
                pos = in.position();
            }
            
            childFieldNode.setValue(readValue(in, childTypeNode));

            if (childFieldNode.getType().isForceAlign()) {
                in.align(ALIGNMENT);
            }

            if (DEBUG) {
                long bytes = in.position() - pos;
                FieldType type = childFieldNode.getType();
                System.out.printf("0x%x: %s %s = %s, bytes: %d, flags: 0x%x 0x%x\n",
                        pos, type.getTypeName(), type.getFieldName(), childFieldNode.getValue(), bytes, type.getFlags1(), type.getFlags2());
            }
            
            fieldNode.getChildren().add(childFieldNode);
        }

        return fieldNode;
    }
    
    private Object readValue(DataInputReader in, FieldTypeNode typeNode) throws IOException, RuntimeTypeException {
        FieldType fieldType = typeNode.getType();
        if (!typeNode.getChildren().isEmpty()) {
            switch (fieldType.getTypeName()) {
                // convert char arrays to string objects
                case "string":
                    return readString(in, typeNode);

                // arrays need a special treatment
                case "Array":
                case "TypelessData":
                    return readArray(in, typeNode);
                    
                default:
                    return readObject(in, typeNode);
            }
        } else {
            switch (fieldType.getTypeName()) {
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
                    throw new RuntimeTypeException("Unknown primitive type: " + fieldType.getTypeName());
            }
        }
    }
    
    private FieldNode readArray(DataInputReader in, FieldTypeNode typeNode) throws IOException, RuntimeTypeException {
        List<FieldTypeNode> children = typeNode.getChildren();
        
        // there should be exactly two fields in an array object: size and data
        if (children.size() != 2) {
            throw new RuntimeTypeException("Unexpected number of array fields: " + children.size());
        }
        
        FieldTypeNode nodeSize = children.get(0);
        FieldTypeNode nodeData = children.get(1);
        
        FieldType typeSize = nodeSize.getType();
        FieldType typeData = nodeData.getType();
        
        // check name of the two array fields
        if (!typeSize.getFieldName().equals("size")) {
            throw new RuntimeTypeException("Unexpected array size field: " + typeSize);
        }
        
        if (!typeData.getFieldName().equals("data")) {
            throw new RuntimeTypeException("Unexpected array data field: " + typeData);
        }
        
        // read the size field
        int size = (int) readValue(in, nodeSize);
        
        FieldNode value = new FieldNode(typeNode);
        
        switch (typeData.getTypeName()) {
            // read byte arrays natively and wrap them as ByteBuffers,
            // which is much faster and more efficient than a list of wrappped
            // Byte/Integer objects
            case "SInt8":
            case "UInt8":
                ByteBuffer buf = ByteBufferUtils.allocate(size);
                
                // NOTE: AudioClips "fake" the size of m_AudioData when the stream is
                // stored in a separate file. The array contains just an offset integer
                // in that case, so pay attention to the bytes remaining in the buffer
                // as well to avoid EOFExceptions.
                // TODO: is there a flag for this behavior?
                buf.limit(Math.min(size, (int) in.remaining()));
                
                in.readBuffer(buf);
                in.align(ALIGNMENT);
                
                buf.clear();
                
                value.setValue(buf);
                break;
                
            // always wrap char arrays so array() is available on the buffer, which
            // is required to convert them to Java strings in readString()
            case "char":
                byte[] raw = new byte[size];
                in.readFully(raw, 0, size);
                in.align(ALIGNMENT);
                value.setValue(ByteBuffer.wrap(raw));
                break;
                
            // read a list of objects
            default:
                List list = new ArrayList(size);
                for (int i = 0; i < size; i++) {
                    list.add(readValue(in, nodeData));
                }
                value.setValue(list);
        }
        
        return value;
    }
    
    private String readString(DataInputReader in, FieldTypeNode typeNode) throws IOException {
        FieldNode array = readArray(in, typeNode.getChildren().get(0));
        
        // strings use "char" arrays, so it should be wrapped in a ByteBuffer
        ByteBuffer buf = (ByteBuffer) array.getValue();
        
        return new String(buf.array());
    }
}
