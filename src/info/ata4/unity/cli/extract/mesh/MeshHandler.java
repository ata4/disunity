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

import info.ata4.unity.engine.CompressedMesh;
import info.ata4.unity.engine.PackedBitVector;
import info.ata4.unity.engine.ChannelInfo;
import info.ata4.unity.engine.SubMesh;
import info.ata4.unity.engine.StreamInfo;
import info.ata4.unity.engine.Mesh;
import info.ata4.io.DataInputReader;
import info.ata4.io.buffer.ByteBufferInputStream;
import info.ata4.io.streams.BitInputStream;
import info.ata4.log.LogUtils;
import info.ata4.unity.DisUnity;
import info.ata4.unity.cli.extract.AssetExtractHandler;
import info.ata4.unity.serdes.UnityObject;
import info.ata4.unity.engine.struct.Color32;
import info.ata4.unity.engine.struct.Vector2f;
import info.ata4.unity.engine.struct.Vector3f;
import info.ata4.unity.engine.struct.Vector4f;
import info.ata4.unity.util.UnityVersion;
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
}
