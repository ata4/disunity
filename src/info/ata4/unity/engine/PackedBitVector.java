/*
 ** 2014 July 10
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.engine;

import info.ata4.unity.rtti.FieldNode;
import java.nio.ByteBuffer;

// PackedBitVector
//   UInt32 m_NumItems
//   float m_Range
//   float m_Start
//   vector m_Data
//   UInt8 m_BitSize

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class PackedBitVector extends UnityObject {

    public PackedBitVector(FieldNode node) {
        super(node);
    }

    public Long getNumItems() {
        return node.getChildValue("m_NumItems");
    }
    
    public Float getRange() {
        return node.getChildValue("m_Range");
    }
    
    public Float getStart() {
        return node.getChildValue("m_Start");
    }
    
    public ByteBuffer getData() {
        return node.getChildArray("m_Data");
    }
    
    public Integer getBitSize() {
        return node.getChildValue("m_BitSize");
    }
    
}
