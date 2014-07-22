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

import java.io.IOException;

/**
 * Mesh writer interface to write MeshData containers to mesh files.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
abstract class MeshWriter {
    
    protected final MeshHandler handler;

    MeshWriter(MeshHandler handler) {
        this.handler = handler;
    }
    
    abstract void write(MeshData meshData) throws IOException;
}
