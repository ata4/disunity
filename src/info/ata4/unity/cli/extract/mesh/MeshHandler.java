/*
 ** 2013 December 06
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.extract.mesh;

import info.ata4.io.DataInputReader;
import info.ata4.io.buffer.ByteBufferInputStream;
import info.ata4.io.streams.BitInputStream;
import info.ata4.log.LogUtils;
import info.ata4.unity.cli.extract.AssetExtractHandler;
import info.ata4.unity.engine.ChannelInfo;
import info.ata4.unity.engine.CompressedMesh;
import info.ata4.unity.engine.Mesh;
import info.ata4.unity.engine.PackedBitVector;
import info.ata4.unity.engine.StreamInfo;
import info.ata4.unity.engine.SubMesh;
import info.ata4.unity.engine.struct.Color32;
import info.ata4.unity.engine.struct.Vector2f;
import info.ata4.unity.engine.struct.Vector3f;
import info.ata4.unity.engine.struct.Vector4f;
import info.ata4.unity.serdes.UnityObject;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class MeshHandler extends AssetExtractHandler {
    
    private static final Logger L = LogUtils.getLogger();
    private MeshFormat format = MeshFormat.OBJ;
    
    public MeshFormat getFormat() {
        return format;
    }

    public void setFormat(MeshFormat format) {
        this.format = format;
    }
    
    @Override
    public void extract(UnityObject obj) throws IOException {
        MeshWriter meshWriter;
        
        switch (format) {
            case PLY:
                meshWriter = new PlyWriter(this);
                break;
                
            case OBJ:
                meshWriter = new ObjWriter(this);
                break;
                
            default:
                throw new RuntimeException("Unknown mesh format: " + format);
        }
        
        MeshData meshData = readMeshData(obj);
        meshWriter.write(meshData);
    }
    
    PrintStream getPrintStream(String name, String ext) throws IOException {
        setOutputFileName(name);
        setOutputFileExtension(ext);
        Path file = getOutputFile();
        return new PrintStream(new BufferedOutputStream(Files.newOutputStream(file)));
    }
    
    private MeshData readMeshData(UnityObject obj) throws IOException {
        Mesh mesh = new Mesh(obj);
        MeshData meshData = new MeshData(mesh);
        
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
                                    meshData.getVertices().add(v);
                                    break;

                                case 1:
                                    Vector3f vn = new Vector3f();
                                    vn.setHalf(half);
                                    vn.read(in);
                                    meshData.getNormals().add(vn);
                                    if (half && channel.dimension == 4) {
                                        in.skipBytes(2); // padding?
                                    }
                                    break;

                                case 2:
                                    Color32 c = new Color32();
                                    c.read(in);
                                    meshData.getColors().add(c);
                                    break;

                                case 3:
                                case 4:
                                    Vector2f vt = new Vector2f();
                                    vt.setHalf(half);
                                    vt.read(in);
                                    if (j == 3) {
                                        meshData.getUV1().add(vt);
                                    } else {
                                        meshData.getUV2().add(vt);
                                    }
                                    break;

                                case 5:
                                    Vector4f t = new Vector4f();
                                    t.setHalf(half);
                                    t.read(in);
                                    meshData.getTangents().add(t);
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
                        meshData.getTriangles().add(in.readUnsignedShort());
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
                meshData.getVertices().add(v);
            }
            
            float[] normalFloats = readPackedNormals(cmesh.normals, cmesh.normalSigns);
            for (int i = 0; i < normalFloats.length / 3; i++) {
                Vector3f vn = new Vector3f();
                vn.x = normalFloats[i * 3];
                vn.y = normalFloats[i * 3 + 1];
                vn.z = normalFloats[i * 3 + 2];
                meshData.getNormals().add(vn);
            }
            
            float[] uvFloats = readPackedFloats(cmesh.UV);
            for (int i = 0; i < uvFloats.length / 2; i++) {
                Vector2f vt = new Vector2f();
                vt.x = uvFloats[i * 2];
                vt.y = uvFloats[i * 2 + 1];
                if (i < vertexFloats.length / 3) {
                    meshData.getUV1().add(vt);
                } else {
                    meshData.getUV2().add(vt);
                }
            }
            
            int[] colorInts = readPackedBits(cmesh.colors);
            for (int i = 0; i < colorInts.length; i++) {
                Color32 c = new Color32();
                c.fromInt(colorInts[i]);
                meshData.getColors().add(c);
            }
            
            int[] triangleInts = readPackedBits(cmesh.triangles);
            for (int i = 0; i < triangleInts.length; i++) {
                meshData.getTriangles().add(triangleInts[i]);
            }
        }
        
        return meshData;
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
}
