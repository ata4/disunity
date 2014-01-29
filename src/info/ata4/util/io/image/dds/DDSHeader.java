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
public class DDSHeader implements Struct {
    
    public static final int DDS_MAGIC = StringUtils.makeID("DDS ");
    public static final int DDS_STRUCT_SIZE = 124;
    
    public static final int SIZE = DDS_STRUCT_SIZE + 4;
    
    public static final int DDS_FOURCC      = 0x00000004 ; // DDPF_FOURCC
    public static final int DDS_RGB         = 0x00000040 ; // DDPF_RGB
    public static final int DDS_RGBA        = 0x00000041 ; // DDPF_RGB | DDPF_ALPHAPIXELS
    public static final int DDS_LUMINANCE   = 0x00020000 ; // DDPF_LUMINANCE
    public static final int DDS_LUMINANCEA  = 0x00020001 ; // DDPF_LUMINANCE | DDPF_ALPHAPIXELS
    public static final int DDS_ALPHA       = 0x00000002 ; // DDPF_ALPHA
    public static final int DDS_PAL8        = 0x00000020 ; // DDPF_PALETTEINDEXED8

    public static final int DDS_HEADER_FLAGS_TEXTURE        = 0x00001007 ; // DDSD_CAPS | DDSD_HEIGHT | DDSD_WIDTH | DDSD_PIXELFORMAT
    public static final int DDS_HEADER_FLAGS_MIPMAP         = 0x00020000 ; // DDSD_MIPMAPCOUNT
    public static final int DDS_HEADER_FLAGS_VOLUME         = 0x00800000 ; // DDSD_DEPTH
    public static final int DDS_HEADER_FLAGS_PITCH          = 0x00000008 ; // DDSD_PITCH
    public static final int DDS_HEADER_FLAGS_LINEARSIZE     = 0x00080000 ; // DDSD_LINEARSIZE

    public static final int DDS_HEIGHT = 0x00000002; // DDSD_HEIGHT
    public static final int DDS_WIDTH  = 0x00000004; // DDSD_WIDTH

    public static final int DDS_SURFACE_FLAGS_TEXTURE = 0x00001000; // DDSCAPS_TEXTURE
    public static final int DDS_SURFACE_FLAGS_MIPMAP  = 0x00400008; // DDSCAPS_COMPLEX | DDSCAPS_MIPMAP
    public static final int DDS_SURFACE_FLAGS_CUBEMAP = 0x00000008; // DDSCAPS_COMPLEX

    public static final int DDS_CUBEMAP_POSITIVEX = 0x00000600; // DDSCAPS2_CUBEMAP | DDSCAPS2_CUBEMAP_POSITIVEX
    public static final int DDS_CUBEMAP_NEGATIVEX = 0x00000a00; // DDSCAPS2_CUBEMAP | DDSCAPS2_CUBEMAP_NEGATIVEX
    public static final int DDS_CUBEMAP_POSITIVEY = 0x00001200; // DDSCAPS2_CUBEMAP | DDSCAPS2_CUBEMAP_POSITIVEY
    public static final int DDS_CUBEMAP_NEGATIVEY = 0x00002200; // DDSCAPS2_CUBEMAP | DDSCAPS2_CUBEMAP_NEGATIVEY
    public static final int DDS_CUBEMAP_POSITIVEZ = 0x00004200; // DDSCAPS2_CUBEMAP | DDSCAPS2_CUBEMAP_POSITIVEZ
    public static final int DDS_CUBEMAP_NEGATIVEZ = 0x00008200; // DDSCAPS2_CUBEMAP | DDSCAPS2_CUBEMAP_NEGATIVEZ

    public static final int DDS_CUBEMAP_ALLFACES  = (DDS_CUBEMAP_POSITIVEX | DDS_CUBEMAP_NEGATIVEX | DDS_CUBEMAP_POSITIVEY | DDS_CUBEMAP_NEGATIVEY | DDS_CUBEMAP_POSITIVEZ | DDS_CUBEMAP_NEGATIVEZ);

    public static final int DDS_CUBEMAP = 0x00000200; // DDSCAPS2_CUBEMAP

    public static final int DDS_FLAGS_VOLUME = 0x00200000; // DDSCAPS2_VOLUME

    public int dwMagic = DDS_MAGIC;
    public int dwSize = DDS_STRUCT_SIZE;
    public int dwFlags = DDS_HEADER_FLAGS_TEXTURE;
    public int dwHeight;
    public int dwWidth;
    public int dwPitchOrLinearSize;
    public int dwDepth;
    public int dwMipMapCount;
    public int[] dwReserved1 = new int[11];
    public DDSPixelFormat ddspf = new DDSPixelFormat();
    public int dwCaps = DDS_SURFACE_FLAGS_TEXTURE;
    public int dwCaps2;
    public int dwCaps3;
    public int dwCaps4;
    public int dwReserved2;

    @Override
    public void read(DataInputReader in) throws IOException {
        dwMagic = in.readInt();
        dwSize = in.readInt();
        dwFlags = in.readInt();
        dwHeight = in.readInt();
        dwWidth = in.readInt();
        dwPitchOrLinearSize = in.readInt();
        dwDepth = in.readInt();
        dwMipMapCount = in.readInt();
        
        for (int i = 0; i < dwReserved1.length; i++) {
            dwReserved1[i] = in.readInt();
        }
        
        ddspf.read(in);
        
        dwCaps = in.readInt();
        dwCaps2 = in.readInt();
        dwCaps3 = in.readInt();
        dwCaps4 = in.readInt();
        dwReserved2 = in.readInt();
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        out.writeInt(dwMagic);
        out.writeInt(dwSize);
        out.writeInt(dwFlags);
        out.writeInt(dwHeight);
        out.writeInt(dwWidth);
        out.writeInt(dwPitchOrLinearSize);
        out.writeInt(dwDepth);
        out.writeInt(dwMipMapCount);
        
        for (int i = 0; i < dwReserved1.length; i++) {
            out.writeInt(dwReserved1[i]);
        }

        ddspf.write(out);
        
        out.writeInt(dwCaps);
        out.writeInt(dwCaps2);
        out.writeInt(dwCaps3);
        out.writeInt(dwCaps4);
        out.writeInt(dwReserved2);
    }
    
}
