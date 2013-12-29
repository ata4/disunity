/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.extract.handler;

import info.ata4.unity.asset.struct.AssetObjectPath;
import info.ata4.unity.cli.extract.AssetExtractHandler;
import info.ata4.unity.enums.AudioType;
import info.ata4.unity.serdes.UnityBuffer;
import info.ata4.unity.serdes.UnityObject;
import info.ata4.util.io.ByteBufferUtils;
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
    
    private static final Logger L = Logger.getLogger(AudioClipHandler.class.getName());
    
    private static final Map<AudioType, String> AUDIO_EXT;
    
    static {
        Map<AudioType, String> extMap = new EnumMap<>(AudioType.class);
        extMap.put(AudioType.OGGVORBIS, "ogg");
        extMap.put(AudioType.WAV, "wav");
        extMap.put(AudioType.GCADPCM, "adp");
        extMap.put(AudioType.MPEG, "mp3");
        extMap.put(AudioType.AIFF, "aif");
        extMap.put(AudioType.XM, "xm");
        extMap.put(AudioType.XMA, "xma");
        extMap.put(AudioType.S3M, "s3m");
        extMap.put(AudioType.MOD, "mod");
        extMap.put(AudioType.AUDIOQUEUE, "caf");
        AUDIO_EXT = Collections.unmodifiableMap(extMap);
    }
    
    private ByteBuffer audioBufferAux;
    
    public ByteBuffer getAudioBuffer() {
        return audioBufferAux;
    }

    public void setAudioBuffer(ByteBuffer audioBufferAux) {
        this.audioBufferAux = audioBufferAux;
    }

    @Override
    public void extract(AssetObjectPath path, UnityObject obj) throws IOException {
        String name = obj.getValue("m_Name");
        
        UnityBuffer audioData = obj.getValue("m_AudioData");
        ByteBuffer audioBuffer = audioData.getBuffer();
        audioBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // load audio buffer from external buffer if stream is set to 2
        int stream = obj.getValue("m_Stream");
        if (stream == 2) {
            L.log(Level.FINE, "{0} uses external audio buffer", name);
            
            int offset = audioBuffer.getInt();
            int size = audioBuffer.capacity();
            audioBuffer.rewind();
            
            audioBuffer.put(ByteBufferUtils.getSlice(audioBufferAux, offset, size));
            audioBuffer.rewind();
        }

        int typeInt = obj.getValue("m_Type");
        AudioType type = AudioType.fromOrdinal(typeInt);
        
        if (type == null) {
            L.log(Level.WARNING, "Audio clip {0} uses unknown audio type {1}",
                    new Object[]{name, type});
            return;
        }
        
        String ext = AUDIO_EXT.get(type);
        
        if (ext == null) {
            L.log(Level.WARNING, "Audio clip {0} uses unsupported audio type {1}",
                    new Object[]{name, type});
            return;
        }
        
        setFileExtension(ext);
        writeFile(audioBuffer, path.pathID, name);
    }
}
