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

import info.ata4.io.DataInputReader;
import info.ata4.log.LogUtils;
import info.ata4.unity.DisUnity;
import info.ata4.unity.asset.struct.ObjectPath;
import info.ata4.unity.cli.extract.AssetExtractHandler;
import info.ata4.unity.serdes.UnityObject;
import info.ata4.unity.struct.Color32;
import info.ata4.unity.struct.Vector2f;
import info.ata4.unity.struct.Vector3f;
import info.ata4.unity.struct.Vector4f;
import info.ata4.unity.util.UnityVersion;
import java.io.BufferedOutputStream;
import java.io.Closeable;
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
    
    private List<Vector3f> vertices;
    private List<Vector3f> normals;
    private List<Color32> colors;
    private List<Vector2f> uv1;
    private List<Vector2f> uv2;
    private List<Vector4f> tangents;
    
    @Override
    public void extract(ObjectPath path, UnityObject obj) throws IOException {
        // TODO: support older mesh formats
        UnityVersion version = getAssetFile().getTypeTree().getEngineVersion();
        if (version == null || version.getMajor() != 4) {
            throw new UnsupportedOperationException("Unity 4 format is supported only");
        }

        mesh = new Mesh(obj);
        
        // TODO: support mesh compression
        if (mesh.meshCompression != 0) {
            throw new UnsupportedOperationException("Compressed meshes aren't supported yet");
        }
        
        readVertexData();
        
        setFileExtension("obj");
        Path objFile = getAssetFile(path.getPathID(), mesh.name);
        try (ObjWriter objWriter = new ObjWriter(objFile)) {
            writeMesh(objWriter);
        }
    }
    
    private void readVertexData() throws IOException {
        // init lists
        vertices = new ArrayList<>();
        normals = new ArrayList<>();
        colors = new ArrayList<>();
        uv1 = new ArrayList<>();
        uv2 = new ArrayList<>();
        tangents = new ArrayList<>();
        
        // get vertex buffer
        ByteBuffer vertexBuffer = mesh.vertexData.dataSize;
        vertexBuffer.order(ByteOrder.LITTLE_ENDIAN);

        L.log(Level.FINE, "Vertex buffer size: {0}", vertexBuffer.capacity());
        
        DataInputReader in = DataInputReader.newReader(vertexBuffer);
        
        List<StreamInfo> streams = mesh.vertexData.streams;
        List<ChannelInfo> channels = mesh.vertexData.channels;
        
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
                    
                    switch (j) {
                        case 0:
                            Vector3f v = new Vector3f(half);
                            v.read(in);
                            vertices.add(v);
                            break;
                            
                        case 1:
                            Vector3f n = new Vector3f(half);
                            n.read(in);
                            normals.add(n);
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
                            Vector2f uv = new Vector2f(half);
                            uv.read(in);
                            if (j == 3) {
                                uv1.add(uv);
                            } else {
                                uv2.add(uv);
                            }
                            break;
                            
                        case 5:
                            Vector4f t = new Vector4f(half);
                            t.read(in);
                            tangents.add(t);
                            break;
                    }
                }
            }
        }
    }
    
    private void writeMesh(ObjWriter obj) {
        obj.writeComment("Created by DisUnity v" + DisUnity.getVersion());
        
        if (!vertices.isEmpty()) {
            for (Vector3f v : vertices) {
                obj.writeVertex(v);
            }
        }
        
        if (!normals.isEmpty()) {
            for (Vector3f vn : normals) {
                obj.writeNormal(vn);
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
                obj.writeUV(vt);
            }
        }
        
        obj.writeLine();
        obj.writeObject(mesh.name);
        obj.writeSmooth(1);
        
        try {
            mesh.indexBuffer.order(ByteOrder.LITTLE_ENDIAN);
            DataInputReader in = DataInputReader.newReader(mesh.indexBuffer);
            for (int i = 0; i < mesh.subMeshes.size(); i++) {
                SubMesh subMesh = mesh.subMeshes.get(i);
                in.position(subMesh.firstByte);
                
                String material = String.format("%s_%d", mesh.name, i);
                obj.writeUsemtl(material);

                for (int j = (int) (subMesh.indexCount / 3); j > 0; j--) {
                    int i1 = in.readUnsignedShort() + 1;
                    int i2 = in.readUnsignedShort() + 1;
                    int i3 = in.readUnsignedShort() + 1;
                    obj.writeFace(i1, i2, i3);
                }
                
                obj.writeLine();
            }
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Index buffer reading error", ex);
        }
    }
    
    private class ObjWriter implements Closeable {
        
        final PrintStream ps;

        ObjWriter(Path file) throws IOException {
            ps = new PrintStream(new BufferedOutputStream(Files.newOutputStream(file)));
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
        
        void writeFace(int i1, int i2, int i3) {
            ps.printf("f %d/%d/%d", i1, i1, i1);
            ps.printf(" %d/%d/%d", i2, i2, i2);
            ps.printf(" %d/%d/%d\n", i3, i3, i3);
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

        @Override
        public void close() throws IOException {
            ps.close();
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

        Mesh(UnityObject obj) {
            name = obj.getValue("m_Name");
            indexBuffer = obj.getValue("m_IndexBuffer");
            meshCompression = obj.getValue("m_MeshCompression");

            UnityObject vertexDataObject = obj.getValue("m_VertexData");
            vertexData = new VertexData(vertexDataObject);

            List<UnityObject> subMeshObjects = obj.getValue("m_SubMeshes");
            subMeshes = new ArrayList<>();
            for (UnityObject subMeshObject : subMeshObjects) {
                subMeshes.add(new SubMesh(subMeshObject));
            }
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
}
