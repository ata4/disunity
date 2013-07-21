/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.extract.handler;

import info.ata4.unity.enums.AudioType;
import info.ata4.unity.struct.asset.AudioClip;
import info.ata4.util.io.ByteBufferInput;
import info.ata4.util.io.DataInputReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AudioClipHandler extends ExtractHandler {
    
    private static final Logger L = Logger.getLogger(RawHandler.class.getName());
    
    private static final Map<AudioType, String> AUDIO_EXT;
    
    static {
        Map<AudioType, String> audioTypes = new EnumMap<>(AudioType.class);
        audioTypes.put(AudioType.OGGVORBIS, "ogg");
        audioTypes.put(AudioType.WAV, "wav");
        audioTypes.put(AudioType.GCADPCM, "adp");
        audioTypes.put(AudioType.MPEG, "mp3");
        audioTypes.put(AudioType.AIFF, "aif");
        audioTypes.put(AudioType.XM, "xm");
        audioTypes.put(AudioType.XMA, "xma");
        audioTypes.put(AudioType.S3M, "s3m");
        audioTypes.put(AudioType.MOD, "mod");
        audioTypes.put(AudioType.AUDIOQUEUE, "caf");
        AUDIO_EXT = Collections.unmodifiableMap(audioTypes);
    }
    
    @Override
    public String getClassName() {
        return "AudioClip";
    }
    
    @Override
    public void extract(ByteBuffer bb, int id) throws IOException {
        DataInputReader in = new DataInputReader(new ByteBufferInput(bb));
        
        AudioClip audio = new AudioClip(getAssetFormat());
        audio.read(in);

        if (audio.audioData.length == 0) {
            L.log(Level.WARNING, "Audio clip {0} empty", audio.name);
            return;
        }
        
        AudioType type = AudioType.fromOrdinal(audio.type);
        
        if (type == null) {
            L.log(Level.WARNING, "Audio clip {0} uses unknown audio type {1}",
                    new Object[]{audio.name, audio.type});
            return;
        }
        
        String ext = AUDIO_EXT.get(type);
        
        if (ext == null) {
            L.log(Level.WARNING, "Audio clip {0} uses unsupported audio type {1}",
                    new Object[]{audio.name, type});
            return;
        }
        
        extractToFile(audio.audioData, id, audio.name, ext);
    }
}
