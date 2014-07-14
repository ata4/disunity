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

import info.ata4.unity.cli.extract.AssetExtractHandler;
import info.ata4.unity.engine.Mesh;
import info.ata4.unity.serdes.UnityObject;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class MeshHandler extends AssetExtractHandler {
    
    private MeshFormat format = MeshFormat.OBJ;
    
    public MeshFormat getFormat() {
        return format;
    }

    public void setFormat(MeshFormat format) {
        this.format = format;
    }
    
    @Override
    public void extract(UnityObject obj) throws IOException {
        // unpack the mesh
        MeshData meshData = new MeshData(new Mesh(obj));
        
        // choose a mesh writer
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
        
        // write mesh
        meshWriter.write(meshData);
    }
    
    PrintStream getPrintStream(String name, String ext) throws IOException {
        setOutputFileName(name);
        setOutputFileExtension(ext);
        Path file = getOutputFile();
        return new PrintStream(new BufferedOutputStream(Files.newOutputStream(file)));
    }
}
