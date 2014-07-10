/*
 ** 2014 July 10
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.engine;

import info.ata4.unity.engine.enums.AudioType;
import info.ata4.unity.serdes.UnityObject;
import java.nio.ByteBuffer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AudioClip {
    
    public final String name;
    public final ByteBuffer audioBuffer;
    public final Integer stream;
    public final AudioType type;

    public AudioClip(UnityObject obj) {
        name = obj.getValue("m_Name");
        audioBuffer = obj.getValue("m_AudioData");
        stream = obj.getValue("m_Stream");
        Integer typeInt = obj.getValue("m_Type");
        type = AudioType.fromOrdinal(typeInt);
    }
    
}
