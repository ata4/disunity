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

import info.ata4.unity.asset.ObjectInfo;
import info.ata4.unity.asset.TypeNode;
import info.ata4.unity.asset.VersionInfo;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ObjectData {
    
    private final long id;
    private final VersionInfo versionInfo;
    private ObjectSerializer serializer;
    private ObjectInfo info;
    private ByteBuffer buffer;
    private TypeNode typeTree;
    private FieldNode instance;
    
    public ObjectData(long id, VersionInfo versionInfo) {
        this.id = id;
        this.versionInfo = versionInfo;
    }
    
    public long getID() {
        return id;
    }
    
    public VersionInfo getVersionInfo() {
        return versionInfo;
    }
    
    public ObjectSerializer getSerializer() {
        return serializer;
    }
    
    public void setSerializer(ObjectSerializer serializer) {
        this.serializer = serializer;
    }
    
    public ObjectInfo getInfo() {
        return info;
    }
    
    public void setInfo(ObjectInfo info) {
        this.info = info;
    }

    public ByteBuffer getBuffer() {
        if (buffer == null && instance != null && typeTree != null) {
            try {
                serializer.serialize(this);
            } catch (IOException ex) {
                throw new RuntimeTypeException(ex);
            }
        }
        return buffer;
    }
    
    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }
    
    public TypeNode getTypeTree() {
        return typeTree;
    }

    public void setTypeTree(TypeNode typeTree) {
        this.typeTree = typeTree;
    }

    public FieldNode getInstance() {
        if (instance == null && buffer != null && typeTree != null) {
            try {
                serializer.deserialize(this);
            } catch (IOException ex) {
                throw new RuntimeTypeException(ex);
            }
        }
        return instance;
    }

    public void setInstance(FieldNode instance) {
        this.instance = instance;
    }
    
    public String getName() {
        String name = getInstance().getString("m_Name");

        if (name == null || name.isEmpty()) {
            name = String.format("Object %d", id);
        }
        
        return name;
    }

    @Override
    public String toString() {
        return "Object " + getID() + " " + getInfo().toString();
    }
}
