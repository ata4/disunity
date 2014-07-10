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

// SubMesh
//   unsigned int firstByte
//   unsigned int indexCount
//   int topology
//   unsigned int firstVertex
//   unsigned int vertexCount
//   AABB localAABB
class SubMesh {
    
    final Long firstByte;
    final Long indexCount;
    final Integer topology;
    final Long firstVertex;
    final Long vertexCount;

    SubMesh(UnityObject obj) {
        firstByte = obj.getValue("firstByte");
        indexCount = obj.getValue("indexCount");
        topology = obj.getValue("topology");
        firstVertex = obj.getValue("firstVertex");
        vertexCount = obj.getValue("vertexCount");
    }
    
}
