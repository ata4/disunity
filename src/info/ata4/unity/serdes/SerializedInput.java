/*
 ** 2013 July 27
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.serdes;

import info.ata4.unity.struct.Struct;
import info.ata4.util.io.DataInputReader;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class SerializedInput {
    
    private static final int ALIGNMENT = 4;
    
    private final DataInputReader in;
    private int bytes;

    public SerializedInput(DataInputReader in) {
        this.in = in;
    }
    
    public byte readByte() throws IOException {
        bytes++;
        return in.readByte();
    }
    
    public int readUnsignedByte() throws IOException {
        bytes++;
        return in.readUnsignedByte();
    }

    public boolean readBoolean() throws IOException {
        bytes++;
        return in.readBoolean();
    }
    
    public int readInt() throws IOException {
        align();
        return in.readInt();
    }
    
    public long readUnsignedInt() throws IOException {
        align();
        return in.readUnsignedInt();
    }
    
    public long readLong() throws IOException {
        align();
        return in.readLong();
    }
    
    public float readFloat() throws IOException {
        align();
        return in.readFloat();
    }
    
    public double readDouble() throws IOException {
        align();
        return in.readDouble();
    }
    
    public short readShort() throws IOException {
        align();
        return in.readShort();
    }

    public int readUnsignedShort() throws IOException {
        align();
        return in.readUnsignedShort();
    }
    
    public byte[] readByteArray() throws IOException {
        int len = readInt();
        byte[] data = new byte[len];
        in.readFully(data);
        bytes = len;
        align();
        return data;
    }
    
    public String readString() throws IOException {
        return new String(readByteArray(), "UTF8");
    }
    
    public void readStruct(Struct obj) throws IOException {
        align();
        obj.read(in);
    }
    
    public void align() throws IOException {
        if (bytes > 0) {
            in.align(bytes, ALIGNMENT);
            bytes = 0;
        }
    }
}
