/*
 ** 2013 June 20
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.enums;

/**
 * Converted TextureFormat enum from the Unity SDK.
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
    UNUSED20,
    UNUSED21,
    UNUSED22,
    UNUSED23,
    UNUSED24,
    UNUSED25,
    UNUSED26,
    UNUSED27,
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
    ATF_RGB_JPG;
    
    private static final TextureFormat[] VALUES = values();
    
    public static TextureFormat fromOrdinal(int value) {
        try {
            return VALUES[value - 1];
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }
}
