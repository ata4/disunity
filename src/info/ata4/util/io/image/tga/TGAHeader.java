/*
 ** 2013 December 25
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.util.io.image.tga;

import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
import info.ata4.io.Struct;
import java.io.IOException;

/**
 * Very simple TGA header struct.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TGAHeader implements Struct {
    
    public static final int SIZE = 18;
    
    public byte idLength;
    public byte colorMapType;
    public byte imageType;
    public int cmFirstIndex;
    public int cmLength;
    public byte cmEntrySize;
    public int originX;
    public int originY;
    public int imageWidth;
    public int imageHeight;
    public byte pixelDepth;
    public byte imageDesc;

    @Override
    public void read(DataInputReader in) throws IOException {
        idLength = in.readByte();
        colorMapType = in.readByte();
        imageType = in.readByte();
        cmFirstIndex = in.readUnsignedShort();
        cmLength = in.readUnsignedShort();
        cmEntrySize = in.readByte();
        originX = in.readUnsignedShort();
        originY = in.readUnsignedShort();
        imageWidth = in.readUnsignedShort();
        imageHeight = in.readUnsignedShort();
        pixelDepth = in.readByte();
        imageDesc = in.readByte();
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        out.writeByte(idLength);
        out.writeByte(colorMapType);
        out.writeByte(imageType);
        out.writeShort(cmFirstIndex);
        out.writeShort(cmLength);
        out.writeByte(cmEntrySize);
        out.writeShort(originX);
        out.writeShort(originY);
        out.writeShort(imageWidth);
        out.writeShort(imageHeight);
        out.writeByte(pixelDepth);
        out.writeByte(imageDesc);
    }
    
}
