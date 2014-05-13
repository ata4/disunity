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
import info.ata4.unity.asset.struct.AssetObjectPath;
import info.ata4.unity.cli.extract.AssetExtractHandler;
import info.ata4.unity.cli.extract.handler.struct.Color32;
import info.ata4.unity.cli.extract.handler.struct.Vector2f;
import info.ata4.unity.cli.extract.handler.struct.Vector3f;
import info.ata4.unity.cli.extract.handler.struct.Vector4f;
import info.ata4.unity.serdes.UnityBuffer;
import info.ata4.unity.serdes.UnityList;
import info.ata4.unity.serdes.UnityObject;
import info.ata4.unity.util.UnityVersion;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
    
    private ByteBuffer indexBuffer;
    private ByteBuffer vertexBuffer;
    private UnityObject obj;
    private String name;
    private boolean debug;

    private final List<Vector3f> vertices = new ArrayList<>();
    private final List<Vector3f> normals = new ArrayList<>();
    private final List<Color32> colors = new ArrayList<>();
    private final List<Vector2f> uv1 = new ArrayList<>();
    private final List<Vector2f> uv2 = new ArrayList<>();
    private final List<Vector4f> tangents = new ArrayList<>();

    @Override
    public void extract(AssetObjectPath path, UnityObject obj) throws IOException {
        this.obj = obj;
        name = obj.getValue("m_Name");
        
        // TODO: support older mesh formats
        UnityVersion version = getAssetFile().getClassType().getEngineVersion();
        if (version == null || version.getMajor() != 4) {
            throw new UnsupportedOperationException("Unity 4 format is supported only");
        }
        
        readIndexData();
        readVertexData();

        setFileExtension("obj");
        Path objFile = getAssetFile(path.getPathID(), name);
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(objFile))) {
            writeMesh(new PrintStream(os));
        }
        
        clear();
    }
    
    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    private void readIndexData() throws IOException {
        // get index buffer
        UnityBuffer indexBufferObj = obj.getValue("m_IndexBuffer");
        indexBuffer = indexBufferObj.getBuffer();
        indexBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
        if (debug) {
            setFileExtension("bin");
            writeFile(indexBuffer, 0, name + "_IndexBuffer");
            indexBuffer.rewind();
        }

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
        
        if (debug) {
            setFileExtension("bin");
            writeFile(vertexBuffer, 0, name + "_DataSize");
            vertexBuffer.rewind();
        }

        L.log(Level.FINE, "Vertex buffer size: {0}", vertexBuffer.capacity());
        
        UnityList channelsObj = vertexDataObj.getValue("m_Channels");
        List<ChannelInfo> channels = new ArrayList<>();
        
        // Known channels:
        // 0 - Empty
        // 1 - Coordinates (Vector3f)
        // 2 - Normals (Vector3f)
        // 3 - Colors (Color32)
        // 4 - UV layer 1 (Vector2f)
        // 5 - UV layer 2 (Vector2f)
        // 6 - Tangents (Vector4f)
        for (Object channel : channelsObj.getList()) {
            UnityObject channelObj = (UnityObject) channel;
            channels.add(new ChannelInfo(channelObj));
        }
        
        // StreamInfo data
        //   UInt32 channelMask  - Channel type mask (see above)
        //   UInt32 offset       - Vertex buffer offset
        //   UInt8 stride        - Total bytes per vertex chunk
        //   UInt8 dividerOp     - ???
        //   UInt16 frequency    - ???
        UnityList streams = vertexDataObj.getValue("m_Streams");
 
        long vertexCount = vertexDataObj.getValue("m_VertexCount");
        
        DataInputReader in = DataInputReader.newReader(vertexBuffer);
        
        for (Object stream : streams.getList()) {
            UnityObject streamObj = (UnityObject) stream;
            
            long channelMask = streamObj.getValue("channelMask");
            long offset = streamObj.getValue("offset");
            
            // skip empty channels
            if (channelMask == 0) {
                continue;
            }
            
            vertexBuffer.position((int) offset);
            
            // read vertex data from each vertex and channel
            for (int i = 0; i < vertexCount; i++) {
                for (int j = 0; j < channels.size(); j++) {
                    // skip unselected channels
                    if ((channelMask & 1 << j) == 0) {
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
        
        ps.printf("usemtl %s_%d\n", name, subMeshIndex);

        try {
            DataInputReader in = DataInputReader.newReader(indexBuffer);
            in.position((int) firstByte);

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
    
    private class ChannelInfo {
        
        int stream;
        int offset;
        int format;
        int dimension;
        
        ChannelInfo(UnityObject obj) {
            // ChannelInfo data
            //   UInt8 stream    - Index into m_Streams
            //   UInt8 offset    - Vertex chunk offset
            //   UInt8 format    - 0 = full precision, 1 = half precision
            //   UInt8 dimension - Number of fields?
            stream = obj.getValue("stream");
            offset = obj.getValue("offset");
            format = obj.getValue("format");
            dimension = obj.getValue("dimension");
        }
    }
}
