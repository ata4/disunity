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

import info.ata4.unity.struct.Color32;
import info.ata4.unity.struct.Vector2f;
import info.ata4.unity.struct.Vector3f;
import java.io.PrintStream;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
class PlyWriter {
    
    final PrintStream ps;

    PlyWriter(PrintStream ps) {
        this.ps = ps;
    }

    void writeLine() {
        ps.println();
    }

    void writeHeaderStart() {
        ps.println("ply");
        ps.println("format ascii 1.0");
    }

    void writeComment(String comment) {
        ps.print("comment ");
        ps.println(comment);
    }

    void writeVertexHeader(int elements, boolean normals, boolean uv1, boolean uv2, boolean colors) {
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

    void writeFaceHeader(int elements) {
        ps.print("element face ");
        ps.println(elements);
        ps.println("property list uchar int vertex_indices");
    }

    void writeHeaderEnd() {
        ps.println("end_header");
    }

    void writeVector(Vector2f v) {
        ps.print(v.x);
        ps.print(" ");
        ps.print(v.y);
    }

    void writeVector(Vector3f v) {
        ps.print(v.x);
        ps.print(" ");
        ps.print(v.y);
        ps.print(" ");
        ps.print(v.z);
    }

    void writeColor(Color32 c) {
        ps.print(c.r);
        ps.print(" ");
        ps.print(c.g);
        ps.print(" ");
        ps.print(c.b);
    }

    void writeVertex(Vector3f v, Vector3f vn, Vector2f vt1, Vector2f vt2, Color32 c) {
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

    void writeFace(int i1, int i2, int i3) {
        ps.print("3 ");
        ps.print(i1);
        ps.print(" ");
        ps.print(i2);
        ps.print(" ");
        ps.println(i3);
    }
    
}
