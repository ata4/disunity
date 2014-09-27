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

import info.ata4.util.collection.TreeNode;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FieldNode extends TreeNode<FieldNode> {
    
    private FieldType type;
    private Object value;    

    public FieldNode(FieldTypeNode typeNode) {
        type = typeNode.getType();
    }

    public FieldType getType() {
        return type;
    }

    public void setType(FieldType field) {
        this.type = field;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public <T> T getField(String name) {
        for (FieldNode child : children) {
            if (child.getType().getFieldName().equals(name)) {
                return (T) child.getValue();
            }
        }
        
        return null;
    }
    
    public boolean setField(String name, Object value) {
        for (FieldNode child : children) {
            if (child.getType().getFieldName().equals(name)) {
                child.setValue(value);
                return true;
            }
        }
        
        return false;
    }
}
