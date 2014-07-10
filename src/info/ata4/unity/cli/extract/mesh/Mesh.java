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
import java.util.ArrayList;
import java.util.List;

// Mesh
//   string m_Name
//   vector m_SubMeshes
//   BlendShapeData m_Shapes
//   vector m_BindPose
//   vector m_BoneNameHashes
//   unsigned int m_RootBoneNameHash
//   UInt8 m_MeshCompression
//   UInt8 m_StreamCompression
//   bool m_IsReadable
//   bool m_KeepVertices
//   bool m_KeepIndices
//   vector m_IndexBuffer
//   vector m_Skin
//   VertexData m_VertexData
//   CompressedMesh m_CompressedMesh
//   AABB m_LocalAABB
//   int m_MeshUsageFlags
class Mesh {
    
    final String name;
    final ByteBuffer indexBuffer;
    final Integer meshCompression;
    final VertexData vertexData;
    final List<SubMesh> subMeshes;
    final CompressedMesh compressedMesh;

    Mesh(UnityObject obj) {
        name = obj.getValue("m_Name");
        indexBuffer = obj.getValue("m_IndexBuffer");
        meshCompression = obj.getValue("m_MeshCompression");
        vertexData = new VertexData((UnityObject) obj.getValue("m_VertexData"));
        List<UnityObject> subMeshObjects = obj.getValue("m_SubMeshes");
        subMeshes = new ArrayList<>();
        for (UnityObject subMeshObject : subMeshObjects) {
            subMeshes.add(new SubMesh(subMeshObject));
        }
        compressedMesh = new CompressedMesh((UnityObject) obj.getValue("m_CompressedMesh"));
    }
    
}
