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

// CompressedMesh
//   PackedBitVector m_Vertices
//   PackedBitVector m_UV
//   PackedBitVector m_BindPoses
//   PackedBitVector m_Normals
//   PackedBitVector m_Tangents
//   PackedBitVector m_Weights
//   PackedBitVector m_NormalSigns
//   PackedBitVector m_TangentSigns
//   PackedBitVector m_BoneIndices
//   PackedBitVector m_Triangles
//   PackedBitVector m_Colors
public class CompressedMesh {
    
    public final PackedBitVector vertices;
    public final PackedBitVector UV;
    public final PackedBitVector bindPoses;
    public final PackedBitVector normals;
    public final PackedBitVector tangents;
    public final PackedBitVector weights;
    public final PackedBitVector normalSigns;
    public final PackedBitVector tangentSigns;
    public final PackedBitVector boneIndices;
    public final PackedBitVector triangles;
    public final PackedBitVector colors;

    public CompressedMesh(UnityObject obj) {
        vertices = new PackedBitVector((UnityObject) obj.getValue("m_Vertices"));
        UV = new PackedBitVector((UnityObject) obj.getValue("m_UV"));
        bindPoses = new PackedBitVector((UnityObject) obj.getValue("m_BindPoses"));
        normals = new PackedBitVector((UnityObject) obj.getValue("m_Normals"));
        tangents = new PackedBitVector((UnityObject) obj.getValue("m_Tangents"));
        weights = new PackedBitVector((UnityObject) obj.getValue("m_Weights"));
        normalSigns = new PackedBitVector((UnityObject) obj.getValue("m_NormalSigns"));
        tangentSigns = new PackedBitVector((UnityObject) obj.getValue("m_TangentSigns"));
        boneIndices = new PackedBitVector((UnityObject) obj.getValue("m_BoneIndices"));
        triangles = new PackedBitVector((UnityObject) obj.getValue("m_Triangles"));
        colors = new PackedBitVector((UnityObject) obj.getValue("m_Colors"));
    }
    
}
