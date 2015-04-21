/*
 ** 2014 September 22
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.rtti;

import info.ata4.unity.asset.Type;
import info.ata4.util.collection.Node;
import java.math.BigInteger;
import java.util.Objects;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FieldNode extends Node<FieldNode> {
    
    private Type type;
    private Object value;    

    public Type getType() {
        return type;
    }

    public void setType(Type field) {
        this.type = field;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
    
    public FieldNode getChild(String name) {
        for (FieldNode child : this) {
            if (child.getType().fieldName().equals(name)) {
                return child;
            }
        }
        return null;
    }

    public <T> T getChildValue(String name, Class<T> type) {
        FieldNode child = getChild(name);
        if (child == null) {
            return null;
        }
        
        Object v = child.getValue();
        if (!type.isInstance(v)) {
            throw new RuntimeTypeException(String.format("Wrong type for %s: expected %s, but got %s", name, type, v.getClass()));
        }
        
        return (T) v;
    }

    public void setChildValue(String name, Object value) {
        FieldNode child = getChild(name);
        if (child == null) {
            throw new RuntimeTypeException("Field " + name + " doesn't exist");
        }
        
        child.setValue(value);
    }
    
    public <T> T getArrayData(Class<T> type) {
        FieldNode arrayField = getChild("Array");
        if (arrayField == null) {
            throw new RuntimeTypeException("Field is not an array");
        }
        
        return arrayField.getChildValue("data", type);
    }

    public void setArrayData(Object value) {
        FieldNode arrayField = getChild("Array");
        if (arrayField == null) {
            throw new RuntimeTypeException("Field is not an array");
        }
        
        arrayField.setChildValue("data", value);
    }
    
    public <T> T getChildArrayData(String name, Class<T> type) {
        return getChild(name).getArrayData(type);
    }

    public void setChildArrayData(String name, Object value) {
        getChild(name).setArrayData(value);
    }

    public byte getSInt8(String name) {
        return getChildValue(name, Number.class).byteValue();
    }
    
    public void setSInt8(String name, byte v) {
        setChildValue(name, v);
    }

    public short getUInt8(String name) {
        return (short) (getChildValue(name, Number.class).byteValue() & 0xff);
    }
    
    public void setUInt8(String name, byte v) {
        setChildValue(name, v);
    }

    public short getSInt16(String name) {
        return getChildValue(name, Number.class).shortValue();
    }
    
    public void setSInt16(String name, short v) {
        setChildValue(name, v);
    }

    public int getUInt16(String name) {
        return getChildValue(name, Number.class).shortValue() & 0xffff;
    }
    
    public void setUInt16(String name, int v) {
        setChildValue(name, v & 0xffff);
    }

    public int getSInt32(String name) {
        return getChildValue(name, Number.class).intValue();
    }
    
    public void setSInt32(String name, int v) {
        setChildValue(name, v);
    }

    public long getUInt32(String name) {
        return getChildValue(name, Number.class).longValue() & 0xffffffffL;
    }
    
    public void getUInt32(String name, long v) {
        setChildValue(name, v & 0xffffffffL);
    }

    public long getSInt64(String name) {
        return getChildValue(name, Number.class).longValue();
    }
    
    public void setSInt64(String name, long v) {
        setChildValue(name, v);
    }

    public BigInteger getUInt64(String name) {
        return getChildValue(name, BigInteger.class);
    }
    
    public void setUInt64(String name, BigInteger v) {
        setChildValue(name, v);
    }

    public float getFloat(String name) {
        return getChildValue(name, Number.class).floatValue();
    }
    
    public void setFloat(String name, float v) {
        setChildValue(name, v);
    }

    public double getDouble(String name) {
        return getChildValue(name, Number.class).doubleValue();
    }
    
    public void setDouble(String name, double v) {
        setChildValue(name, v);
    }

    public boolean getBoolean(String name) {
        return getChildValue(name, Boolean.class);
    }
    
    public void setBoolean(String name, boolean v) {
        setChildValue(name, v);
    }

    public String getString(String name) {
        return getChildValue(name, String.class);
    }
    
    public void setString(String name, String v) {
        setChildValue(name, v);
    }
    
    public FieldNode getObject(String name) {
        return getChildValue(name, FieldNode.class);
    }
    
    public void setObject(String name, FieldNode v) {
        setChildValue(name, v);
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 97 * hash + Objects.hashCode(this.type);
        hash = 97 * hash + Objects.hashCode(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FieldNode other = (FieldNode) obj;
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }
}
