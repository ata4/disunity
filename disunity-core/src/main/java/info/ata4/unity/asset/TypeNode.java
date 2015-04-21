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

/**
 * Type tree node class.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @unity TypeNode
 */
public class TypeNode extends Node<TypeNode> {
    
    private Type type;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
