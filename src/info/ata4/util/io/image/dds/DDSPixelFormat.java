/*
 ** 2013 June 17
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.util.io.image.dds;

import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
import info.ata4.io.Struct;
import info.ata4.util.string.StringUtils;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DDSPixelFormat implements Struct {
    
    public static final int DDSPF_STRUCT_SIZE = 32;
    
    public static final int DDPF_ALPHAPIXELS = 0x1;
    public static final int DDPF_ALPHA = 0x2;
    public static final int DDPF_FOURCC = 0x4;
    public static final int DDPF_RGB = 0x40;
    public static final int DDPF_RGBA = DDPF_RGB | DDPF_ALPHAPIXELS;
    
    public static final int PF_DXT1 = StringUtils.makeID("DXT1");
    public static final int PF_DXT3 = StringUtils.makeID("DXT3");
    public static final int PF_DXT5 = StringUtils.makeID("DXT5");
    
    public int dwSize = DDSPF_STRUCT_SIZE;
    public int dwFlags;
    public int dwFourCC;
    public int dwRGBBitCount;
    public int dwRBitMask;
    public int dwGBitMask;
    public int dwBBitMask;
    public int dwABitMask;

    @Override
    public void read(DataInputReader in) throws IOException {
        dwSize = in.readInt();
        dwFlags = in.readInt();
        dwFourCC = in.readInt();
        dwRGBBitCount = in.readInt();
        dwRBitMask = in.readInt();
        dwGBitMask = in.readInt();
        dwBBitMask = in.readInt();
        dwABitMask = in.readInt();
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        out.writeInt(dwSize);
        out.writeInt(dwFlags);
        out.writeInt(dwFourCC);
        out.writeInt(dwRGBBitCount);
        out.writeInt(dwRBitMask);
        out.writeInt(dwGBitMask);
        out.writeInt(dwBBitMask);
        out.writeInt(dwABitMask);
    }
    
}
