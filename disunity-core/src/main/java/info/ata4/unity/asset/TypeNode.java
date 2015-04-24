/*
 ** 2015 April 15
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.asset;

import info.ata4.util.collection.Node;
import java.util.Objects;

/**
 * Type tree node class.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @unity TypeNode
 */
public class TypeNode extends Node<TypeNode> {
    
    private Type type;

    public Type type() {
        return type;
    }

    public void type(Type type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 31 * hash + Objects.hashCode(this.type);
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
        final TypeNode other = (TypeNode) obj;
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        return super.equals(obj);
    }
}
