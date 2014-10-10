/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.extract;

import info.ata4.io.DataInputReader;
import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.log.LogUtils;
import info.ata4.unity.engine.AudioClip;
import info.ata4.unity.engine.enums.AudioType;
import info.ata4.unity.rtti.ObjectData;
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
public class AudioClipHandler extends AbstractObjectExtractor {

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
    
    private final DataInputReader audioDataAux;
    
    public AudioClipHandler(DataInputReader audioDataAux) {
        super("AudioClip");
        this.audioDataAux = audioDataAux;
    }
    
    @Override
    public void process(ObjectData object) throws IOException {
        AudioClip audio = new AudioClip(object.getInstance());
        String name = audio.getName();
        
        ByteBuffer audioData = audio.getAudioData();
        
        // skip empty buffers
        if (ByteBufferUtils.isEmpty(audioData)) {
            L.log(Level.WARNING, "Audio clip {0} is empty", name);
            return;
        }

        // load audio buffer from external buffer if stream is set to 2
        if (audio.getStream() == 2) {
            L.log(Level.FINE, "Audio clip {0} uses external audio data",
                    name);
            
            // read offset integer from buffer
            audioData.order(ByteOrder.LITTLE_ENDIAN);
            audioData.rewind();
            int offset = audioData.getInt(0);

            // make sure the .resS is loaded
            if (audioDataAux == null) {
                L.log(Level.WARNING, "Audio clip {0} uses an external .resS file that doesn't exist!", name);
                return;
            }

            audioData = ByteBufferUtils.allocate(audioData.capacity());
            
            audioDataAux.position(offset);
            audioDataAux.readBuffer(audioData);
        }

        String ext = null;

        AudioType type = audio.getType();
        if (type != null) {
            ext = AUDIO_EXT.get(type);
        }

        // use .bin if the file extension cannot be determined
        if (ext == null) {
            L.log(Level.WARNING, "Audio clip {0} uses unknown audio type {1}",
                    new Object[]{name, type});
            ext = "bin";
        }
        
        files.add(new MutableFileHandle(name, ext, audioData));
    }

}
