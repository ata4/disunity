/*
 ** 2013 December 26
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.util.io.image.ktx;

import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
import info.ata4.io.Struct;
import java.io.IOException;
import java.util.Arrays;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class KTXHeader implements Struct {
    
    public static final int SIZE = 64;
    
    public static byte[] IDENTIFIER = DatatypeConverter.parseHexBinary("AB4B5458203131BB0D0A1A0A");
    public static byte[] ENDIANESS_LE = new byte[] {1, 2, 3, 4};
    public static byte[] ENDIANESS_BE = new byte[] {4, 3, 2, 1};
    
    // constants for glInternalFormat
    public static final int GL_ETC1_RGB8_OES = 0x8D64;
    
    public static final int GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG = 0x8C00;
    public static final int GL_COMPRESSED_RGB_PVRTC_2BPPV1_IMG = 0x8C01;
    public static final int GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG = 0x8C02;
    public static final int GL_COMPRESSED_RGBA_PVRTC_2BPPV1_IMG = 0x8C03;
    
    public static final int GL_ATC_RGB_AMD = 0x8C92;
    public static final int GL_ATC_RGBA_EXPLICIT_ALPHA_AMD = 0x8C93;
    public static final int GL_ATC_RGBA_INTERPOLATED_ALPHA_AMD = 0x87EE;
    
    public static final int GL_COMPRESSED_RGB8_ETC2 = 0x9274;
    public static final int GL_COMPRESSED_SRGB8_ETC2 = 0x9275;
    public static final int GL_COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2 = 0x9276;
    public static final int GL_COMPRESSED_SRGB8_PUNCHTHROUGH_ALPHA1_ETC2 = 0x9277;
    public static final int GL_COMPRESSED_RGBA8_ETC2_EAC = 0x9278;
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ETC2_EAC = 0x9279;
    public static final int GL_COMPRESSED_R11_EAC = 0x9270;
    public static final int GL_COMPRESSED_SIGNED_R11_EAC = 0x9271;
    public static final int GL_COMPRESSED_RG11_EAC = 0x9272;
    public static final int GL_COMPRESSED_SIGNED_RG11_EAC = 0x9273;
    
    // constants for glBaseInternalFormat
    public static final int GL_RED = 0x1903;
    public static final int GL_GREEN = 0x1904;
    public static final int GL_BLUE = 0x1905;
    public static final int GL_ALPHA = 0x1906;
    public static final int GL_RGB = 0x1907;
    public static final int GL_RGBA = 0x1908;
    public static final int GL_RG = 0x8227;
    
    public boolean swap;
    public int glType;
    public int glTypeSize;
    public int glFormat;
    public int glInternalFormat;
    public int glBaseInternalFormat;
    public int pixelWidth;
    public int pixelHeight;
    public int pixelDepth;
    public int numberOfArrayElements;
    public int numberOfFaces;
    public int numberOfMipmapLevels;
    public int bytesOfKeyValueData;

    @Override
    public void read(DataInputReader in) throws IOException {
        byte[] identifier = new byte[IDENTIFIER.length];
        in.readFully(identifier);
        if (!Arrays.equals(identifier, IDENTIFIER)) {
            throw new IOException("Invalid identifier");
        }
        
        byte[] endianness = new byte[4];
        in.readFully(endianness);
        
        if (Arrays.equals(endianness, ENDIANESS_LE)) {
            swap = true;
        } else if (Arrays.equals(endianness, ENDIANESS_BE)) {
            swap = false;
        } else {
            throw new IOException("Invalid endianness");
        }
        
        in.setSwap(swap);
        
        glType = in.readInt();
        glTypeSize = in.readInt();
        glFormat = in.readInt();
        glInternalFormat = in.readInt();
        glBaseInternalFormat = in.readInt();
        pixelWidth = in.readInt();
        pixelHeight = in.readInt();
        pixelDepth = in.readInt();
        numberOfArrayElements = in.readInt();
        numberOfFaces = in.readInt();
        numberOfMipmapLevels = in.readInt();
        bytesOfKeyValueData = in.readInt();
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        out.setSwap(swap);
        out.write(IDENTIFIER);
        out.write(swap ? ENDIANESS_LE : ENDIANESS_BE);
        out.writeInt(glType);
        out.writeInt(glTypeSize);
        out.writeInt(glFormat);
        out.writeInt(glInternalFormat);
        out.writeInt(glBaseInternalFormat);
        out.writeInt(pixelWidth);
        out.writeInt(pixelHeight);
        out.writeInt(pixelDepth);
        out.writeInt(numberOfArrayElements);
        out.writeInt(numberOfFaces);
        out.writeInt(numberOfMipmapLevels);
        out.writeInt(bytesOfKeyValueData);
    }
    
}
