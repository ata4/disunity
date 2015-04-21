/*
 ** 2014 December 26
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.engine;

import info.ata4.unity.engine.enums.AudioType;
import info.ata4.unity.rtti.FieldNode;
import java.nio.ByteBuffer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AudioClip extends UnityObject {

    public AudioClip(FieldNode object) {
        super(object);
    }
    
    public int getFormat() {
        return object.getSInt32("m_Format");
    }
    
    public AudioType getType() {
        return AudioType.fromOrdinal(object.getSInt32("m_Type"));
    }

    public Integer getStream() {
        return object.getSInt32("m_Stream");
    }
    
    public ByteBuffer getAudioData() {
        String fieldName;
        
        if (object.getType().version() > 2) {
            // vector m_AudioData
            fieldName = "m_AudioData";
        } else {
            // TypelessData audio data
            fieldName = "audio data";
        }
        
        return object.getChildArrayData(fieldName, ByteBuffer.class);
    }
    
}
