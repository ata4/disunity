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
import info.ata4.unity.engine.struct.Vector2f;
import info.ata4.unity.engine.struct.Vector3f;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
class ObjWriter extends MeshWriter {
    
    private PrintStream ps;

    ObjWriter(MeshHandler handler) {
        super(handler);
    }
    
  @Override
    public void write(MeshData meshData) throws IOException {
        Mesh mesh = meshData.getMesh();

        List<Vector3f> vertices = meshData.getVertices();
        List<Vector3f> normals = meshData.getNormals();
        List<Integer> triangles = meshData.getTriangles();
        
        // OBJ doesn't support more than one UV layer, so select the first
        // non-empty list
        List<Vector2f> uv = new ArrayList<>();
        if (!meshData.getUV1().isEmpty()) {
            uv = meshData.getUV1();
        } else if (!meshData.getUV2().isEmpty()) {
            uv = meshData.getUV2();
        }
        
        try (PrintStream ps_ = handler.getPrintStream(mesh.name, "obj")) {
            ps = ps_;
        
            writeComment("Created by " + DisUnity.getSignature());

            // write vertex array
            for (Vector3f v : vertices) {
                writeVertex(v);
            }

            // write normal array
            for (Vector3f vn : normals) {
                writeNormal(vn);
            }

            for (Vector2f vt : uv) {
                writeUV(vt);
            }

            // write sub-meshes as materials
            writeLine();
            writeObject(mesh.name);
            writeSmooth(1);

            final int subMeshes = mesh.subMeshes.size();
            for (int i = 0; i < subMeshes; i++) {
                SubMesh subMesh = mesh.subMeshes.get(i);

                if (subMeshes == 1) {
                    writeUsemtl(mesh.name);
                } else {
                    writeUsemtl(String.format("%s_%d", mesh.name, i));
                }

                // 3 indices per face
                final int numFaces = subMesh.indexCount.intValue() / 3;

                // 3 indices per face, 2 bytes per index
                final int ofsFaces = subMesh.firstByte.intValue() / 6;

                for (int j = ofsFaces; j < ofsFaces + numFaces; j++) {
                    int i1 = triangles.get(j * 3);
                    int i2 = triangles.get(j * 3 + 1);
                    int i3 = triangles.get(j * 3 + 2);

                    writeFace(i1, i2, i3, !uv.isEmpty(), !normals.isEmpty());
                }

                writeLine();
            }
        }
    }

    private void writeLine() {
        ps.println();
    }

    private void writeComment(String comment) {
        ps.print("# ");
        ps.println(comment);
    }

    private void writeObject(String name) {
        ps.print("g ");
        ps.println(name);
    }

    private void writeSmooth(int smooth) {
        ps.print("s ");
        ps.println(smooth);
    }

    private void writeUsemtl(String material) {
        ps.print("usemtl ");
        ps.println(material);
    }

    private void writeFace(int i1, int i2, int i3, boolean vt, boolean vn) {
        // OBJ indices start from 1
        i1++;
        i2++;
        i3++;
        if (vt && !vn) {
            ps.printf("f %d/%d %d/%d %d/%d\n", i1, i1, i2, i2, i3, i3);
        } else if (!vt && vn) {
            ps.printf("f %d//%d %d//%d %d//%d\n", i1, i1, i2, i2, i3, i3);
        } else if (vt && vn) {
            ps.printf("f %d/%d/%d %d/%d/%d %d/%d/%d\n", i1, i1, i1, i2, i2, i2, i3, i3, i3);
        } else {
            ps.printf("f %d %d %d\n", i1, i2, i3);
        }
    }

    private void writeVector(String prefix, Vector2f v) {
        ps.print(prefix);
        ps.print(" ");
        ps.print(v.x);
        ps.print(" ");
        ps.print(v.y);
        ps.println();
    }

    private void writeVector(String prefix, Vector3f v) {
        ps.print(prefix);
        ps.print(" ");
        ps.print(v.x);
        ps.print(" ");
        ps.print(v.y);
        ps.print(" ");
        ps.print(v.z);
        ps.println();
    }

    private void writeVertex(Vector3f v) {
        writeVector("v", v);
    }

    private void writeNormal(Vector3f vn) {
        writeVector("vn", vn);
    }

    private void writeUV(Vector2f vt) {
        writeVector("vt", vt);
    }
}
