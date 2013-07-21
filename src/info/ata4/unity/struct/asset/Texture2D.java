/*
 ** 2013 June 19
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.struct.asset;

import info.ata4.unity.asset.AssetFormat;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

//    v6:
//    +--Texture2D "Base" (11)
//       |--string "m_Name" (1)
//       |  +--Array "Array" (2)
//       |     |--SInt32 "size"
//       |     +--char "data"
//       |--int "m_Width"
//       |--int "m_Height"
//       |--int "m_CompleteImageSize"
//       |--int "m_TextureFormat"
//       |--bool "m_MipMap"
//       |--bool "m_IsReadable"
//       |--int "m_ImageCount"
//       |--int "m_TextureDimension"
//       |--GLTextureSettings "m_TextureSettings" (4)
//       |  |--int "m_FilterMode"
//       |  |--int "m_Aniso"
//       |  |--float "m_MipBias"
//       |  +--int "m_WrapMode"
//       +--TypelessData "image data" (2)
//          |--SInt32 "size"
//          +--UInt8 "data"
//
//    v7:
//    +--Texture2D "Base" (13)
//       |--string "m_Name" (1)
//       |  +--Array "Array" (2)
//       |     |--SInt32 "size"
//       |     +--char "data"
//       |--int "m_Width"
//       |--int "m_Height"
//       |--int "m_CompleteImageSize"
//       |--int "m_TextureFormat"
//       |--bool "m_MipMap"
//       |--bool "m_IsReadable"
//       |--bool "m_ReadAllowed"
//       |--int "m_ImageCount"
//       |--int "m_TextureDimension"
//       |--GLTextureSettings "m_TextureSettings" (4)
//       |  |--int "m_FilterMode"
//       |  |--int "m_Aniso"
//       |  |--float "m_MipBias"
//       |  +--int "m_WrapMode"
//       |--int "m_LightmapFormat"
//       +--TypelessData "image data" (2)
//          |--SInt32 "size"
//          +--UInt8 "data"
//
//    v9:
//    +--Texture2D "Base" (14)
//       |--string "m_Name" (1)
//       |  +--Array "Array" (2)
//       |     |--SInt32 "size"
//       |     +--char "data"
//       |--int "m_Width"
//       |--int "m_Height"
//       |--int "m_CompleteImageSize"
//       |--int "m_TextureFormat"
//       |--bool "m_MipMap"
//       |--bool "m_IsReadable"
//       |--bool "m_ReadAllowed"
//       |--int "m_ImageCount"
//       |--int "m_TextureDimension"
//       |--GLTextureSettings "m_TextureSettings" (4)
//       |  |--int "m_FilterMode"
//       |  |--int "m_Aniso"
//       |  |--float "m_MipBias"
//       |  +--int "m_WrapMode"
//       |--int "m_LightmapFormat"
//       |--int "m_ColorSpace"
//       +--TypelessData "image data" (2)
//          |--SInt32 "size"
//          +--UInt8 "data"

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class Texture2D extends UnityObject {
    
    private static final Logger L = Logger.getLogger(Texture2D.class.getName());
    
    public int width;
    public int height;
    public int completeImageSize;
    public int textureFormat;
    public boolean mipMap;
    public boolean isReadable;
    public boolean readAllowed;
    public int imageCount;
    public int textureDimension;
    public GLTextureSettings textureSettings;
    public int lightmapFormat;
    public int colorSpace;
    public byte[] imageData;
    
    public Texture2D(AssetFormat formatInfo) {
        super(formatInfo);
        textureSettings = new GLTextureSettings(formatInfo);
    }
    
    @Override
    public void readData() throws IOException {
        super.readData();
        
        width = readInt();
        L.log(Level.FINEST, "width = {0}", width);
        
        height = readInt();
        L.log(Level.FINEST, "height = {0}", height);
        
        completeImageSize = readInt();
        L.log(Level.FINEST, "completeImageSize = {0}", completeImageSize);
        
        textureFormat = readInt();
        L.log(Level.FINEST, "textureFormat = {0}", textureFormat);
        
        mipMap = readBoolean();
        L.log(Level.FINEST, "mipMap = {0}", mipMap);
        
        isReadable = readBoolean();
        L.log(Level.FINEST, "isReadable = {0}", isReadable);
        
        // TODO: validate
        if (formatInfo.getFormat() >= 7) {
            readAllowed = readBoolean();
            L.log(Level.FINEST, "readAllowed = {0}", readAllowed);
        }
        
        imageCount = readInt();
        L.log(Level.FINEST, "imageCount = {0}", imageCount);
        textureDimension = readInt();
        L.log(Level.FINEST, "textureDimension = {0}", textureDimension);

        readObject(textureSettings);
        
        // TODO: validate
        if (formatInfo.getFormat() >= 7) {
            lightmapFormat = readInt();
            L.log(Level.FINEST, "lightmapFormat = {0}", lightmapFormat);
        }
        
        if (formatInfo.getFormat() >= 9) {
            colorSpace = readInt();
            L.log(Level.FINEST, "colorSpace = {0}", colorSpace);
        }
        
        imageData = readByteArray();
        L.log(Level.FINEST, "imageData = {0} bytes", imageData.length);
    }
}
