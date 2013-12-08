/*
 ** 2013 December 06
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.extract.handler;

import info.ata4.unity.DisUnity;
import info.ata4.unity.asset.struct.AssetObjectPath;
import info.ata4.unity.cli.extract.handler.struct.Color32;
import info.ata4.unity.cli.extract.handler.struct.Vector2f;
import info.ata4.unity.cli.extract.handler.struct.Vector3f;
import info.ata4.unity.cli.extract.handler.struct.Vector4f;
import info.ata4.unity.serdes.UnityBuffer;
import info.ata4.unity.serdes.UnityList;
import info.ata4.unity.serdes.UnityObject;
import info.ata4.util.io.DataInputReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class MeshHandler extends ExtractHandler {
    
    private static final Logger L = Logger.getLogger(MeshHandler.class.getName());
    
    private ByteBuffer indexBuffer;
    private ByteBuffer vertexBuffer;
    private UnityObject obj;
    private String name;

    private List<Vector3f> vertices = new ArrayList<>();
    private List<Vector3f> normals = new ArrayList<>();
    private List<Color32> colors = new ArrayList<>();
    private List<Vector2f> uv1 = new ArrayList<>();
    private List<Vector2f> uv2 = new ArrayList<>();
    private List<Vector4f> tangents = new ArrayList<>();
    
    @Override
    public String getClassName() {
        return "Mesh";
    }
    
    @Override
    public String getFileExtension() {
        return "obj";
    }

    @Override
    public void extract(AssetObjectPath path, UnityObject obj) throws IOException {
        this.obj = obj;
        
        readIndexData();
        readVertexData();
        
        name = obj.getValue("m_Name");

        try (PrintStream ps = new PrintStream(getAssetFile(path.pathID, name))) {
            writeMesh(ps);
        }
        
        clear();
    }
    
    private void readIndexData() {
        // get index buffer
        UnityBuffer indexBufferObj = obj.getValue("m_IndexBuffer");
        indexBuffer = indexBufferObj.getBuffer();
        indexBuffer.order(ByteOrder.LITTLE_ENDIAN);

        L.log(Level.FINE, "Index buffer size: {0}", indexBuffer.capacity());
    }
    
    private void readVertexData() throws IOException {
        int meshCompression = obj.getValue("m_MeshCompression");
        if (meshCompression != 0) {
            // TODO
            throw new UnsupportedOperationException("Compressed meshes aren't supported yet");
        }
        
        // get vertex buffer
        UnityObject vertexDataObj = obj.getValue("m_VertexData");
        UnityBuffer dataSizeObj = vertexDataObj.getValue("m_DataSize");
        vertexBuffer = dataSizeObj.getBuffer();
        vertexBuffer.order(ByteOrder.LITTLE_ENDIAN);

        L.log(Level.FINE, "Vertex buffer size: {0}", vertexBuffer.capacity());
        
        // extract vertex data
        UnityList streams = vertexDataObj.getValue("m_Streams");
        long vertexCount = vertexDataObj.getValue("m_VertexCount");
        
        DataInputReader in = new DataInputReader(vertexBuffer);
        
        for (Object stream : streams.getList()) {
            UnityObject streamObj = (UnityObject) stream;
            
            long channelMask = streamObj.getValue("channelMask");
            long offset = streamObj.getValue("offset");
            
            vertexBuffer.position((int) offset);
            
            for (int i = 0; i < vertexCount; i++) {
                if ((channelMask & 1) != 0) {
                    Vector3f v = new Vector3f();
                    v.read(in);
                    vertices.add(v);
                }
                
                if ((channelMask & 2) != 0) {
                    Vector3f n = new Vector3f();
                    n.read(in);
                    normals.add(n);
                }
                
                if ((channelMask & 4) != 0) {
                    Color32 c = new Color32();
                    c.read(in);
                    colors.add(c);
                }
                
                if ((channelMask & 8) != 0) {
                    Vector2f uv = new Vector2f();
                    uv.read(in);
                    uv1.add(uv);
                }
                
                if ((channelMask & 16) != 0) {
                    Vector2f uv = new Vector2f();
                    uv.read(in);
                    uv2.add(uv);
                }
                
                if ((channelMask & 32) != 0) {
                    Vector4f t = new Vector4f();
                    t.read(in);
                    tangents.add(t);
                }
            }
        }
    }
    
    private void clear() {
        vertices.clear();
        normals.clear();
        colors.clear();
        uv1.clear();
        uv2.clear();
        tangents.clear();
        
        vertexBuffer = null;
        indexBuffer = null;
    }

    private void writeMesh(PrintStream ps) {
        ps.println("# Created by DisUnity v" + DisUnity.getVersion());
        
        if (!vertices.isEmpty()) {
            for (Vector3f v : vertices) {
                ps.println("v " + v.x + " " + v.y + " " + v.z);
            }
        }
        
        if (!normals.isEmpty()) {
            for (Vector3f vn : normals) {
                ps.println("vn " + vn.x + " " + vn.y + " " + vn.z);
            }
        }
        
        List<Vector2f> uv;
        
        if (!uv1.isEmpty()) {
            uv = uv1;
        } else if (!uv2.isEmpty()) {
            uv = uv2;
        } else {
            uv = null;
        }
        
        if (uv != null) {
            for (Vector2f vt : uv) {
                ps.println("vt " + vt.x + " " + vt.y);
            }
        }
        
        ps.println();
        ps.println("o " + name);
        ps.println("s 1");
        
        long totalVerts = 0;
        long totalIndices = 0;
        
        UnityList subMeshesObj = obj.getValue("m_SubMeshes");
        int subMeshIndex = 0;

        for (Object subMesh : subMeshesObj.getList()) {
            UnityObject subMeshObj = (UnityObject) subMesh;

            long vertexCount = subMeshObj.getValue("vertexCount");
            totalVerts += vertexCount;

            long indexCount = subMeshObj.getValue("indexCount");
            totalIndices += indexCount;

            writeSubMesh(ps, subMeshObj, subMeshIndex);
            
            subMeshIndex++;
        }

        L.log(Level.FINE, "Submeshes: {0}", subMeshesObj.getList().size());
        L.log(Level.FINE, "Total indices: {0}", totalIndices);
        L.log(Level.FINE, "Total vertices: {0}", totalVerts);
    }
    
    private void writeSubMesh(PrintStream ps, UnityObject subMeshObj, int subMeshIndex) {
        long firstByte = subMeshObj.getValue("firstByte");
        long indexCount = subMeshObj.getValue("indexCount");
        
        ps.printf("usemtl %s_submesh_%02d\n", name, subMeshIndex);

        try {
            DataInputReader in = new DataInputReader(indexBuffer);
            indexBuffer.position((int) firstByte);

            for (int i = 0; i < indexCount / 3; i++) {
                int i1 = in.readUnsignedShort() + 1;
                int i2 = in.readUnsignedShort() + 1;
                int i3 = in.readUnsignedShort() + 1;
                ps.printf("f %d/%d/%d", i1, i1, i1);
                ps.printf(" %d/%d/%d", i2, i2, i2);
                ps.printf(" %d/%d/%d\n", i3, i3, i3);
            }
            
            ps.println();
        } catch (Exception ex) {
            L.log(Level.SEVERE, "Index buffer reading error", ex);
        }
    }
}
