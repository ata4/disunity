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
import info.ata4.unity.rtti.FieldNode;
import java.nio.ByteBuffer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AudioClip extends UnityObject {

    public AudioClip(FieldNode node) {
        super(node);
    }
    
    public ByteBuffer getAudioData() {
        return node.getChildArray("m_AudioData");
    }
    
    public Integer getStream() {
        return node.getChildValue("m_Stream");
    }
    
    public AudioType getType() {
        return AudioType.fromOrdinal(node.<Integer>getChildValue("m_Type"));
    }
    
}
