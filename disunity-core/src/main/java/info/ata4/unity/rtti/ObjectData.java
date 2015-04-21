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
    
    public long ID() {
        return id;
    }
    
    public VersionInfo versionInfo() {
        return versionInfo;
    }
    
    public ObjectSerializer serializer() {
        return serializer;
    }
    
    public void serializer(ObjectSerializer serializer) {
        this.serializer = serializer;
    }
    
    public ObjectInfo info() {
        return info;
    }
    
    public void info(ObjectInfo info) {
        this.info = info;
    }

    public ByteBuffer buffer() {
        if (buffer == null && instance != null && typeTree != null) {
            try {
                serializer.serialize(this);
            } catch (IOException ex) {
                throw new RuntimeTypeException(ex);
            }
        }
        return buffer;
    }
    
    public void buffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }
    
    public TypeNode typeTree() {
        return typeTree;
    }

    public void typeTree(TypeNode typeTree) {
        this.typeTree = typeTree;
    }

    public FieldNode instance() {
        if (instance == null && buffer != null && typeTree != null) {
            try {
                serializer.deserialize(this);
            } catch (IOException ex) {
                throw new RuntimeTypeException(ex);
            }
        }
        return instance;
    }

    public void instance(FieldNode instance) {
        this.instance = instance;
    }
    
    public String name() {
        String name = instance().getString("m_Name");

        if (name == null || name.isEmpty()) {
            name = String.format("Object %d", id);
        }
        
        return name;
    }

    @Override
    public String toString() {
        return "Object " + ID() + " " + info().toString();
    }
}
