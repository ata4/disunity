/*
 ** 2013 June 27
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.struct.asset;

import info.ata4.unity.asset.AssetFormat;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

//    v6:
//    +--AudioClip "Base" (6)
//       |--string "m_Name" (1)
//       |  +--Array "Array" (2)
//       |     |--SInt32 "size"
//       |     +--char "data"
//       |--SInt32 "m_Format"
//       |--SInt32 "m_Type"
//       |--bool "m_3D"
//       |--vector "m_AudioData" (1)
//       |  +--Array "Array" (2)
//       |     |--SInt32 "size"
//       |     +--UInt8 "data"
//       +--bool "m_DecompressOnLoad"
//
//
//    v7:
//    +--AudioClip "Base" (7)
//       |--string "m_Name" (1)
//       |  +--Array "Array" (2)
//       |     |--SInt32 "size"
//       |     +--char "data"
//       |--SInt32 "m_Format"
//       |--SInt32 "m_Type"
//       |--bool "m_3D"
//       |--bool "m_UseHardware"
//       |--int "m_Stream"
//       +--vector "m_AudioData" (1)
//          +--Array "Array" (2)
//             |--SInt32 "size"
//             +--UInt8 "data"

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AudioClip extends UnityObject {
    
    private static final Logger L = Logger.getLogger(AudioClip.class.getName());
    
    public int format;
    public int type;
    public boolean use3D;
    public boolean useHardware;
    public boolean decompressOnLoad;
    public int stream;
    public byte[] audioData;
    
    public AudioClip(AssetFormat formatInfo) {
        super(formatInfo);
    }

    @Override
    public void readData() throws IOException {
        super.readData();
        
        format = in.readInt();
        L.log(Level.FINEST, "format = {0}", format);
        
        type = in.readInt();
        L.log(Level.FINEST, "type = {0}", type);
        
        use3D = in.readBoolean();
        L.log(Level.FINEST, "use3D = {0}", use3D);
        
        if (formatInfo.getFormat() >= 7) {
            useHardware = in.readBoolean();
            L.log(Level.FINEST, "useHardware = {0}", useHardware);

            stream = in.readInt();
            L.log(Level.FINEST, "stream = {0}", stream);
        }
        
        audioData = in.readByteArray();
        L.log(Level.FINEST, "audioData = {0}", audioData);
        
        if (formatInfo.getFormat() < 7) {
            decompressOnLoad = in.readBoolean();
            L.log(Level.FINEST, "decompressOnLoad = {0}", stream);
        }
    }
}
