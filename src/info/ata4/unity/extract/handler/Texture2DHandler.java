/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.extract.handler;

import info.ata4.unity.enums.TextureFormat;
import static info.ata4.unity.enums.TextureFormat.ATC_RGB4;
import static info.ata4.unity.enums.TextureFormat.ATC_RGBA8;
import static info.ata4.unity.enums.TextureFormat.PVRTC_RGB2;
import static info.ata4.unity.enums.TextureFormat.PVRTC_RGB4;
import static info.ata4.unity.enums.TextureFormat.PVRTC_RGBA2;
import static info.ata4.unity.enums.TextureFormat.PVRTC_RGBA4;
import info.ata4.unity.serdes.UnityArray;
import info.ata4.unity.serdes.UnityObject;
import info.ata4.unity.struct.ObjectPath;
import info.ata4.unity.struct.dds.DDSHeader;
import info.ata4.unity.struct.dds.DDSPixelFormat;
import info.ata4.util.io.ByteBufferOutput;
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
public class Texture2DHandler extends ExtractHandler {
    
    private static final Logger L = Logger.getLogger(Texture2DHandler.class.getName());
    
    private TextureFormat tf;
    private ObjectPath path;
    private UnityObject obj;
    private String name;
    private ByteBuffer imageBuffer;
    
    @Override
    public String getClassName() {
        return "Texture2D";
    }
    
    @Override
    public void extract(ObjectPath path, UnityObject obj) throws IOException {
        this.path = path;
        this.obj = obj;
        
        name = obj.getValue("m_Name");
        UnityArray imageData = obj.getValue("image data");
        imageBuffer = imageData.getRaw();

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
                
            default:
                extractDDS();
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
                L.log(Level.WARNING, "Texture2D {0} has unsupported texture format {1}",
                        new Object[] {name, tf});
                return;
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
        DataOutputWriter out = new DataOutputWriter(new ByteBufferOutput(bbTex));
        dds.write(out);
        
        // write data
        bbTex.put(imageBuffer);
        
        bbTex.rewind();
        
        writeFile(bbTex, path.pathID, name, "dds");
    }

    private void extractPVR() {
        // TODO
        throw new UnsupportedOperationException("PVR not yet implemented");
    }
    
    private void extractATC() {
        // TODO
        throw new UnsupportedOperationException("ATC not yet implemented");
    }
}
