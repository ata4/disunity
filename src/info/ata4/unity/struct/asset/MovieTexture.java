/*
 ** 2013 July 01
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

//    +--MovieTexture "Base" (4)
//       |--string "m_Name" (1)
//       |  +--Array "Array" (2)
//       |     |--SInt32 "size"
//       |     +--char "data"
//       |--bool "m_Loop"
//       |--PPtr<AudioClip> "m_AudioClip" (2)
//       |  |--SInt32 "m_FileID"
//       |  +--SInt32 "m_PathID"
//       +--vector "m_MovieData" (1)
//          +--Array "Array" (2)
//             |--SInt32 "size"
//             +--UInt8 "data"

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class MovieTexture extends UnityObject {
    
    private static final Logger L = Logger.getLogger(MovieTexture.class.getName());

    public boolean loop;
    public PPtr audioClip;
    public byte[] moveData;
    
    public MovieTexture(AssetFormat formatInfo) {
        super(formatInfo);
        audioClip = new PPtr(formatInfo);
    }
    
    @Override
    public void readData() throws IOException {
        super.readData();
        
        loop = in.readBoolean();
        L.log(Level.FINEST, "loop = {0} ", loop);
        
        in.readStruct(audioClip);
        
        moveData = in.readByteArray();
        L.log(Level.FINEST, "moveData = {0} bytes", moveData.length);
    }
}
