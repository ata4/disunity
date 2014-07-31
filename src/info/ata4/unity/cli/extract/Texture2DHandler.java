/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.extract;

import info.ata4.io.DataOutputWriter;
import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.log.LogUtils;
import info.ata4.unity.asset.struct.ObjectPath;
import info.ata4.unity.enums.TextureFormat;
import static info.ata4.unity.enums.TextureFormat.*;
import info.ata4.unity.serdes.UnityObject;
import info.ata4.util.io.image.dds.DDSHeader;
import info.ata4.util.io.image.dds.DDSPixelFormat;
import info.ata4.util.io.image.ktx.KTXHeader;
import info.ata4.util.io.image.tga.TGAHeader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class Texture2DHandler extends AssetExtractHandler {
    
    private static final Logger L = LogUtils.getLogger();
    
    private ObjectPath path;
    private Texture2D tex;
    private boolean tgaSaveMipMaps = true;
    
    public boolean isTargaSaveMipMaps() {
        return tgaSaveMipMaps;
    }

    public void setTargaSaveMipMaps(boolean tgaSaveMipMaps) {
        this.tgaSaveMipMaps = tgaSaveMipMaps;
    }
    
    @Override
    public void extract(UnityObject obj) throws IOException {
        this.path = path;

        try {
            // create Texture2D from serialized object
            tex = new Texture2D(obj);
        } catch (RuntimeException ex) {
            L.log(Level.WARNING, "Deserialization error", ex);
            return;
        }
        
        if (tex.textureFormat == null) {
            L.log(Level.WARNING, "Texture2D {0}: Unknown texture format {1}",
                    new Object[] {tex.name, tex.textureFormatOrd});
            return;
        }

        // some textures (font textures?) don't have any image data, not sure why...
        if (tex.imageBuffer.capacity() == 0) {
            L.log(Level.WARNING, "Texture2D {0}: Empty image buffer", tex.name);
            return;
        }
        
        // choose a fitting container format
        switch (tex.textureFormat) {
            case Alpha8:
            case RGB24:
            case RGBA32:
            case BGRA32:
            case ARGB32:
            case ARGB4444:
            case RGBA4444:
            case RGB565:
                extractTGA();
                break;
            
            case PVRTC_RGB2:
            case PVRTC_RGBA2:
            case PVRTC_RGB4:
            case PVRTC_RGBA4:
            case ATC_RGB4:
            case ATC_RGBA8:
            case ETC_RGB4:
            case ETC2_RGB4:
            case ETC2_RGB4_PUNCHTHROUGH_ALPHA:
            case ETC2_RGBA8:
            case EAC_R:
            case EAC_R_SIGNED:
            case EAC_RG:
            case EAC_RG_SIGNED:
                extractKTX();
                break;

            case DXT1:
            case DXT5:
                extractDDS();
                break;
                
            default:
                L.log(Level.WARNING, "Texture2D {0}: Unsupported texture format {1}",
                        new Object[] {tex.name, tex.textureFormat});
        }
    }
    
    private int getMipMapCount(int width, int height) {
        int mipMapCount = 1;
        for (int dim = Math.max(width, height); dim > 1; dim /= 2) {
            mipMapCount++;
        }
        return mipMapCount;
    }
    
    private void extractDDS() throws IOException {
        DDSHeader header = new DDSHeader();
        header.dwWidth = tex.width;
        header.dwHeight = tex.height;

        switch (tex.textureFormat) {
            case Alpha8:
                header.ddspf.dwFlags = DDSPixelFormat.DDPF_ALPHA;
                header.ddspf.dwABitMask = 0xff;
                header.ddspf.dwRGBBitCount = 8;
                break;
                
            case RGB24:
                header.ddspf.dwFlags = DDSPixelFormat.DDPF_RGB;
                header.ddspf.dwRBitMask = 0xff0000;
                header.ddspf.dwGBitMask = 0x00ff00;
                header.ddspf.dwBBitMask = 0x0000ff;
                header.ddspf.dwRGBBitCount = 24;
                break;
                
            case RGBA32:
                header.ddspf.dwFlags = DDSPixelFormat.DDPF_RGBA;
                header.ddspf.dwRBitMask = 0x000000ff;
                header.ddspf.dwGBitMask = 0x0000ff00;
                header.ddspf.dwBBitMask = 0x00ff0000;
                header.ddspf.dwABitMask = 0xff000000;
                header.ddspf.dwRGBBitCount = 32;
                break;
                
            case BGRA32:
                header.ddspf.dwFlags = DDSPixelFormat.DDPF_RGBA;
                header.ddspf.dwRBitMask = 0x00ff0000;
                header.ddspf.dwGBitMask = 0x0000ff00;
                header.ddspf.dwBBitMask = 0x000000ff;
                header.ddspf.dwABitMask = 0xff000000;
                header.ddspf.dwRGBBitCount = 32;
                break;
                
            case ARGB32:
                header.ddspf.dwFlags = DDSPixelFormat.DDPF_RGBA;
                header.ddspf.dwRBitMask = 0x0000ff00;
                header.ddspf.dwGBitMask = 0x00ff0000;
                header.ddspf.dwBBitMask = 0xff000000;
                header.ddspf.dwABitMask = 0x000000ff;
                header.ddspf.dwRGBBitCount = 32;
                break;
                    
            case ARGB4444:
                header.ddspf.dwFlags = DDSPixelFormat.DDPF_RGBA;
                header.ddspf.dwRBitMask = 0x0f00;
                header.ddspf.dwGBitMask = 0x00f0;
                header.ddspf.dwBBitMask = 0x000f;
                header.ddspf.dwABitMask = 0xf000;
                header.ddspf.dwRGBBitCount = 16;
                break;
                
            case RGBA4444:
                header.ddspf.dwFlags = DDSPixelFormat.DDPF_RGBA;
                header.ddspf.dwRBitMask = 0xf000;
                header.ddspf.dwGBitMask = 0x0f00;
                header.ddspf.dwBBitMask = 0x00f0;
                header.ddspf.dwABitMask = 0x000f;
                header.ddspf.dwRGBBitCount = 16;
                break;
                
            case RGB565:
                header.ddspf.dwFlags = DDSPixelFormat.DDPF_RGB;
                header.ddspf.dwRBitMask = 0xf800;
                header.ddspf.dwGBitMask = 0x07e0;
                header.ddspf.dwBBitMask = 0x001f;
                header.ddspf.dwRGBBitCount = 16;
                break;
            
            case DXT1:
                header.ddspf.dwFourCC = DDSPixelFormat.PF_DXT1;
                break;
            
            case DXT5:
                header.ddspf.dwFourCC = DDSPixelFormat.PF_DXT5; 
                break;
                
            default:
                throw new IllegalStateException("Invalid texture format for DDS: " + tex.textureFormat);
        }
        
        // set mip map flags if required
        if (tex.mipMap) {
            header.dwFlags |= DDSHeader.DDS_HEADER_FLAGS_MIPMAP;
            header.dwCaps |= DDSHeader.DDS_SURFACE_FLAGS_MIPMAP;
            header.dwMipMapCount = getMipMapCount(header.dwWidth, header.dwHeight);
        }
        
        if( tex.textureFormat == DXT1 || tex.textureFormat == DXT5 )
        {
            flipDDSVertically( header.dwMipMapCount );
        }
        
        // set and calculate linear size
        header.dwFlags |= DDSHeader.DDS_HEADER_FLAGS_LINEARSIZE;
        if (header.ddspf.dwFourCC != 0) {
            header.dwPitchOrLinearSize = header.dwWidth * header.dwHeight;
            
            if (tex.textureFormat == TextureFormat.DXT1) {
                header.dwPitchOrLinearSize /= 2;
            }
            
            header.ddspf.dwFlags |= DDSPixelFormat.DDPF_FOURCC;
        } else {
            header.dwPitchOrLinearSize = (tex.width * tex.height * header.ddspf.dwRGBBitCount) / 8;
        }
        
        ByteBuffer bbTex = ByteBuffer.allocateDirect(DDSHeader.SIZE + tex.imageBuffer.capacity());
        bbTex.order(ByteOrder.LITTLE_ENDIAN);
        
        // write header
        DataOutputWriter out = DataOutputWriter.newWriter(bbTex);
        header.write(out);
        
        // write data
        bbTex.put(tex.imageBuffer);
        
        bbTex.rewind();
        
        setOutputFileName(tex.name);
        setOutputFileExtension("dds");
        writeData(bbTex);
    }
    
    /**
     * Flip image vertically (DXT1 and DXT5 only)
     * @param mipmapCount mipmap count
     */
    private void flipDDSVertically( int mipmapCount )
    {
        if( tex.textureFormat != DXT1 && tex.textureFormat != DXT5 )
        {
            throw new IllegalStateException("Only DXT1 and DXT5 compressed image are supported. Texture format: " + tex.textureFormat);
        }
        
        int width = tex.width, height = tex.height;
        byte[] imageData = tex.imageBuffer.array();
        byte[] imageDataCopy = new byte[ imageData.length ];
        
        int mipmapByteOffset = 0;
        
        int blockByteSize = 8;
        if( tex.textureFormat == DXT5 )
        {
            blockByteSize = 16;
        }
        
        // For each texture mipmap plane
        for( int i = 0; i < mipmapCount; ++i )
        {
            int byteCount = ( ( width + 3 ) / 4 ) * ( ( height + 3 ) / 4 ) * blockByteSize;
            int widthBlockCount = ( ( width + 3 ) / 4 );
            int heightBlockCount = ( ( height + 3 ) / 4 );
            int blockRowByteCount =  widthBlockCount * blockByteSize;
            
            // Process one row of block at a time
            for( int j = 0; j < heightBlockCount; ++j )
            {
                int srcRowOffset = mipmapByteOffset + j * blockRowByteCount;
                int dstRowOffset = mipmapByteOffset + ( heightBlockCount - j - 1 ) * blockRowByteCount;
                
                // Copy each src block row from top to bottom to dst from bottom to top. This flips vertically DXTn block rows.
                System.arraycopy( imageData, srcRowOffset, imageDataCopy, dstRowOffset, blockRowByteCount );
                
                /**
                 * Flip vertically pixels of each DXTn block
                 * 
                 * DXT1 color block layout (64 bits, 4x4 pixel block)
                 * bytes 0 and 1: color0
                 * bytes 2 and 3: color1
                 * bytes 4 to 7: 4x4 2 bits lookup table (32 bits)
                 * 
                 * DXT5 color + alpha block layout (128 bits, 4x4 pixel block)
                 * byte 0 alpha0
                 * byte 1 alpha1
                 * bytes 2 to 7 4x4 3 bits lookup table (48 bits)
                 * bytes 8 to 15 DXT1 block
                 * 
                 * See: http://www.opengl.org/registry/specs/EXT/texture_compression_s3tc.txt
                 */
                for( int k = 0; k < widthBlockCount; ++k )
                {
                    // color block data start position in DTX1 block (no alpha data).
                    int blockByteOffset = dstRowOffset + k * blockByteSize + 4;
                     
                    if( tex.textureFormat == DXT5 )
                    {
                        // 4x4 pixel alpha block vertical flip
                        //

                        // alpha block data start position
                        blockByteOffset = dstRowOffset + k * blockByteSize + 2;
                        
                        /**
                         * Since row are made of 12 bits (4 pixels of 3 bits each) row data is not byte aligned, so append data in integers, to work with bit masks.
                         * In row12 and row34:
                         * Nibbles (half-bytes) are ordered like this: 0,1,2,3,4,5,6,7,8,9,10,11
                         * After vertical flip, nibbles follow this order: 9,10,11,6,7,8,3,4,5,0,1,2
                         */
                        int row12 = ( ( imageDataCopy[ blockByteOffset + 5 ] << 16 ) & 0x00ff0000 ) |
                                    ( ( imageDataCopy[ blockByteOffset + 4 ] << 8 ) & 0x0000ff00 ) |
                                    ( ( imageDataCopy[ blockByteOffset + 3 ] ) & 0x000000ff );
                        int row34 = ( ( imageDataCopy[ blockByteOffset + 2 ] << 16 ) & 0x00ff0000 ) |
                                    ( ( imageDataCopy[ blockByteOffset + 1 ] << 8 ) & 0x0000ff00 ) |
                                    ( ( imageDataCopy[ blockByteOffset + 0 ] ) & 0x000000ff );
                        
                        // After these two lines nibbles follow this order : 3,4,5,0,1,2,9,10,11,6,7,8
                        row12 = ( ( row12 & 0x00000fff ) << 12 ) | ( ( row12 & 0x00fff000 ) >> 12 );
                        row34 = ( ( row34 & 0x00000fff ) << 12 ) | ( ( row34 & 0x00fff000 ) >> 12 );
                        
                        // Nibbles can now be swapped by pair
                        imageDataCopy[ blockByteOffset + 5 ] = (byte)( ( row34 & 0x00ff0000 ) >> 16 );
                        imageDataCopy[ blockByteOffset + 4 ] = (byte)( ( row34 & 0x0000ff00 ) >> 8 );
                        imageDataCopy[ blockByteOffset + 3 ] = (byte)( ( row34 & 0x000000ff ) );
                        imageDataCopy[ blockByteOffset + 2 ] = (byte)( ( row12 & 0x00ff0000 ) >> 16 );
                        imageDataCopy[ blockByteOffset + 1 ] = (byte)( ( row12 & 0x0000ff00 ) >> 8 );
                        imageDataCopy[ blockByteOffset + 0 ] = (byte)( ( row12 & 0x000000ff ) );
                        
                        // color block data start position after alpha block
                        blockByteOffset += 10;
                    }
                    
                    // 4x4 pixel color block vertical flip
                    //
                    
                    // switch row 1 and 4
                    byte temp = imageDataCopy[ blockByteOffset ];
                    imageDataCopy[ blockByteOffset ] = imageDataCopy[ blockByteOffset + 3 ];
                    imageDataCopy[ blockByteOffset + 3 ] = temp;
                    
                    // switch row 2 and 3
                    temp = imageDataCopy[ blockByteOffset + 1 ];
                    imageDataCopy[ blockByteOffset + 1 ] = imageDataCopy[ blockByteOffset + 2 ];
                    imageDataCopy[ blockByteOffset + 2 ] = temp;
                }
            }
            
            mipmapByteOffset += byteCount;
            width = Math.max( 1 , width / 2 );
            height = Math.max( 1 , height / 2 );
        }
        
        tex.imageBuffer.rewind();
        tex.imageBuffer.put( imageDataCopy );
        tex.imageBuffer.rewind();
    }
    
    private void extractPKM() throws IOException {
        // texWidth and texHeight are width and height rounded up to multiple of 4.
        int texWidth = ((tex.width - 1) | 3) + 1;
        int texHeight = ((tex.height - 1) | 3) + 1;

        ByteBuffer res = ByteBuffer.allocateDirect(16 + tex.imageBuffer.capacity());
        res.order(ByteOrder.BIG_ENDIAN);

        res.putLong(0x504b4d2031300000L); // PKM 10\0\0

        res.putShort((short) texWidth);
        res.putShort((short) texHeight);
        res.putShort(tex.width.shortValue());
        res.putShort(tex.height.shortValue());

        res.put(tex.imageBuffer);

        res.rewind();

        setOutputFileName(tex.name);
        setOutputFileExtension("pkm");
        writeData(res);
    }

    private void extractTGA() throws IOException {
        TGAHeader header = new TGAHeader();
        
        switch (tex.textureFormat) {
            case Alpha8:
                header.imageType = 3;
                header.pixelDepth = 8;
                break;

            case RGBA32:
            case ARGB32:
            case BGRA32:
            case RGBA4444:
            case ARGB4444:
                header.imageType = 2;
                header.pixelDepth = 32;
                break;

            case RGB24:
            case RGB565:
                header.imageType = 2;
                header.pixelDepth = 24;
                break;

            default:
                throw new IllegalStateException("Invalid texture format for TGA: " + tex.textureFormat);
        }
        
        convertToRGBA32();
        
        ByteBuffer bb = tex.imageBuffer;

        int mipMapCount = 1;
        
        if (tex.mipMap) {
            mipMapCount = getMipMapCount(tex.width, tex.height);
        }
        
        for (int i = 0; i < tex.imageCount; i++) {
            header.imageWidth = tex.width;
            header.imageHeight = tex.height;
            
            for (int j = 0; j < mipMapCount; j++) {
                int imageSize = header.imageWidth * header.imageHeight * header.pixelDepth / 8;
                
                if (tgaSaveMipMaps || j == 0) {
                    ByteBuffer bbTga = ByteBuffer.allocateDirect(TGAHeader.SIZE + imageSize);
                    bbTga.order(ByteOrder.LITTLE_ENDIAN);

                    // write TGA header
                    DataOutputWriter out = DataOutputWriter.newWriter(bbTga);
                    header.write(out);

                    // write image data
                    bb.limit(bb.position() + imageSize);
                    bbTga.put(bb);
                    bb.limit(bb.capacity());

                    assert !bbTga.hasRemaining();

                    // write file
                    bbTga.rewind();

                    String fileName = tex.name;

                    if (tex.imageCount > 1) {
                        fileName += "_" + i;
                    }

                    if (tex.mipMap && tgaSaveMipMaps) {
                        fileName += "_mip_" + j;
                    }

                    setOutputFileName(fileName);
                    setOutputFileExtension("tga");
                    writeData(bbTga);
                } else {
                    bb.position(bb.position() + imageSize);
                }

                // prepare for the next mip map
                if (header.imageWidth > 1) {
                    header.imageWidth /= 2;
                }
                if (header.imageHeight > 1) {
                    header.imageHeight /= 2;
                }
            }
        }

        assert !bb.hasRemaining();
    }
    
    private void convertToRGBA32() {
        ByteBuffer imageBuffer = tex.imageBuffer;
        TextureFormat tf = tex.textureFormat;
        
        if (tf == RGBA32 || tf == ARGB32) {
            // convert ARGB and RGBA directly by swapping the bytes to get BGRA
            byte[] pixelOld = new byte[4];
            byte[] pixelNew = new byte[4];
            for (int i = 0; i < imageBuffer.capacity() / 4; i++) {
                imageBuffer.mark();
                imageBuffer.get(pixelOld);
                
                if (tf == ARGB32) {
                    // ARGB -> BGRA
                    pixelNew[0] = pixelOld[3];
                    pixelNew[1] = pixelOld[2];
                    pixelNew[2] = pixelOld[1];
                    pixelNew[3] = pixelOld[0];
                } else {
                    // RGBA -> BGRA
                    pixelNew[0] = pixelOld[2];
                    pixelNew[1] = pixelOld[1];
                    pixelNew[2] = pixelOld[0];
                    pixelNew[3] = pixelOld[3];
                }
                
                imageBuffer.reset();
                imageBuffer.put(pixelNew);
            }

            imageBuffer.rewind();
        } if (tf == RGB24) {
            // convert RGB directly to BGR
            byte[] pixelOld = new byte[3];
            byte[] pixelNew = new byte[3];
            for (int i = 0; i < imageBuffer.capacity() / 3; i++) {
                imageBuffer.mark();
                imageBuffer.get(pixelOld);
                
                pixelNew[0] = pixelOld[2];
                pixelNew[1] = pixelOld[1];
                pixelNew[2] = pixelOld[0];
                
                imageBuffer.reset();
                imageBuffer.put(pixelNew);
            }

            imageBuffer.rewind();
        } else if (tf == ARGB4444 || tf == RGBA4444) {
            // convert 16 bit RGBA/ARGB to 32 bit BGRA
            int newImageSize = imageBuffer.capacity() * 2;
            ByteBuffer imageBufferNew = ByteBuffer.allocateDirect(newImageSize);
            
            byte[] pixelOld = new byte[4];
            byte[] pixelNew = new byte[4];
            for (int i = 0; i < imageBuffer.capacity() / 2; i++) {
                int pixelOldShort = imageBuffer.getShort();
                
                pixelOld[0] = (byte) ((pixelOldShort & 0xf000) >> 12);
                pixelOld[1] = (byte) ((pixelOldShort & 0x0f00) >> 8);
                pixelOld[2] = (byte) ((pixelOldShort & 0x00f0) >> 4);
                pixelOld[3] = (byte)  (pixelOldShort & 0x000f);
                
                // convert range
                pixelOld[0] <<= 4;
                pixelOld[1] <<= 4;
                pixelOld[2] <<= 4;
                pixelOld[3] <<= 4;
                
                if (tf == ARGB4444) {
                    // ARBG -> BGRA
                    pixelNew[0] = pixelOld[3];
                    pixelNew[1] = pixelOld[2];
                    pixelNew[2] = pixelOld[1];
                    pixelNew[3] = pixelOld[0];
                } else {
                    // RBGA -> BGRA
                    pixelNew[0] = pixelOld[2];
                    pixelNew[1] = pixelOld[1];
                    pixelNew[2] = pixelOld[0];
                    pixelNew[3] = pixelOld[3];
                }
                
                imageBufferNew.put(pixelNew);
            }
            
            assert !imageBuffer.hasRemaining();
            assert !imageBufferNew.hasRemaining();
            
            imageBufferNew.rewind();
            imageBuffer = imageBufferNew;
        } else if (tf == RGB565) {
            // convert 16 bit RGB to 24 bit
            int newImageSize = (imageBuffer.capacity() / 2) * 3;
            ByteBuffer imageBufferNew = ByteBuffer.allocateDirect(newImageSize);
            
            byte[] pixel = new byte[3];
            for (int i = 0; i < imageBuffer.capacity() / 2; i++) {
                short pixelOld = imageBuffer.getShort();

                pixel[0] = (byte) ((pixelOld & 0xf800) >> 11);
                pixel[1] = (byte) ((pixelOld & 0x07e0) >> 5);
                pixel[2] = (byte)  (pixelOld & 0x001f);

                // fix color mapping (http://stackoverflow.com/a/9069480)
                pixel[0] = (byte) ((pixel[0] * 527 + 23) >> 6);
                pixel[1] = (byte) ((pixel[1] * 259 + 33) >> 6);
                pixel[2] = (byte) ((pixel[2] * 527 + 23) >> 6);
                
                imageBufferNew.put(pixel);
            }
            
            assert !imageBuffer.hasRemaining();
            assert !imageBufferNew.hasRemaining();
            
            imageBufferNew.rewind();
            imageBuffer = imageBufferNew;
        }
        
        tex.imageBuffer = imageBuffer;
    }
    
    private void extractKTX() throws IOException {
        KTXHeader header = new KTXHeader();
        header.swap = true;
        header.glTypeSize = 1;
        header.pixelWidth = tex.width;
        header.pixelHeight = tex.height;
        header.pixelDepth = 0;
        header.numberOfFaces = tex.imageCount;
        header.numberOfMipmapLevels = tex.mipMap ? getMipMapCount(header.pixelWidth, header.pixelHeight) : 1;
        int bpp;
        
        switch (tex.textureFormat) {
            case PVRTC_RGB2:
                header.glInternalFormat = KTXHeader.GL_COMPRESSED_RGB_PVRTC_2BPPV1_IMG;
                header.glBaseInternalFormat = KTXHeader.GL_RGB;
                bpp = 2;
                break;
                
            case PVRTC_RGBA2:
                header.glInternalFormat = KTXHeader.GL_COMPRESSED_RGBA_PVRTC_2BPPV1_IMG;
                header.glBaseInternalFormat = KTXHeader.GL_RGBA;
                bpp = 2;
                break;

            case PVRTC_RGB4:
                header.glInternalFormat = KTXHeader.GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG;
                header.glBaseInternalFormat = KTXHeader.GL_RGB;
                bpp = 4;
                break;
                
            case PVRTC_RGBA4:
                header.glInternalFormat = KTXHeader.GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG;
                header.glBaseInternalFormat = KTXHeader.GL_RGBA;
                bpp = 4;
                break;
                
            case ATC_RGB4:
                header.glInternalFormat = KTXHeader.GL_ATC_RGB_AMD;
                header.glBaseInternalFormat = KTXHeader.GL_RGB;
                bpp = 4;
                break;

            case ATC_RGBA8:
                header.glInternalFormat = KTXHeader.GL_ATC_RGBA_EXPLICIT_ALPHA_AMD;
                header.glBaseInternalFormat = KTXHeader.GL_RGBA;
                bpp = 8;
                break;
                
            case ETC_RGB4:
                header.glInternalFormat = KTXHeader.GL_ETC1_RGB8_OES;
                header.glBaseInternalFormat = KTXHeader.GL_RGB;
                bpp = 4;
                break;
                
            case ETC2_RGB4:
                header.glInternalFormat = KTXHeader.GL_COMPRESSED_RGB8_ETC2;
                header.glBaseInternalFormat = KTXHeader.GL_RGB;
                bpp = 4;
                break;
                
            case ETC2_RGB4_PUNCHTHROUGH_ALPHA:
                header.glInternalFormat = KTXHeader.GL_COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2;
                header.glBaseInternalFormat = KTXHeader.GL_RGBA;
                bpp = 4;
                break;
                
            case ETC2_RGBA8:
                header.glInternalFormat = KTXHeader.GL_COMPRESSED_RGBA8_ETC2_EAC;
                header.glBaseInternalFormat = KTXHeader.GL_RGBA;
                bpp = 8;
                break;
         
            case EAC_R:
                header.glInternalFormat = KTXHeader.GL_COMPRESSED_R11_EAC;
                header.glBaseInternalFormat = KTXHeader.GL_RED;
                bpp = 4;
                break;
                
            case EAC_R_SIGNED:
                header.glInternalFormat = KTXHeader.GL_COMPRESSED_SIGNED_R11_EAC;
                header.glBaseInternalFormat = KTXHeader.GL_RED;
                bpp = 4;
                break;
                
            case EAC_RG:
                header.glInternalFormat = KTXHeader.GL_COMPRESSED_RG11_EAC;
                header.glBaseInternalFormat = KTXHeader.GL_RG;
                bpp = 8;
                break;
                
            case EAC_RG_SIGNED:
                header.glInternalFormat = KTXHeader.GL_COMPRESSED_SIGNED_RG11_EAC;
                header.glBaseInternalFormat = KTXHeader.GL_RG;
                bpp = 4;
                break;
                
            default:
                throw new IllegalStateException("Invalid texture format for KTX: " + tex.textureFormat);
        }
        
        // header + raw image data + mip map image sizes
        int imageSizeTotal = KTXHeader.SIZE + tex.imageBuffer.capacity() + header.numberOfMipmapLevels * 4;
        ByteBuffer bb = ByteBuffer.allocateDirect(imageSizeTotal);
        
        // write header
        header.write(DataOutputWriter.newWriter(bb));
        
        int mipMapWidth = header.pixelWidth;
        int mipMapHeight = header.pixelHeight;
        int mipMapOffset = 0;
        for (int i = 0; i < header.numberOfMipmapLevels; i++) {
            // write mip map size
            bb.putInt(mipMapWidth);
            
            // get mip map image data
            int mipMapSize = (mipMapWidth * mipMapHeight * bpp) / 8;
            ByteBuffer mipMapBuffer = ByteBufferUtils.getSlice(tex.imageBuffer, mipMapOffset, mipMapSize);

            // write image data
            bb.put(mipMapBuffer);
            
            // prepare next mip map
            mipMapWidth /= 2;
            mipMapHeight /= 2;
            mipMapOffset += mipMapSize;
        }

        // write file
        bb.rewind();

        setOutputFileName(tex.name);
        setOutputFileExtension("ktx");
        writeData(bb);
    }
    
    private class Texture2D {
        
        String name;
        Integer width;
        Integer height;
        Integer completeImageSize;
        Integer textureFormatOrd;
        TextureFormat textureFormat;
        Boolean mipMap;
        Boolean isReadable;
        Boolean readAllowed;
        Integer imageCount;
        Integer textureDimension;
        Integer lightmapFormat;
        Integer colorSpace;
        ByteBuffer imageBuffer;
        
        Texture2D(UnityObject obj) {
            name = obj.getValue("m_Name");
            width = obj.getValue("m_Width");
            height = obj.getValue("m_Height");
            completeImageSize = obj.getValue("m_CompleteImageSize");
            
            textureFormatOrd = obj.getValue("m_TextureFormat");
            textureFormat = TextureFormat.fromOrdinal(textureFormatOrd);

            mipMap = obj.getValue("m_MipMap");
            isReadable = obj.getValue("m_IsReadable");
            readAllowed = obj.getValue("m_ReadAllowed");
            imageCount = obj.getValue("m_ImageCount");
            textureDimension = obj.getValue("m_TextureDimension");
            lightmapFormat = obj.getValue("m_LightmapFormat");
            colorSpace = obj.getValue("m_ColorSpace");
            
            imageBuffer = obj.getValue("image data");
            imageBuffer.order(ByteOrder.LITTLE_ENDIAN);
            
            //L.log( Level.INFO, "texture name: {0} width: {1} height: {2} lightmapFormat: {3} textureFormat: {4} textureFormatOrd: {5} colorSpace: {6} mipMap: {7}", new Object[] { name, width, height, lightmapFormat, textureFormat, textureFormatOrd, colorSpace, mipMap } );
        }
    }
}
