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
import info.ata4.unity.rtti.FieldNode;
import java.nio.ByteBuffer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class Texture2D extends UnityObject {

    public Texture2D(FieldNode node) {
        super(node);
    }

    public Integer getWidth() {
        return node.getChildValue("m_Width");
    }

    public Integer getHeight() {
        return node.getChildValue("m_Height");
    }

    public Integer getCompleteImageSize() {
        return node.getChildValue("m_CompleteImageSize");
    }

    public TextureFormat getTextureFormat() {
        return TextureFormat.fromOrdinal(node.<Integer>getChildValue("m_TextureFormat"));
    }

    public Boolean getMipMap() {
        return node.getChildValue("m_MipMap");
    }

    public Boolean getIsReadable() {
        return node.getChildValue("m_IsReadable");
    }

    public Boolean getReadAllowed() {
        return node.getChildValue("m_ReadAllowed");
    }

    public Integer getImageCount() {
        return node.getChildValue("m_ImageCount");
    }

    public Integer getTextureDimension() {
        return node.getChildValue("m_TextureDimension");
    }

    public Integer getLightmapFormat() {
        return node.getChildValue("m_LightmapFormat");
    }

    public Integer getColorSpace() {
        return node.getChildValue("m_ColorSpace");
    }

    public ByteBuffer getImageData() {
        return node.getChildValue("image data");
    }
    
}
