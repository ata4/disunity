/*
 ** 2013 December 07
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.serdes;

import java.nio.ByteBuffer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class UnityBuffer extends UnityType {
    
    private ByteBuffer bb;

    public UnityBuffer(String type, ByteBuffer bb) {
        super(type);
        this.bb = bb;
    }
    
    public UnityBuffer(String type, byte[] data) {
        this(type, ByteBuffer.wrap(data));
    }

    public ByteBuffer getBuffer() {
        return bb;
    }
}
