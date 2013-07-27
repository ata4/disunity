/*
 ** 2013 July 24
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.serialization;

import info.ata4.unity.asset.Asset;
import info.ata4.unity.io.AssetInput;
import info.ata4.unity.struct.FieldNode;
import info.ata4.unity.struct.ObjectPath;
import info.ata4.util.io.ByteBufferInput;
import info.ata4.util.io.DataInputReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Deserializer for asset objects.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetDeserializer {
    
    private static final Logger L = Logger.getLogger(AssetDeserializer.class.getName());
    
    private final Asset asset;
    private AssetInput in;
    private ByteBuffer bb;

    public AssetDeserializer(Asset asset) {
        this.asset = asset;
    }
    
    public AssetObject deserializeObject(ObjectPath path) {
        // create a byte buffer for the data area
        ByteBuffer bbData = asset.getDataBuffer();
        bbData.position(path.offset);
        bb = bbData.slice();
        bb.limit(path.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        
        // create asset input
        in = new AssetInput(new DataInputReader(new ByteBufferInput(bb)));

        FieldNode classNode = asset.getFieldTree().get(path.classID1);
        
        L.log(Level.FINEST, "class {0} {1}", new Object[]{classNode.name, classNode.type});

        AssetObject ac = new AssetObject();
        ac.setName(classNode.name);
        ac.setType(classNode.type);

        for (FieldNode fieldNode : classNode) {
            ac.put(fieldNode.name, deserializeField(fieldNode));
        }
        
        return ac;
    }
    
    private AssetObject deserializeObject(FieldNode field) {
        L.log(Level.FINEST, "class {0} {1}", new Object[]{field.name, field.type});
        
        AssetObject ac = new AssetObject();
        ac.setName(field.name);
        ac.setType(field.type);

        for (FieldNode fieldNode : field) {
            ac.put(field.name, deserializeField(fieldNode));
        }
        
        return ac;
    }
    
    private AssetField deserializeField(FieldNode field) {
        AssetField af = new AssetField();
        
        Object value = null;
        
        try {
            value = deserializePrimitive(field);

            if (value == null) {
                value = deserializeObject(field);
            }
        } catch (Exception ex) {
            L.log(Level.WARNING, "Can't read value of field " + field.name, ex);
        }
        
        L.log(Level.FINEST, "field {0} ({1}) = {2}", new Object[]{field.name, field.type, value});
        
        af.setName(field.name);
        af.setType(field.type);
        af.setValue(value);

        return af;
    }
    
    private Object deserializePrimitive(FieldNode field) throws IOException {
        switch (field.type) {
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

            default:
                return null;
        }
    }
}
