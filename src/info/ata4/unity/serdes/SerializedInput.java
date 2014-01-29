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

import info.ata4.io.DataInputReader;
import info.ata4.io.Struct;
import java.io.IOException;
import java.math.BigInteger;

/**
 * Low-level input layer for reading serialized data and handling alignment.
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
    
    public short readShort() throws IOException {
        bytes += 2;
        return in.readShort();
    }

    public int readUnsignedShort() throws IOException {
        bytes += 2;
        return in.readUnsignedShort();
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
    
    public BigInteger readUnsignedLong() throws IOException {
        align();
        return in.readUnsignedLong();
    }
    
    public float readFloat() throws IOException {
        align();
        return in.readFloat();
    }
    
    public double readDouble() throws IOException {
        align();
        return in.readDouble();
    }
    
    public byte[] readByteArray(int size) throws IOException {
        byte[] data = new byte[size];
        
        // NOTE: AudioClips "fake" the size of m_AudioData when the stream is
        // stored in a separate file. The array contains just an offset integer
        // in that case, so pay attention to the bytes remaining in the buffer
        // as well to avoid EOFExceptions.
        // TODO: is there a flag for this behavior?
        size = Math.min(size, (int) in.remaining());
        
        in.readFully(data, 0, size);
        bytes = size;
        align();
        return data;
    }
    
    public byte[] readByteArray() throws IOException {
        int len = readInt();
        return readByteArray(len);
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
