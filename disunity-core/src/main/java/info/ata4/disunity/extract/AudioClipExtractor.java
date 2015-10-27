/*
 ** 2014 December 26
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.extract;

import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.log.LogUtils;
import info.ata4.unity.engine.AudioClip;
import info.ata4.unity.engine.enums.AudioType;
import info.ata4.unity.rtti.ObjectData;
import info.ata4.unity.util.UnityClass;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AudioClipExtractor extends AbstractAssetExtractor {
    
    private static final Logger L = LogUtils.getLogger();
    
    private static final Map<AudioType, String> AUDIO_EXT;

    static {
        Map<AudioType, String> extMap = new EnumMap<>(AudioType.class);
        extMap.put(AudioType.ACC, "aif");
        extMap.put(AudioType.AIFF, "aif");
        extMap.put(AudioType.AUDIOQUEUE, "caf");
        extMap.put(AudioType.GCADPCM, "adp");
        extMap.put(AudioType.MOD, "mod");
        extMap.put(AudioType.MPEG, "mp3");
        extMap.put(AudioType.OGGVORBIS, "ogg");
        extMap.put(AudioType.S3M, "s3m");
        extMap.put(AudioType.WAV, "wav");
        extMap.put(AudioType.XM, "xm");
        extMap.put(AudioType.XMA, "xma");
        AUDIO_EXT = Collections.unmodifiableMap(extMap);
    }

    @Override
    public UnityClass getUnityClass() {
        return new UnityClass("AudioClip");
    }
    
        
    @Override
    public void extract(ObjectData objectData) throws IOException {
        AudioClip audio = new AudioClip(objectData.instance());
        String name = audio.getName();
        
        if (objectData.instance().getChild("m_Resource") != null) {
            L.log(Level.WARNING, "Audio clip {0} has external resource, which is not supported by disunity", audio.getName());
            return;
        }
        
        ByteBuffer audioData = audio.getAudioData();
        
        // skip empty buffers
        if (ByteBufferUtils.isEmpty(audioData)) {
            L.log(Level.WARNING, "Audio clip {0} is empty", name);
            return;
        }
        
        AudioType type = audio.getType();
        String ext = null;
        if (type != null) {
            ext = AUDIO_EXT.get(type);
        }

        // use .bin if the file extension cannot be determined
        if (ext == null) {
            L.log(Level.WARNING, "Audio clip {0} uses unknown audio type {1}",
                    new Object[]{name, type});
            ext = "bin";
        }
        
        writeFile(name, ext, audioData);
    }
}
