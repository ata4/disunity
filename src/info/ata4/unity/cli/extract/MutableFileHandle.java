/*
 ** 2014 October 06
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.extract;

import java.nio.ByteBuffer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class MutableFileHandle implements FileHandle {
    
    private String name;
    private String ext;
    private ByteBuffer data;
    
    public MutableFileHandle() {
    }
    
    public MutableFileHandle(String name, String ext, ByteBuffer data) {
        this.name = name;
        this.ext = ext;
        this.data = data;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getExtension() {
        return ext;
    }

    public void setExtension(String ext) {
        this.ext = ext;
    }

    @Override
    public ByteBuffer getData() {
        return data;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
    }
}
