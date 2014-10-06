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

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class CompressedMesh extends UnityObject {

    public CompressedMesh(FieldNode node) {
        super(node);
    }
    
    public PackedBitVector getVertices() {
        return new PackedBitVector(node.getChild("m_Vertices"));
    }

    public PackedBitVector getUV() {
        return new PackedBitVector(node.getChild("m_UV"));
    }

    public PackedBitVector getBindPoses() {
        return new PackedBitVector(node.getChild("m_BindPoses"));
    }

    public PackedBitVector getNormals() {
        return new PackedBitVector(node.getChild("m_Normals"));
    }

    public PackedBitVector getTangents() {
        return new PackedBitVector(node.getChild("m_Tangents"));
    }

    public PackedBitVector getWeights() {
        return new PackedBitVector(node.getChild("m_Weights"));
    }

    public PackedBitVector getNormalSigns() {
        return new PackedBitVector(node.getChild("m_NormalSigns"));
    }

    public PackedBitVector getTangentSigns() {
        return new PackedBitVector(node.getChild("m_TangentSigns"));
    }

    public PackedBitVector getBoneIndices() {
        return new PackedBitVector(node.getChild("m_BoneIndices"));
    }

    public PackedBitVector getTriangles() {
        return new PackedBitVector(node.getChild("m_Triangles"));
    }

    public PackedBitVector getColors() {
        return new PackedBitVector(node.getChild("m_Colors"));
    }
    
}
