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

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class Mesh extends UnityObject {

    public Mesh(FieldNode node) {
        super(node);
    }

    public ByteBuffer getIndexBuffer() {
        return node.getChildArray("m_IndexBuffer");
    }

    public Integer getMeshCompression() {
        return node.getChildValue("m_MeshCompression");
    }

    public VertexData getVertexData() {
        return new VertexData(node.getChild("m_VertexData"));
    }

    public List<SubMesh> getSubMeshes() {
        FieldNode subMeshes = node.getChild("m_SubMeshes");
        List<SubMesh> subMeshesList = new ArrayList<>();
        
        for (FieldNode subMesh : subMeshes) {
            subMeshesList.add(new SubMesh(subMesh));
        }
        
        return subMeshesList;
    }

    public CompressedMesh getCompressedMesh() {
        return new CompressedMesh(node.getChild("m_CompressedMesh"));
    }
}
