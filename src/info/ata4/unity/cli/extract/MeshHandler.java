/*
 ** 2013 December 06
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.extract;

import info.ata4.io.DataInputReader;
import info.ata4.io.buffer.ByteBufferInputStream;
import info.ata4.log.LogUtils;
import info.ata4.unity.DisUnity;
import info.ata4.unity.serdes.UnityObject;
import info.ata4.unity.struct.Color32;
import info.ata4.unity.struct.Vector2f;
import info.ata4.unity.struct.Vector3f;
import info.ata4.unity.struct.Vector4f;
import info.ata4.unity.util.UnityVersion;
import info.ata4.util.io.BitInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class MeshHandler extends AssetExtractHandler {
    
    private static final Logger L = LogUtils.getLogger();
    
    private Mesh mesh;
    private MeshFormat format = MeshFormat.OBJ;
    
    private List<Vector3f> vertices;
    private List<Vector3f> normals;
    private List<Color32> colors;
    private List<Vector2f> uv1;
    private List<Vector2f> uv2;
    private List<Vector4f> tangents;
    private List<Integer> triangles;
    
    @Override
    public void extract(UnityObject obj) throws IOException {
        // TODO: support older mesh formats
        UnityVersion version = getAssetFile().getTypeTree().getEngineVersion();
        if (version == null || version.getMajor() != 4) {
            throw new UnsupportedOperationException("Unity 4 format is supported only");
        }

        mesh = new Mesh(obj);
        
        readVertexData();
        
        switch (format) {
            case PLY:
                writePlyMesh();
                break;
                
            case OBJ:
                writeObjMesh();
                break;
        }
    }
    
    public MeshFormat getFormat() {
        return format;
    }

    public void setFormat(MeshFormat format) {
        this.format = format;
    }
    
    private void readVertexData() throws IOException {
        // init lists
        vertices = new ArrayList<>();
        normals = new ArrayList<>();
        colors = new ArrayList<>();
        uv1 = new ArrayList<>();
        uv2 = new ArrayList<>();
        tangents = new ArrayList<>();
        triangles = new ArrayList<>();
        
        if (mesh.meshCompression == 0) {
            // get vertex buffer
            ByteBuffer vertexBuffer = mesh.vertexData.dataSize;
            vertexBuffer.order(ByteOrder.LITTLE_ENDIAN);

            L.log(Level.FINE, "Vertex buffer size: {0}", vertexBuffer.capacity());

            DataInputReader in = DataInputReader.newReader(vertexBuffer);

            List<StreamInfo> streams = mesh.vertexData.streams;
            List<ChannelInfo> channels = mesh.vertexData.channels;

            try {
                for (StreamInfo stream : streams) {
                    // skip empty channels
                    if (stream.channelMask == 0) {
                        continue;
                    }

                    vertexBuffer.position(stream.offset.intValue());

                    // read vertex data from each vertex and channel
                    for (int i = 0; i < mesh.vertexData.vertexCount; i++) {
                        for (int j = 0; j < channels.size(); j++) {
                            // skip unselected channels
                            if ((stream.channelMask & 1 << j) == 0) {
                                continue;
                            }

                            ChannelInfo channel = channels.get(j);
                            boolean half = channel.format == 1;

                            // Known channels:
                            // 0 - Coordinates (Vector3f)
                            // 1 - Normals (Vector3f)
                            // 2 - Colors (Color32)
                            // 3 - UV layer 1 (Vector2f)
                            // 4 - UV layer 2 (Vector2f)
                            // 5 - Tangents (Vector4f)
                            switch (j) {
                                case 0:
                                    Vector3f v = new Vector3f();
                                    v.setHalf(half);
                                    v.read(in);
                                    vertices.add(v);
                                    break;

                                case 1:
                                    Vector3f vn = new Vector3f();
                                    vn.setHalf(half);
                                    vn.read(in);
                                    normals.add(vn);
                                    if (half && channel.dimension == 4) {
                                        in.skipBytes(2); // padding?
                                    }
                                    break;

                                case 2:
                                    Color32 c = new Color32();
                                    c.read(in);
                                    colors.add(c);
                                    break;

                                case 3:
                                case 4:
                                    Vector2f vt = new Vector2f();
                                    vt.setHalf(half);
                                    vt.read(in);
                                    if (j == 3) {
                                        uv1.add(vt);
                                    } else {
                                        uv2.add(vt);
                                    }
                                    break;

                                case 5:
                                    Vector4f t = new Vector4f();
                                    t.setHalf(half);
                                    t.read(in);
                                    tangents.add(t);
                                    break;
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                L.log(Level.SEVERE, "Vertex buffer reading error", ex);
            }

            try {
                mesh.indexBuffer.order(ByteOrder.LITTLE_ENDIAN);
                in = DataInputReader.newReader(mesh.indexBuffer);
                for (SubMesh subMesh : mesh.subMeshes) {
                    in.position(subMesh.firstByte);

                    for (int j = 0; j < subMesh.indexCount; j++) {
                        triangles.add(in.readUnsignedShort());
                    }
                }
            } catch (IOException ex) {
                L.log(Level.SEVERE, "Index buffer reading error", ex);
            }
        } else {
            CompressedMesh cmesh = mesh.compressedMesh;

            float[] vertexFloats = readPackedFloats(cmesh.vertices);
            for (int i = 0; i < vertexFloats.length / 3; i++) {
                Vector3f v = new Vector3f();
                v.x = vertexFloats[i * 3];
                v.y = vertexFloats[i * 3 + 1];
                v.z = vertexFloats[i * 3 + 2];
                vertices.add(v);
            }
            
            float[] normalFloats = readPackedNormals(cmesh.normals, cmesh.normalSigns);
            for (int i = 0; i < normalFloats.length / 3; i++) {
                Vector3f vn = new Vector3f();
                vn.x = normalFloats[i * 3];
                vn.y = normalFloats[i * 3 + 1];
                vn.z = normalFloats[i * 3 + 2];
                normals.add(vn);
            }
            
            float[] uvFloats = readPackedFloats(cmesh.UV);
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
            
            int[] colorInts = readPackedBits(cmesh.colors);
            for (int i = 0; i < colorInts.length; i++) {
                Color32 c = new Color32();
                c.fromInt(colorInts[i]);
                colors.add(c);
            }
            
            int[] triangleInts = readPackedBits(cmesh.triangles);
            for (int i = 0; i < triangleInts.length; i++) {
                triangles.add(triangleInts[i]);
            }
        }
    }
    
    private int[] readPackedBits(PackedBitVector pbv) throws IOException {
        // don't waste time on empty vectors
        if (pbv.numItems == 0 || pbv.bitSize == 0) {
            return new int[]{};
        }
        
        // the values are packed with a variable bit length
        BitInputStream bis = new BitInputStream(new ByteBufferInputStream(pbv.data));
        bis.setBitLength(pbv.bitSize);
        
        int numItems = pbv.numItems.intValue();
        int[] items = new int[numItems];
        for (int i = 0; i < items.length; i++) {
            items[i] = bis.read();
        }
        
        return items;
    }
    
    private float[] readPackedFloats(PackedBitVector pbv) throws IOException {
        // don't waste time on empty vectors
        if (pbv.numItems == 0 || pbv.bitSize == 0) {
            return new float[]{};
        }
        
        // expand integers to floats using the range and offset floats in the
        // packed bit vector
        int[] items = readPackedBits(pbv);
        float[] floats = new float[items.length];
        
        int maxValue = (1 << pbv.bitSize) - 1;
        float range = pbv.range / maxValue;
        float start = pbv.start;

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
        
        int maxValue = (1 << pbv.bitSize) - 1;
        float range = pbv.range / maxValue;
        float start = pbv.start;

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
    
    private void writeObjMesh() throws IOException {
        setOutputFileName(mesh.name);
        setOutputFileExtension("obj");
        
        Path objFile = getOutputFile();
        
        try (PrintStream ps = getPrintStream(objFile)) {
            ObjWriter obj = new ObjWriter(ps);
            obj.writeComment("Created by " + DisUnity.getSignature());

            // write vertex array
            for (Vector3f v : vertices) {
                obj.writeVertex(v);
            }

            // write normal array
            for (Vector3f vn : normals) {
                obj.writeNormal(vn);
            }

            // OBJ doesn't support more than one UV layer, so select the first
            // non-empty list
            List<Vector2f> uv = new ArrayList<>();

            if (!uv1.isEmpty()) {
                uv = uv1;
            } else if (!uv2.isEmpty()) {
                uv = uv2;
            }

            for (Vector2f vt : uv) {
                obj.writeUV(vt);
            }

            // write sub-meshes as materials
            obj.writeLine();
            obj.writeObject(mesh.name);
            obj.writeSmooth(1);

            final int subMeshes = mesh.subMeshes.size();
            for (int i = 0; i < subMeshes; i++) {
                SubMesh subMesh = mesh.subMeshes.get(i);
                
                if (subMeshes == 1) {
                    obj.writeUsemtl(mesh.name);
                } else {
                    obj.writeUsemtl(String.format("%s_%d", mesh.name, i));
                }
                
                // 3 indices per face
                final int numFaces = subMesh.indexCount.intValue() / 3;
                
                // 3 indices per face, 2 bytes per index
                final int ofsFaces = subMesh.firstByte.intValue() / 6;
                
                for (int j = ofsFaces; j < ofsFaces + numFaces; j++) {
                    int i1 = triangles.get(j * 3);
                    int i2 = triangles.get(j * 3 + 1);
                    int i3 = triangles.get(j * 3 + 2);
                    
                    obj.writeFace(i1, i2, i3, !uv.isEmpty(), !normals.isEmpty());
                }

                obj.writeLine();
            }
        }
    }

    private PrintStream getPrintStream(Path file) throws IOException {
        return new PrintStream(new BufferedOutputStream(Files.newOutputStream(file)));
    }

    private class ObjWriter {
        
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
    
    private void writePlyMesh() throws IOException {
        setOutputFileExtension("ply");
        
        // PLY can't have more than one mesh per file, so write one file per
        // sub-mesh
        final int subMeshes = mesh.subMeshes.size();
        for (int i = 0; i < subMeshes; i++) {
            SubMesh subMesh = mesh.subMeshes.get(i);

            // use prefix if there's more than one submesh
            if (subMeshes == 1) {
                setOutputFileName(mesh.name);
            } else {
                setOutputFileName(String.format("%s_%d", mesh.name, i));
            }
            
            Path plyFile = getOutputFile();
            try (PrintStream ps = getPrintStream(plyFile)) {
                final int numVertices = subMesh.vertexCount.intValue();
                final int ofsVertices = subMesh.firstVertex.intValue();
                
                // 3 indices per face
                final int numFaces = subMesh.indexCount.intValue() / 3;
                
                // 3 indices per face, 2 bytes per index
                final int ofsFaces = subMesh.firstByte.intValue() / 6;

                // write header
                PlyWriter ply = new PlyWriter(ps);
                ply.writeHeaderStart();
                ply.writeComment("Created by " + DisUnity.getSignature());
                ply.writeVertexHeader(numVertices, !normals.isEmpty(), !uv1.isEmpty(),
                        !uv2.isEmpty(), !colors.isEmpty());
                ply.writeFaceHeader(numFaces);
                ply.writeHeaderEnd();

                // write vertices
                for (int j = ofsVertices; j < ofsVertices + numVertices; j++) {
                    Vector3f v = vertices.get(j);
                    Vector3f vn = normals.isEmpty() ? null : normals.get(j);
                    Vector2f vt1 = uv1.isEmpty() ? null : uv1.get(j);
                    Vector2f vt2 = uv2.isEmpty() ? null : uv2.get(j);
                    Color32 c = colors.isEmpty() ? null : colors.get(j);
                    ply.writeVertex(v, vn, vt1, vt2, c);
                }

                // write faces
                for (int j = ofsFaces; j < ofsFaces + numFaces; j++) {
                    int i1 = triangles.get(j * 3);
                    int i2 = triangles.get(j * 3 + 1);
                    int i3 = triangles.get(j * 3 + 2);
                    ply.writeFace(i1, i2, i3);
                }
            }
        }
    }
    
    private class PlyWriter {
        
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

    // Mesh
    //   string m_Name
    //   vector m_SubMeshes
    //   BlendShapeData m_Shapes
    //   vector m_BindPose
    //   vector m_BoneNameHashes
    //   unsigned int m_RootBoneNameHash
    //   UInt8 m_MeshCompression
    //   UInt8 m_StreamCompression
    //   bool m_IsReadable
    //   bool m_KeepVertices
    //   bool m_KeepIndices
    //   vector m_IndexBuffer
    //   vector m_Skin
    //   VertexData m_VertexData
    //   CompressedMesh m_CompressedMesh
    //   AABB m_LocalAABB
    //   int m_MeshUsageFlags
    private class Mesh {

        final String name;
        final ByteBuffer indexBuffer;
        final Integer meshCompression;
        final VertexData vertexData;
        final List<SubMesh> subMeshes;
        final CompressedMesh compressedMesh;

        Mesh(UnityObject obj) {
            name = obj.getValue("m_Name");
            indexBuffer = obj.getValue("m_IndexBuffer");
            meshCompression = obj.getValue("m_MeshCompression");
            vertexData = new VertexData((UnityObject) obj.getValue("m_VertexData"));

            List<UnityObject> subMeshObjects = obj.getValue("m_SubMeshes");
            subMeshes = new ArrayList<>();
            for (UnityObject subMeshObject : subMeshObjects) {
                subMeshes.add(new SubMesh(subMeshObject));
            }
            
            compressedMesh = new CompressedMesh((UnityObject) obj.getValue("m_CompressedMesh"));
        }
    }

    // SubMesh
    //   unsigned int firstByte
    //   unsigned int indexCount
    //   int topology
    //   unsigned int firstVertex
    //   unsigned int vertexCount
    //   AABB localAABB
    private class SubMesh {

        final Long firstByte;
        final Long indexCount;
        final Integer topology;
        final Long firstVertex;
        final Long vertexCount;

        SubMesh(UnityObject obj) {
            firstByte = obj.getValue("firstByte");
            indexCount = obj.getValue("indexCount");
            topology = obj.getValue("topology");
            firstVertex = obj.getValue("firstVertex");
            vertexCount = obj.getValue("vertexCount");
        }
    }

    // VertexData
    //   unsigned int m_CurrentChannels
    //   unsigned int m_VertexCount
    //   vector m_Channels
    //   vector m_Streams
    //   TypelessData m_DataSize
    private class VertexData {

        final Long currentChannels;
        final Long vertexCount;
        final List<ChannelInfo> channels;
        final List<StreamInfo> streams;
        final ByteBuffer dataSize;

        VertexData(UnityObject obj) {
            currentChannels = obj.getValue("m_CurrentChannels");
            vertexCount = obj.getValue("m_VertexCount");
            
            List<UnityObject> channelObjects = obj.getValue("m_Channels");
            channels = new ArrayList<>();
            for (UnityObject channelObject : channelObjects) {
                channels.add(new ChannelInfo(channelObject));
            }
            
            List<UnityObject> streamObjects = obj.getValue("m_Streams");
            streams = new ArrayList<>();
            for (UnityObject streamObject : streamObjects) {
                streams.add(new StreamInfo(streamObject));
            }
            
            dataSize = obj.getValue("m_DataSize");
        }
    }

    // ChannelInfo
    //   UInt8 stream
    //   UInt8 offset
    //   UInt8 format
    //   UInt8 dimension
    private class ChannelInfo {

        // Index into m_Streams
        final Integer stream;
        
        // Vertex chunk offset
        final Integer offset;
        
        // 0 = full precision, 1 = half precision
        final Integer format;
        
        // Number of fields?
        final Integer dimension;

        ChannelInfo(UnityObject obj) {
            stream = obj.getValue("stream");
            offset = obj.getValue("offset");
            format = obj.getValue("format");
            dimension = obj.getValue("dimension");
        }
    }

    // StreamInfo data
    //   unsigned int channelMask
    //   unsigned int offset
    //   UInt8 stride
    //   UInt8 dividerOp
    //   UInt16 frequency
    private class StreamInfo {

        final Long channelMask;
        final Long offset;
        final Integer stride;
        final Integer dividerOp;
        final Integer frequency;

        StreamInfo(UnityObject obj) {
            channelMask = obj.getValue("channelMask");
            offset = obj.getValue("offset");
            stride = obj.getValue("stride");
            dividerOp = obj.getValue("dividerOp");
            frequency = obj.getValue("frequency");
        }
    }

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
    private class CompressedMesh {
        
       final PackedBitVector vertices;
       final PackedBitVector UV;
       final PackedBitVector bindPoses;
       final PackedBitVector normals;
       final PackedBitVector tangents;
       final PackedBitVector weights;
       final PackedBitVector normalSigns;
       final PackedBitVector tangentSigns;
       final PackedBitVector boneIndices;
       final PackedBitVector triangles;
       final PackedBitVector colors;
       
       CompressedMesh(UnityObject obj) {
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
    
    // PackedBitVector
    //   UInt32 m_NumItems
    //   float m_Range
    //   float m_Start
    //   vector m_Data
    //   UInt8 m_BitSize
    private class PackedBitVector {
        
        final Long numItems;
        final Float range;
        final Float start;
        final ByteBuffer data;
        final Integer bitSize;
        
        PackedBitVector(UnityObject obj) {
            numItems = obj.getValue("m_NumItems");
            range = obj.getValue("m_Range");
            start = obj.getValue("m_Start");
            data = obj.getValue("m_Data");
            bitSize = obj.getValue("m_BitSize");
        }
    }
    
    public static enum MeshFormat {
        OBJ, PLY;
    }
}
