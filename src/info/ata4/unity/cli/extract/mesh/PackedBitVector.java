/*
 ** 2014 July 10
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.extract.mesh;

import info.ata4.unity.serdes.UnityObject;
import java.nio.ByteBuffer;

// PackedBitVector
//   UInt32 m_NumItems
//   float m_Range
//   float m_Start
//   vector m_Data
//   UInt8 m_BitSize
class PackedBitVector {
    
    final Long numItems;
    final Float range;
    final Float start;
    final ByteBuffer data;
    final Integer bitSize;

    PackedBitVector(UnityObject obj) {
        numItems = obj.getValue("m_NumItems");
        range = obj.getValue("m_Range");
        start = obj.getValue("m_Start");
        data = obj.getValue("m_Data");
        bitSize = obj.getValue("m_BitSize");
    }
    
}
