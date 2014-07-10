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

import info.ata4.unity.engine.struct.Vector2f;
import info.ata4.unity.engine.struct.Vector3f;
import java.io.PrintStream;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
class ObjWriter {
    
    final PrintStream ps;

    ObjWriter(PrintStream ps) {
        this.ps = ps;
    }

    void writeLine() {
        ps.println();
    }

    void writeComment(String comment) {
        ps.print("# ");
        ps.println(comment);
    }

    void writeObject(String name) {
        ps.print("o ");
        ps.println(name);
    }

    void writeSmooth(int smooth) {
        ps.print("s ");
        ps.println(smooth);
    }

    void writeUsemtl(String material) {
        ps.print("usemtl ");
        ps.println(material);
    }

    void writeFace(int i1, int i2, int i3, boolean vt, boolean vn) {
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

    void writeVector(String prefix, Vector2f v) {
        ps.print(prefix);
        ps.print(" ");
        ps.print(v.x);
        ps.print(" ");
        ps.print(v.y);
        ps.println();
    }

    void writeVector(String prefix, Vector3f v) {
        ps.print(prefix);
        ps.print(" ");
        ps.print(v.x);
        ps.print(" ");
        ps.print(v.y);
        ps.print(" ");
        ps.print(v.z);
        ps.println();
    }

    void writeVertex(Vector3f v) {
        writeVector("v", v);
    }

    void writeNormal(Vector3f vn) {
        writeVector("vn", vn);
    }

    void writeUV(Vector2f vt) {
        writeVector("vt", vt);
    }
    
}
