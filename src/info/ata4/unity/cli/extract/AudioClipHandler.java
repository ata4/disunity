/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.extract;

import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.log.LogUtils;
import info.ata4.unity.enums.AudioType;
import info.ata4.unity.serdes.UnityObject;
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
public class AudioClipHandler extends AssetExtractHandler {
    
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
    public void extract(UnityObject obj) throws IOException {
        AudioClip audio = new AudioClip(obj);

        // load audio buffer from external buffer if stream is set to 2
        int stream = obj.getValue("m_Stream");
        if (stream == 2) {
            L.log(Level.FINE, "Audio clip {0} uses external audio data",
                    audio.name);
            int size = audio.audioBuffer.capacity();
            
            // read offset integer from buffer
            audio.audioBuffer.order(ByteOrder.LITTLE_ENDIAN);
            int offset = audio.audioBuffer.getInt();
            audio.audioBuffer.rewind();
            
            ByteBuffer audioBufferAux = getAssetFile().getAudioBuffer();
            
            // make sure the .resS is loaded
            if (audioBufferAux == null) {
                L.log(Level.WARNING, "Audio clip {0} uses an external .resS file that doesn't exist!",
                        audio.name);
                return;
            }
            
            audio.audioBuffer.put(ByteBufferUtils.getSlice(audioBufferAux, offset, size));
            audio.audioBuffer.rewind();
        }
        
        String ext = "bin";
        
        if (audio.type == null) {
            L.log(Level.WARNING, "Audio clip {0} uses unknown audio type {1}",
                    new Object[]{audio.name, audio.type});
        } else {
            if (AUDIO_EXT.containsKey(audio.type)) {
                ext = AUDIO_EXT.get(audio.type);
            } else {
                L.log(Level.WARNING, "Audio clip {0} uses unknown audio type {1}",
                        new Object[]{audio.name, audio.type});
            }
        }
        
        setOutputFileName(audio.name);
        setOutputFileExtension(ext);
        writeData(audio.audioBuffer);
    }
    
    private class AudioClip {
        
        final String name;
        final ByteBuffer audioBuffer;
        final Integer stream;
        final AudioType type;
        
        AudioClip(UnityObject obj) {
            name = obj.getValue("m_Name");
            audioBuffer = obj.getValue("m_AudioData");
            stream = obj.getValue("m_Stream");
            Integer typeInt = obj.getValue("m_Type");
            type = AudioType.fromOrdinal(typeInt);
        }
    }
}
