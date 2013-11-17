/*
 ** 2013 November 17
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.asset;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetBundleEntry {
    
    private final AssetBundle bundle;
    private String name;
    private int offset;
    private int length;

    public AssetBundleEntry(final AssetBundle outer) {
        this.bundle = outer;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return length;
    }
    
    public int getLength() {
        return length;
    }
    
    public void setLength(int length) {
        this.length = length;
    }

    public int getOffset() {
        return offset;
    }
    
    public void setOffset(int offset) {
        this.offset = offset;
    }

    public ByteBuffer getByteBuffer() throws IOException {
        ByteBuffer bbd = bundle.getDataByteBuffer();
        bbd.position(getOffset());
        ByteBuffer bb = bbd.slice();
        bb.limit(getSize());
        return bb;
    }
}
