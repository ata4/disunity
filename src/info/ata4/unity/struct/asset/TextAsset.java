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

//    +--TextAsset "Base" (3)
//       |--string "m_Name" (1)
//       |  +--Array "Array" (2)
//       |     |--SInt32 "size"
//       |     +--char "data"
//       |--string "m_Script" (1)
//       |  +--Array "Array" (2)
//       |     |--SInt32 "size"
//       |     +--char "data"
//       +--string "m_PathName" (1)
//          +--Array "Array" (2)
//             |--SInt32 "size"
//             +--char "data"

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TextAsset extends UnityObject {
    
    private static final Logger L = Logger.getLogger(TextAsset.class.getName());
    
    public byte[] script;
    public String pathName;
    
    public TextAsset(AssetFormat formatInfo) {
        super(formatInfo);
    }

    @Override
    public void readData() throws IOException {
        super.readData();
        
        script = in.readByteArray();
        L.log(Level.FINEST, "script = {0} bytes", script.length);
        
        pathName = in.readString();
        L.log(Level.FINEST, "pathName = {0}", pathName);
    }

}
