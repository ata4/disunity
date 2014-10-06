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

// SubMesh (Unity 4)
//   unsigned int firstByte
//   unsigned int indexCount
//   int topology
//   unsigned int firstVertex
//   unsigned int vertexCount
//   AABB localAABB

// SubMesh (Unity 3)
//   UInt32 firstByte
//   UInt32 indexCount
//   UInt32 isTriStrip
//   UInt32 triangleCount
//   UInt32 firstVertex
//   UInt32 vertexCount
//   AABB localAABB

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class SubMesh extends UnityObject {

    public SubMesh(FieldNode node) {
        super(node);
    }
    
    public Number getFirstByte() {
        return node.getChildValue("firstByte");
    }
    
    public Number getIndexCount() {
        return node.getChildValue("indexCount");
    }
    
    public Number getTopology() {
        return node.getChildValue("topology");
    }
    
    public Number getFirstVertex() {
        return node.getChildValue("firstVertex");
    }
    
    public Number getVertexCount() {
        return node.getChildValue("vertexCount");
    }
}
