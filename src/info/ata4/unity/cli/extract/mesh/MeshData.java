/*
 ** 2014 Juli 10
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.extract.mesh;

import info.ata4.unity.engine.Mesh;
import info.ata4.unity.engine.struct.Color32;
import info.ata4.unity.engine.struct.Vector2f;
import info.ata4.unity.engine.struct.Vector3f;
import info.ata4.unity.engine.struct.Vector4f;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
class MeshData {
    
    private final List<Vector3f> vertices = new ArrayList<>();
    private final List<Vector3f> normals = new ArrayList<>();
    private final List<Color32> colors = new ArrayList<>();
    private final List<Vector2f> uv1 = new ArrayList<>();
    private final List<Vector2f> uv2 = new ArrayList<>();
    private final List<Vector4f> tangents = new ArrayList<>();
    private final List<Integer> triangles = new ArrayList<>();
    private final Mesh mesh;

    MeshData(Mesh mesh) {
        this.mesh = mesh;
    }

    protected List<Vector3f> getVertices() {
        return vertices;
    }

    protected List<Vector3f> getNormals() {
        return normals;
    }

    protected List<Color32> getColors() {
        return colors;
    }

    protected List<Vector2f> getUV1() {
        return uv1;
    }

    protected List<Vector2f> getUV2() {
        return uv2;
    }

    protected List<Vector4f> getTangents() {
        return tangents;
    }

    protected List<Integer> getTriangles() {
        return triangles;
    }

    protected Mesh getMesh() {
        return mesh;
    }
}
