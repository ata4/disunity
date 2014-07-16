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
import info.ata4.unity.engine.struct.Vector2f;
import info.ata4.unity.engine.struct.Vector3f;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
class ObjWriter extends MeshWriter {
    
    private PrintStream ps;
    private List<Vector2f> vts;
    private List<Vector3f> vns;

    ObjWriter(MeshHandler handler) {
        super(handler);
    }
    
    @Override
    public void write(MeshData meshData) throws IOException {
        Mesh mesh = meshData.getMesh();
        
        vns = meshData.getNormals();
        vts = meshData.getUV1();
        
        // use second layer if the first one is unused
        if (vts.isEmpty()) {
            vts = meshData.getUV2();
        }
        
        try (PrintStream ps_ = handler.getPrintStream(mesh.name, "obj")) {
            ps = ps_;
        
            writeComment("Created by " + DisUnity.getSignature());

            // write vertex array
            for (Vector3f v : meshData.getVertices()) {
                writeVertex(v);
            }

            // write normal array
            for (Vector3f vn : vns) {
                writeNormal(vn);
            }

            // write texture coordinate array
            for (Vector2f vt : vts) {
                writeUV(vt);
            }

            writeLine();
            writeObject(mesh.name);
            writeSmooth(1);

            final int subMeshCount = mesh.subMeshes.size();
            final int vertsPerFace = 3;
            for (int i = 0; i < subMeshCount; i++) {
                // write sub-meshes as materials
                if (subMeshCount == 1) {
                    writeUsemtl(mesh.name);
                } else {
                    writeUsemtl(String.format("%s_%d", mesh.name, i));
                }
                
                // write sub-mesh triangles
                List<Integer> subMeshTriangles = meshData.getTriangles().get(i);
                List<Integer> faceTriangles = new ArrayList<>();
                
                for (Integer t : subMeshTriangles) {
                    faceTriangles.add(t);
                    
                    if (faceTriangles.size() == vertsPerFace) {
                        writeFace(faceTriangles);
                        faceTriangles.clear();
                    }
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

    private void writeFace(List<Integer> indices) {
        ps.print("f ");
        
        // reverse winding to fix normals after x axis has been flipped
        Collections.reverse(indices);
        
        boolean vt = !vts.isEmpty();
        boolean vn = !vns.isEmpty();
        
        for (int index : indices) {
            // OBJ indices start from 1
            int i = index + 1;
            
            ps.print(i);
            
            if (vt || vn) {
                ps.print('/');

                if (vt) {
                    ps.print(i);
                }

                ps.print('/');

                if (vn) {
                    ps.print(i);
                }
            }
            
            ps.print(' ');
        }
        
        ps.print('\n');
    }

    private void writeVector(String prefix, Vector2f v) {
        ps.print(prefix);
        ps.print(' ');
        ps.print(v.x);
        ps.print(' ');
        ps.print(1 - v.y);
        ps.println();
    }

    private void writeVector(String prefix, Vector3f v) {
        ps.print(prefix);
        ps.print(' ');
        ps.print(-v.x);
        ps.print(' ');
        ps.print(v.y);
        ps.print(' ');
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
