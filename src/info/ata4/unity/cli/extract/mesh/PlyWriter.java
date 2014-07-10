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

import info.ata4.unity.DisUnity;
import info.ata4.unity.engine.Mesh;
import info.ata4.unity.engine.SubMesh;
import info.ata4.unity.engine.struct.Color32;
import info.ata4.unity.engine.struct.Vector2f;
import info.ata4.unity.engine.struct.Vector3f;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
class PlyWriter extends MeshWriter {
    
    private PrintStream ps;

    PlyWriter(MeshHandler handler) {
        super(handler);
    }
    
    @Override
    void write(MeshData meshData) throws IOException {
        Mesh mesh = meshData.getMesh();

        List<Vector3f> vertices = meshData.getVertices();
        List<Vector3f> normals = meshData.getNormals();
        List<Integer> triangles = meshData.getTriangles();
        List<Vector2f> uv1 = meshData.getUV1();
        List<Vector2f> uv2 = meshData.getUV2();
        List<Color32> colors = meshData.getColors();
        
        // PLY can't have more than one mesh per file, so write one file per
        // sub-mesh
        final int subMeshes = mesh.subMeshes.size();
        for (int i = 0; i < subMeshes; i++) {
            SubMesh subMesh = mesh.subMeshes.get(i);

            // use prefix if there's more than one submesh
            String name = mesh.name;
            if (subMeshes > 1) {
                name = String.format("%s_%d", name, i);
            }
            
            try (PrintStream ps_ = handler.getPrintStream(mesh.name, "obj")) {
                ps = ps_;
                
                final int numVertices = subMesh.vertexCount.intValue();
                final int ofsVertices = subMesh.firstVertex.intValue();
                
                // 3 indices per face
                final int numFaces = subMesh.indexCount.intValue() / 3;
                
                // 3 indices per face, 2 bytes per index
                final int ofsFaces = subMesh.firstByte.intValue() / 6;

                // write header
                writeHeaderStart();
                writeComment("Created by " + DisUnity.getSignature());
                writeVertexHeader(numVertices, !normals.isEmpty(), !uv1.isEmpty(),
                        !uv2.isEmpty(), !colors.isEmpty());
                writeFaceHeader(numFaces);
                writeHeaderEnd();

                // write vertices
                for (int j = ofsVertices; j < ofsVertices + numVertices; j++) {
                    Vector3f v = vertices.get(j);
                    Vector3f vn = normals.isEmpty() ? null : normals.get(j);
                    Vector2f vt1 = uv1.isEmpty() ? null : uv1.get(j);
                    Vector2f vt2 = uv2.isEmpty() ? null : uv2.get(j);
                    Color32 c = colors.isEmpty() ? null : colors.get(j);
                    writeVertex(v, vn, vt1, vt2, c);
                }

                // write faces
                for (int j = ofsFaces; j < ofsFaces + numFaces; j++) {
                    int i1 = triangles.get(j * 3);
                    int i2 = triangles.get(j * 3 + 1);
                    int i3 = triangles.get(j * 3 + 2);
                    writeFace(i1, i2, i3);
                }
            }
        }
    }

    private void writeLine() {
        ps.println();
    }

    private void writeHeaderStart() {
        ps.println("ply");
        ps.println("format ascii 1.0");
    }

    private void writeComment(String comment) {
        ps.print("comment ");
        ps.println(comment);
    }

    private void writeVertexHeader(int elements, boolean normals, boolean uv1, boolean uv2, boolean colors) {
        ps.print("element vertex ");
        ps.println(elements);
        ps.println("property float x");
        ps.println("property float y");
        ps.println("property float z");
        if (normals) {
            ps.println("property float nx");
            ps.println("property float ny");
            ps.println("property float nz");
        }
        if (uv1) {
            ps.println("property float s");
            ps.println("property float t");
        }
        if (uv2) {
            ps.println("property float s2");
            ps.println("property float t2");
        }
        if (colors) {
            ps.println("property uchar red");
            ps.println("property uchar green");
            ps.println("property uchar blue");
        }
    }

    private void writeFaceHeader(int elements) {
        ps.print("element face ");
        ps.println(elements);
        ps.println("property list uchar int vertex_indices");
    }

    private void writeHeaderEnd() {
        ps.println("end_header");
    }

    private void writeVector(Vector2f v) {
        ps.print(v.x);
        ps.print(" ");
        ps.print(v.y);
    }

    private void writeVector(Vector3f v) {
        ps.print(v.x);
        ps.print(" ");
        ps.print(v.y);
        ps.print(" ");
        ps.print(v.z);
    }

    private void writeColor(Color32 c) {
        ps.print(c.r);
        ps.print(" ");
        ps.print(c.g);
        ps.print(" ");
        ps.print(c.b);
    }

    private void writeVertex(Vector3f v, Vector3f vn, Vector2f vt1, Vector2f vt2, Color32 c) {
        writeVector(v);
        if (vn != null) {
            ps.print(" ");
            writeVector(vn);
        }
        if (vt1 != null) {
            ps.print(" ");
            writeVector(vt1);
        }
        if (vt2 != null) {
            ps.print(" ");
            writeVector(vt2);
        }
        if (c != null) {
            ps.print(" ");
            writeColor(c);
        }
        writeLine();
    }

    private void writeFace(int i1, int i2, int i3) {
        ps.print("3 ");
        ps.print(i1);
        ps.print(" ");
        ps.print(i2);
        ps.print(" ");
        ps.println(i3);
    }
}
