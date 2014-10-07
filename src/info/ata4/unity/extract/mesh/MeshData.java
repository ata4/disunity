/*
 ** 2014 Juli 10
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.extract.mesh;

import info.ata4.io.DataInputReader;
import info.ata4.io.buffer.ByteBufferInputStream;
import info.ata4.io.streams.BitInputStream;
import info.ata4.log.LogUtils;
import info.ata4.unity.engine.ChannelInfo;
import info.ata4.unity.engine.CompressedMesh;
import info.ata4.unity.engine.Mesh;
import info.ata4.unity.engine.PackedBitVector;
import info.ata4.unity.engine.StreamInfo;
import info.ata4.unity.engine.SubMesh;
import info.ata4.unity.engine.VertexData;
import info.ata4.unity.engine.struct.Color32;
import info.ata4.unity.engine.struct.Vector2f;
import info.ata4.unity.engine.struct.Vector3f;
import info.ata4.unity.engine.struct.Vector4f;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Container for decoded mesh data than is used by MeshWriter objects.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
class MeshData {
    
    private static final Logger L = LogUtils.getLogger();
    
    private static final int CHANNEL_COUNT = 6;
    private static final int CHANNEL_VERTS = 0;
    private static final int CHANNEL_NORMALS = 1;
    private static final int CHANNEL_COLORS = 2;
    private static final int CHANNEL_UV1 = 3;
    private static final int CHANNEL_UV2 = 4;
    private static final int CHANNEL_TANGENTS = 5;
    
    private final Mesh mesh;
    private final List<Vector3f> vertices = new ArrayList<>();
    private final List<Vector3f> normals = new ArrayList<>();
    private final List<Color32> colors = new ArrayList<>();
    private final List<Vector2f> uv1 = new ArrayList<>();
    private final List<Vector2f> uv2 = new ArrayList<>();
    private final List<Vector4f> tangents = new ArrayList<>();
    private final List<List<Integer>> indices = new ArrayList<>();
    private final List<List<Integer>> triangles = new ArrayList<>();
    
    MeshData(Mesh mesh) throws IOException {
        this.mesh = mesh;
        
        if (mesh.getMeshCompression() == 0) {
            readVertexBuffer();
            readIndexBuffer();
        } else {
            readCompressedMesh();
        }
    }
    
    Mesh getMesh() {
        return mesh;
    }

    List<Vector3f> getVertices() {
        return vertices;
    }

    List<Vector3f> getNormals() {
        return normals;
    }

    List<Color32> getColors() {
        return colors;
    }

    List<Vector2f> getUV1() {
        return uv1;
    }

    List<Vector2f> getUV2() {
        return uv2;
    }

    List<Vector4f> getTangents() {
        return tangents;
    }
    
    List<List<Integer>> getIndices() {
        return indices;
    }

    List<List<Integer>> getTriangles() {
        return triangles;
    }
    
    private int[] readPackedBits(PackedBitVector pbv) throws IOException {
        int numItems = pbv.getNumItems().intValue();
        int bitSize = pbv.getBitSize();
        
        // don't waste time on empty vectors
        if (numItems == 0 || bitSize == 0) {
            return new int[]{};
        }
        
        // the values are packed with a variable bit length
        BitInputStream bis = new BitInputStream(new ByteBufferInputStream(pbv.getData()));
        bis.setBitLength(bitSize);
        
        int[] items = new int[numItems];
        for (int i = 0; i < items.length; i++) {
            items[i] = bis.read();
        }
        
        return items;
    }
    
    private float[] readPackedFloats(PackedBitVector pbv) throws IOException {
        int numItems = pbv.getNumItems().intValue();
        int bitSize = pbv.getBitSize();
        
        // don't waste time on empty vectors
        if (numItems == 0 || bitSize == 0) {
            return new float[]{};
        }
        
        // expand integers to floats using the range and offset floats in the
        // packed bit vector
        int[] items = readPackedBits(pbv);
        float[] floats = new float[items.length];
        
        int maxValue = (1 << bitSize) - 1;
        float range = pbv.getRange() / maxValue;
        float start = pbv.getStart();

        for (int i = 0; i < floats.length; i++) {
            floats[i] = items[i] * range + start;
        }
        
        return floats;
    }
    
    private float[] readPackedNormals(PackedBitVector pbv, PackedBitVector pbvSigns) throws IOException {
        int[] items = readPackedBits(pbv); // 2 elements per vertex
        int[] signs = readPackedBits(pbvSigns); // one element per vertex
        
        // don't waste time on empty vectors
        if (items.length == 0 || signs.length == 0) {
            return new float[]{};
        }
        
        // convert signage 0 to -1
        for (int i = 0; i < signs.length; i++) {
            if (signs[i] == 0) {
                signs[i] = -1;
            }
        }
        
        float[] floats = new float[signs.length * 3]; // 3 elements per vertex
        
        int maxValue = (1 << pbv.getBitSize()) - 1;
        float range = pbv.getRange() / maxValue;
        float start = pbv.getStart();

        for (int i = 0; i < floats.length / 3; i++) {
            float x = items[i * 2] * range + start;
            float y = items[i * 2 + 1] * range + start;
            
            // reconstruct z using x^2 + y^2 + z^2 = 1, since a normal vector
            // can be represented as a dot on the surface of a sphere
            float z = (float) ((1 - Math.pow(x, 2) - Math.pow(y, 2)) * signs[i]);
            
            floats[i * 3] = x;
            floats[i * 3 + 1] = y;
            floats[i * 3 + 2] = z;
        }
        
        return floats;
    }

    private void readVertexBuffer() throws IOException {
        VertexData vertexData = mesh.getVertexData();
        
        // get vertex buffer
        ByteBuffer vertexBuffer = vertexData.getDataSize();
        vertexBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
        L.log(Level.FINE, "Vertex buffer size: {0}", vertexBuffer.capacity());

        DataInputReader in = DataInputReader.newReader(vertexBuffer);

        List<StreamInfo> streams = vertexData.getStreams();
        List<ChannelInfo> channels = vertexData.getChannels();
        
        for (StreamInfo stream : streams) {
            // skip empty channels
            int channelMask = stream.getChannelMask().intValue();
            if (channelMask == 0) {
                continue;
            }

            vertexBuffer.position(stream.getOffset().intValue());

            // read vertex data from each vertex and channel
            for (int i = 0; i < vertexData.getVertexCount(); i++) {
                for (int j = 0; j < CHANNEL_COUNT; j++) {
                    // skip unselected channels
                    if ((channelMask & 1 << j) == 0) {
                        continue;
                    }
                    
                    boolean half = false;
                    ChannelInfo channel = null;

                    // channels may not be available in older versions
                    if (!channels.isEmpty()) {
                        channel = channels.get(j);
                        half = channel.getFormat() == 1;
                    }

                    switch (j) {
                        case CHANNEL_VERTS:
                            Vector3f v = new Vector3f();
                            v.setHalf(half);
                            v.read(in);
                            vertices.add(v);
                            break;

                        case CHANNEL_NORMALS:
                            Vector3f vn = new Vector3f();
                            vn.setHalf(half);
                            vn.read(in);
                            normals.add(vn);
                            if (half && channel != null && channel.getDimension() == 4) {
                                in.skipBytes(2); // padding?
                            }
                            break;

                        case CHANNEL_COLORS:
                            Color32 c = new Color32();
                            c.read(in);
                            colors.add(c);
                            break;

                        case CHANNEL_UV1:
                        case CHANNEL_UV2:
                            Vector2f vt = new Vector2f();
                            vt.setHalf(half);
                            vt.read(in);
                            if (j == CHANNEL_UV1) {
                                uv1.add(vt);
                            } else {
                                uv2.add(vt);
                            }
                            break;

                        case CHANNEL_TANGENTS:
                            Vector4f t = new Vector4f();
                            t.setHalf(half);
                            t.read(in);
                            tangents.add(t);
                            break;
                    }
                }
                
                in.align(stream.getStride().intValue());
            }
        }
    }

    private void readIndexBuffer() throws IOException {
        ByteBuffer indexBuffer = mesh.getIndexBuffer();
        indexBuffer.order(ByteOrder.LITTLE_ENDIAN);
        DataInputReader in = DataInputReader.newReader(indexBuffer);

        for (SubMesh subMesh : mesh.getSubMeshes()) {
            List<Integer> subMeshIndices = new ArrayList<>();
            List<Integer> subMeshTriangles = new ArrayList<>();
            
            in.position(subMesh.getFirstByte().longValue());
            for (long j = 0; j < subMesh.getIndexCount().longValue(); j++) {
                subMeshIndices.add(in.readUnsignedShort());
            }
            
            // read triangle strips if topology/isTriStrip is not zero
            if (subMesh.getTopology().longValue() == 0) {
                // use indices as is
                subMeshTriangles.addAll(subMeshIndices);
            } else {
                LinkedList<Integer> tris = new LinkedList<>();
                for (int k = 0; k < subMeshIndices.size(); k++) {
                    tris.addLast(subMeshIndices.get(k));

                    // fill deque with three indices
                    if (tris.size() < 3) {
                        continue;
                    }

                    int t1 = tris.get(0);
                    int t2 = tris.get(1);
                    int t3 = tris.get(2);
                    
                    // shift deque
                    tris.removeFirst();

                    // skip degenerated triangles
                    if (t1 == t2 || t1 == t3 || t3 == t2) {
                        continue;
                    }

                    // switch winding direction every other triangle
                    if (k % 2 == 0) {
                        subMeshTriangles.add(t1);
                        subMeshTriangles.add(t2);
                        subMeshTriangles.add(t3);
                    } else {
                        subMeshTriangles.add(t3);
                        subMeshTriangles.add(t2);
                        subMeshTriangles.add(t1);
                    }
                }
            }
            
            indices.add(subMeshIndices);
            triangles.add(subMeshTriangles);
        }
    }

    private void readCompressedMesh() throws IOException {
        CompressedMesh cmesh = mesh.getCompressedMesh();

        float[] vertexFloats = readPackedFloats(cmesh.getVertices());
        for (int i = 0; i < vertexFloats.length / 3; i++) {
            Vector3f v = new Vector3f();
            v.x = vertexFloats[i * 3];
            v.y = vertexFloats[i * 3 + 1];
            v.z = vertexFloats[i * 3 + 2];
            vertices.add(v);
        }

        float[] normalFloats = readPackedNormals(cmesh.getNormals(), cmesh.getNormalSigns());
        for (int i = 0; i < normalFloats.length / 3; i++) {
            Vector3f vn = new Vector3f();
            vn.x = normalFloats[i * 3];
            vn.y = normalFloats[i * 3 + 1];
            vn.z = normalFloats[i * 3 + 2];
            normals.add(vn);
        }

        float[] uvFloats = readPackedFloats(cmesh.getUV());
        for (int i = 0; i < uvFloats.length / 2; i++) {
            Vector2f vt = new Vector2f();
            vt.x = uvFloats[i * 2];
            vt.y = uvFloats[i * 2 + 1];
            if (i < vertexFloats.length / 3) {
                uv1.add(vt);
            } else {
                uv2.add(vt);
            }
        }

        int[] colorInts = readPackedBits(cmesh.getColors());
        for (int i = 0; i < colorInts.length; i++) {
            Color32 c = new Color32();
            c.fromInt(colorInts[i]);
            colors.add(c);
        }
        
        // TODO: works for triangulated meshes only!
        int[] triangleInts = readPackedBits(cmesh.getTriangles());
        for (SubMesh subMesh : mesh.getSubMeshes()) {
            List<Integer> subMeshIndices = new ArrayList<>();
            List<Integer> subMeshTriangles = new ArrayList<>();
            
            final int vertOfs = subMesh.getFirstVertex().intValue();
            final int vertCount = subMesh.getVertexCount().intValue();
            for (int j = vertOfs; j < vertCount; j++) {
                subMeshTriangles.add(triangleInts[j]);
                subMeshIndices.add(triangleInts[j]);
            }
            
            indices.add(subMeshIndices);
            triangles.add(subMeshTriangles);
        }
    }
}
