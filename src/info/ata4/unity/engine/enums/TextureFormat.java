/*
 ** 2013 June 20
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.engine.enums;

/**
 * Converted TextureFormat and TextureImporterFormat enums from the Unity SDK.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public enum TextureFormat {

    Alpha8,
    ARGB4444,
    RGB24,
    RGBA32,
    ARGB32,
    UNUSED06,
    RGB565,
    UNUSED08,
    UNUSED09,
    DXT1,
    UNUSED11,
    DXT5,
    RGBA4444,
    UNUSED14,
    UNUSED15,
    UNUSED16,
    UNUSED17,
    UNUSED18,
    UNUSED19,
    WiiI4,
    WiiI8,
    WiiIA4,
    WiiIA8,
    WiiRGB565,
    WiiRGB5A3,
    WiiRGBA8,
    WiiCMPR,
    UNUSED28,
    UNUSED29,
    PVRTC_RGB2,
    PVRTC_RGBA2,
    PVRTC_RGB4,
    PVRTC_RGBA4,
    ETC_RGB4,
    ATC_RGB4,
    ATC_RGBA8,
    BGRA32,
    ATF_RGB_DXT1,
    ATF_RGBA_JPG,
    ATF_RGB_JPG,
    EAC_R,
    EAC_R_SIGNED,
    EAC_RG,
    EAC_RG_SIGNED,
    ETC2_RGB4,
    ETC2_RGB4_PUNCHTHROUGH_ALPHA,
    ETC2_RGBA8,
    ASTC_RGB_4x4,
    ASTC_RGB_5x5,
    ASTC_RGB_6x6,
    ASTC_RGB_8x8,
    ASTC_RGB_10x10,
    ASTC_RGB_12x12,
    ASTC_RGBA_4x4,
    ASTC_RGBA_5x5,
    ASTC_RGBA_6x6,
    ASTC_RGBA_8x8,
    ASTC_RGBA_10x10,
    ASTC_RGBA_12x12;
    
    private static final TextureFormat[] VALUES = values();
    
    public static TextureFormat fromOrdinal(int value) {
        try {
            return VALUES[value - 1];
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }
}
