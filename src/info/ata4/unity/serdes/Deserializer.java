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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        
        bbAsset = ByteBufferUtils.getSlice(bbAssets, path.getOffset(), path.getLength());
        bbAsset.order(ByteOrder.LITTLE_ENDIAN);
        
        if (debug) {
            try {
                Path dumpFile = Paths.get(String.format("0x%x.bin", path.getOffset()));
                ByteBufferUtils.save(dumpFile, bbAsset);
                bbAsset.rewind();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        in = new SerializedInput(new DataInputReader(bbAsset));
        
        Map<Integer, AssetFieldType> classMapping = asset.getClassType().getMapping();
        AssetFieldType classNode = classMapping.get(path.getClassID());
        
        if (classNode == null) {
            throw new DeserializationException("Class not found in type tree");
        }
        
        if (classNode.getChildren().isEmpty()) {
            classNode = classMapping.get(path.getClassID());
        }
        
        UnityObject ac = new UnityObject(classNode.getType());
        ac.setName(classNode.getName());

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
        UnityObject ac = new UnityObject(field.getType());
        ac.setName(field.getName());
        
        for (AssetFieldType fieldNode : field) {
            ac.addField(readField(fieldNode));
        }
        
        return ac;
    }
    
    private UnityField readField(AssetFieldType field) throws DeserializationException {
        UnityField af = new UnityField(field.getType());
        af.setName(field.getName());
        
        int pos = 0;
        
        if (debug) {
            pos = bbAsset.position();
        }
        
        try {
            af.setValue(readFieldValue(field));
        } catch (IOException ex) {
            throw new DeserializationException("Can't read value of field " + field.getName(), ex);
        }
        
        if (debug) {
            int bytes = bbAsset.position() - pos;
            System.out.printf("0x%x: %s %s = %s, bytes: %d, flags: 0x%x 0x%x\n",
                    pos, af.getType(), af.getName(), af.getValue(), bytes, field.getFlags1(), field.getFlags2());
        }

        return af;
    }

    private Object readFieldValue(AssetFieldType field) throws IOException, DeserializationException {
        Object value;
        
        if (field.getChildren().isEmpty()) {
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
        switch (field.getType()) {
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
                throw new DeserializationException("Unknown primitive type: " + field.getType());
        }
    }
    
    private Object readComplex(AssetFieldType field) throws IOException, DeserializationException {
        switch (field.getType()) {
            case "string":
                return in.readString();
            
            case "map":
            case "vector":
            case "staticvector":
            case "set":
                return readArray(field.getChildren().get(0));
            
            case "Array":
            case "TypelessData":
                return readArray(field);
                
            default:
                return readObject(field);
        }
    }
    
    private UnityType readArray(AssetFieldType field) throws IOException, DeserializationException {
        List<AssetFieldType> children = field.getChildren();
        AssetFieldType sizeField = children.get(0);
        AssetFieldType dataField = children.get(1);
        int size = (int) readFieldValue(sizeField);
        String dataType = dataField.getType();
        
        if (dataType.equals("UInt8") || dataType.equals("char")) {
            // read byte arrays natively and wrap them as ByteBuffers, which is
            // much faster and more efficient than a list of bytes wrapped as
            // integer objects
            byte[] data = in.readByteArray(size);
            return new UnityBuffer(dataType, data);
        } else {
            // read a list of objects
            UnityList uarray = new UnityList(dataType);
            List<Object> objList = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                objList.add(readFieldValue(dataField));
            }
            uarray.setList(objList);
            return uarray;
        }
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
