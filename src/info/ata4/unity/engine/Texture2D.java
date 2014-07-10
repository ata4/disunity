/*
 ** 2014 Juli 10
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.engine;

import info.ata4.unity.engine.enums.TextureFormat;
import info.ata4.unity.serdes.UnityObject;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class Texture2D {
    
    public String name;
    public Integer width;
    public Integer height;
    public Integer completeImageSize;
    public Integer textureFormatOrd;
    public TextureFormat textureFormat;
    public Boolean mipMap;
    public Boolean isReadable;
    public Boolean readAllowed;
    public Integer imageCount;
    public Integer textureDimension;
    public Integer lightmapFormat;
    public Integer colorSpace;
    public ByteBuffer imageBuffer;

    public Texture2D(UnityObject obj) {
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
    }
    
}
