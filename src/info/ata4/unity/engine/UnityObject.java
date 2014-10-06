/*
 ** 2014 October 06
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.engine;

import info.ata4.unity.rtti.FieldNode;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class UnityObject {
    
    protected final FieldNode node;

    public UnityObject(FieldNode node) {
        this.node = node;
    }

    public String getName() {
        return node.getChildValue("m_Name");
    }
}
