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

import info.ata4.unity.serdes.UnityObject;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

// Mesh (Unity 4)
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

// Mesh (Unity 3)
//   string m_Name
//   vector m_SubMeshes
//   UInt8 m_MeshCompression
//   vector m_IndexBuffer
//   vector m_Skin
//   vector m_BindPose
//   VertexData m_VertexData
//   CompressedMesh m_CompressedMesh
//   AABB m_LocalAABB
//   int m_MeshUsageFlags

public class Mesh {
    
    public final String name;
    public final ByteBuffer indexBuffer;
    public final Integer meshCompression;
    public final VertexData vertexData;
    public final List<SubMesh> subMeshes;
    public final CompressedMesh compressedMesh;

    public Mesh(UnityObject obj) {
        name = obj.getValue("m_Name");
        indexBuffer = obj.getValue("m_IndexBuffer");
        meshCompression = obj.getValue("m_MeshCompression");
        vertexData = obj.getObject("m_VertexData", VertexData.class);
        List<UnityObject> subMeshObjects = obj.getValue("m_SubMeshes");
        subMeshes = new ArrayList<>();
        for (UnityObject subMeshObject : subMeshObjects) {
            subMeshes.add(new SubMesh(subMeshObject));
        }
        compressedMesh = obj.getObject("m_CompressedMesh", CompressedMesh.class);
    }
    
}
