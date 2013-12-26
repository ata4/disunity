/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.extract.handler;

import info.ata4.unity.asset.struct.AssetObjectPath;
import info.ata4.unity.cli.extract.AssetExtractHandler;
import info.ata4.unity.cli.extract.handler.struct.DDSHeader;
import info.ata4.unity.cli.extract.handler.struct.DDSPixelFormat;
import info.ata4.unity.cli.extract.handler.struct.TGAHeader;
import info.ata4.unity.enums.TextureFormat;
import static info.ata4.unity.enums.TextureFormat.*;
import info.ata4.unity.serdes.UnityBuffer;
import info.ata4.unity.serdes.UnityObject;
import info.ata4.util.io.DataOutputWriter;
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
    
    private static final Logger L = Logger.getLogger(Texture2DHandler.class.getName());
    
    private TextureFormat tf;
    private AssetObjectPath path;
    private UnityObject obj;
    private String name;
    private ByteBuffer imageBuffer;
    
    @Override
    public void extract(AssetObjectPath path, UnityObject obj) throws IOException {
        this.path = path;
        this.obj = obj;
        
        name = obj.getValue("m_Name");
        UnityBuffer imageData = obj.getValue("image data");
        imageBuffer = imageData.getBuffer();

        // some textures don't have any image data, not sure why...
        if (imageBuffer.capacity() == 0) {
            L.log(Level.WARNING, "Texture2D {0} is empty", name);
            return;
        }
        
        // get texture format
        int textureFormat = obj.getValue("m_TextureFormat");
        tf = TextureFormat.fromOrdinal(textureFormat);
        if (tf == null) {
            L.log(Level.WARNING, "Texture2D {0} has unknown texture format {1}",
                    new Object[]{name, textureFormat});
            return;
        }
        
        // choose a fitting container format
        switch (tf) {
            case Alpha8:
            case RGB24:
            case RGBA32:
            case BGRA32:
            case ARGB32:
                extractTGA();
                break;
            
            case PVRTC_RGB2:
            case PVRTC_RGBA2:
            case PVRTC_RGB4:
            case PVRTC_RGBA4:
                extractPVR();
                break;

            case ATC_RGB4:
            case ATC_RGBA8:
                extractATC();
                break;

            case ETC_RGB4:
                extractPKM();
                break;
                
            case ARGB4444:
            case RGB565:
            case DXT1:
            case DXT5:
                extractDDS();
                break;
                
            default:
                L.log(Level.WARNING, "Texture2D {0} has unsupported texture format {1}",
                        new Object[] {name, tf});
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
        int width = obj.getValue("m_Width");
        int height = obj.getValue("m_Height");
        
        DDSHeader dds = new DDSHeader();
        dds.dwWidth = width;
        dds.dwHeight = height;

        switch (tf) {
            case Alpha8:
                dds.ddspf.dwFlags = DDSPixelFormat.DDPF_ALPHA;
                dds.ddspf.dwABitMask = 0xff;
                dds.ddspf.dwRGBBitCount = 8;
                break;
                
            case RGB24:
                dds.ddspf.dwFlags = DDSPixelFormat.DDPF_RGB;
                dds.ddspf.dwRBitMask = 0xff0000;
                dds.ddspf.dwGBitMask = 0x00ff00;
                dds.ddspf.dwBBitMask = 0x0000ff;
                dds.ddspf.dwRGBBitCount = 24;
                break;
                
            case RGBA32:
                dds.ddspf.dwFlags = DDSPixelFormat.DDPF_RGBA;
                dds.ddspf.dwRBitMask = 0x000000ff;
                dds.ddspf.dwGBitMask = 0x0000ff00;
                dds.ddspf.dwBBitMask = 0x00ff0000;
                dds.ddspf.dwABitMask = 0xff000000;
                dds.ddspf.dwRGBBitCount = 32;
                break;
                
            case BGRA32:
                dds.ddspf.dwFlags = DDSPixelFormat.DDPF_RGBA;
                dds.ddspf.dwRBitMask = 0x00ff0000;
                dds.ddspf.dwGBitMask = 0x0000ff00;
                dds.ddspf.dwBBitMask = 0x000000ff;
                dds.ddspf.dwABitMask = 0xff000000;
                dds.ddspf.dwRGBBitCount = 32;
                break;
                
            case ARGB32:
                dds.ddspf.dwFlags = DDSPixelFormat.DDPF_RGBA;
                dds.ddspf.dwRBitMask = 0x0000ff00;
                dds.ddspf.dwGBitMask = 0x00ff0000;
                dds.ddspf.dwBBitMask = 0xff000000;
                dds.ddspf.dwABitMask = 0x000000ff;
                dds.ddspf.dwRGBBitCount = 32;
                break;
                    
            case ARGB4444:
                dds.ddspf.dwFlags = DDSPixelFormat.DDPF_RGBA;
                dds.ddspf.dwRBitMask = 0x0f00;
                dds.ddspf.dwGBitMask = 0x00f0;
                dds.ddspf.dwBBitMask = 0x000f;
                dds.ddspf.dwABitMask = 0xf000;
                dds.ddspf.dwRGBBitCount = 16;
                break;
                
            case RGB565:
                dds.ddspf.dwFlags = DDSPixelFormat.DDPF_RGB;
                dds.ddspf.dwRBitMask = 0xf800;
                dds.ddspf.dwGBitMask = 0x07e0;
                dds.ddspf.dwBBitMask = 0x001f;
                dds.ddspf.dwRGBBitCount = 16;
                break;
            
            case DXT1:
                dds.ddspf.dwFourCC = DDSPixelFormat.PF_DXT1;
                break;
            
            case DXT5:
                dds.ddspf.dwFourCC = DDSPixelFormat.PF_DXT5; 
                break;
                
            default:
                throw new IllegalStateException("Invalid texture format for DDS: " + tf);
        }

        // set mip map flags if required
        boolean mipMap = obj.getValue("m_MipMap");
        if (mipMap) {
            dds.dwFlags |= DDSHeader.DDS_HEADER_FLAGS_MIPMAP;
            dds.dwCaps |= DDSHeader.DDS_SURFACE_FLAGS_MIPMAP;
            dds.dwMipMapCount = getMipMapCount(dds.dwWidth, dds.dwHeight);
        }
        
        // set and calculate linear size
        dds.dwFlags |= DDSHeader.DDS_HEADER_FLAGS_LINEARSIZE;
        if (dds.ddspf.dwFourCC != 0) {
            dds.dwPitchOrLinearSize = dds.dwWidth * dds.dwHeight;
            
            if (tf == TextureFormat.DXT1) {
                dds.dwPitchOrLinearSize /= 2;
            }
            
            dds.ddspf.dwFlags |= DDSPixelFormat.DDPF_FOURCC;
        } else {
            dds.dwPitchOrLinearSize = (width * height * dds.ddspf.dwRGBBitCount) / 8;
        }
        
        // TODO: convert AG to RGB normal maps? (colorSpace = 0)
        
        ByteBuffer bbTex = ByteBuffer.allocateDirect(128 + imageBuffer.capacity());
        bbTex.order(ByteOrder.LITTLE_ENDIAN);
        
        // write header
        DataOutputWriter out = new DataOutputWriter(bbTex);
        dds.write(out);
        
        // write data
        bbTex.put(imageBuffer);
        
        bbTex.rewind();
        
        setFileExtension("dds");
        writeFile(bbTex, path.pathID, name);
    }

    private void extractPVR() {
        // TODO
        throw new UnsupportedOperationException("PVR not yet implemented");
    }
    
    private void extractATC() {
        // TODO
        throw new UnsupportedOperationException("ATC not yet implemented");
    }

    private void extractPKM() throws IOException {
        int width = obj.getValue("m_Width");
        int height = obj.getValue("m_Height");

        // texWidth and texHeight are width and height rounded up to multiple of 4.
        int texWidth = ((width - 1) | 3) + 1;
        int texHeight = ((height - 1) | 3) + 1;

        ByteBuffer res = ByteBuffer.allocateDirect(16 + imageBuffer.capacity());
        res.order(ByteOrder.BIG_ENDIAN);

        res.putLong(0x504b4d2031300000L); // PKM 10\0\0

        res.putShort((short) texWidth);
        res.putShort((short) texHeight);
        res.putShort((short) width);
        res.putShort((short) height);

        res.put(imageBuffer);

        res.rewind();

        setFileExtension("pkm");
        writeFile(res, path.pathID, name);
    }

    private void extractTGA() throws IOException {
        boolean convert = false;
        
        TGAHeader tgah = new TGAHeader();
        tgah.imageWidth = obj.getValue("m_Width");
        tgah.imageHeight = obj.getValue("m_Height");
        
        switch (tf) {
            case Alpha8:
                tgah.imageType = 3;
                tgah.pixelDepth = 8;
                break;
                
            case RGB24:
                tgah.imageType = 2;
                tgah.pixelDepth = 24;
                break;
                
            case RGBA32:
                tgah.imageType = 2;
                tgah.pixelDepth = 32;
                break;
                
            case ARGB32:
            case BGRA32:
                tgah.imageType = 2;
                tgah.pixelDepth = 32;
                convert = true;
                break;
                
            default:
                throw new IllegalStateException("Invalid texture format for TGA: " + tf);
        }
        
        // convert non-native color formats
        if (convert) {
            for (int i = 0; i < imageBuffer.limit() / 4; i++) {
                imageBuffer.mark();
                int pixelOld = imageBuffer.getInt();
                int pixelNew;

                if (tf == ARGB32) {
                    // rotate left: ARGB -> RGBA
                    pixelNew = Integer.rotateLeft(pixelOld, 8);
                } else {
                    // swap B and R
                    pixelNew = pixelOld & 0x00ff00ff; // get G & A
                    pixelNew |= (pixelOld & 0xff000000) >>> 16; // add B
                    pixelNew |= (pixelOld & 0x0000ff00) << 16; // add R
                }

                imageBuffer.reset();
                imageBuffer.putInt(pixelNew);
            }
            
            imageBuffer.rewind();
        }

        boolean mipMap = obj.getValue("m_MipMap");
        int mipMapCount = getMipMapCount(tgah.imageWidth, tgah.imageHeight);
        
        if (!mipMap) {
            mipMapCount = 1;
        }
        
        for (int i = 0; i < mipMapCount; i++) {
            int imageSize = tgah.imageWidth * tgah.imageHeight * tgah.pixelDepth / 8;
 
            ByteBuffer bb = ByteBuffer.allocateDirect(imageSize + 18);
            bb.order(ByteOrder.LITTLE_ENDIAN);

            // write TGA header
            DataOutputWriter out = new DataOutputWriter(bb);
            tgah.write(out);

            // write image data
            imageBuffer.limit(imageBuffer.position() + imageSize);
            bb.put(imageBuffer);
            
            assert !bb.hasRemaining();
            
            // write file
            bb.rewind();
            
            String fileName = name;
            if (mipMap) {
                fileName += "_mip_" + i;
            }

            setFileExtension("tga");
            writeFile(bb, path.pathID, fileName);
            
            // prepare for the next mip map
            tgah.imageWidth /= 2;
            tgah.imageHeight /= 2;
        }
        
        assert !imageBuffer.hasRemaining();
    }
}
