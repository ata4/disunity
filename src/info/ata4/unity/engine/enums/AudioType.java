/*
 ** 2013 June 27
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.engine.enums;

/**
 * Converted AudioType enum from the Unity SDK.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public enum AudioType {
    
    UNKNOWN,
    ACC,
    AIFF,
    UNUSED04,
    UNUSED05,
    UNUSED06,
    UNUSED07,
    UNUSED08,
    GCADPCM,
    IT,
    UNUSED11,
    MOD,
    MPEG,
    OGGVORBIS,
    UNUSED15,
    UNUSED16,
    S3M,
    UNUSED18,
    UNUSED19,
    WAV,
    XM,
    XMA,
    UNUSED23,
    AUDIOQUEUE;
    
    private static final AudioType[] VALUES = values();
    
    public static AudioType fromOrdinal(int value) {
        try {
            return VALUES[value - 1];
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }
}
