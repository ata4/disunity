/*
 ** 2013 July 21
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

//+--SubstanceArchive "Base" (2)
//   |--string "m_Name" (1)
//   |  +--Array "Array" (2)
//   |     |--SInt32 "size"
//   |     +--char "data"
//   +--vector "m_PackageData" (1)
//      +--Array "Array" (2)
//         |--SInt32 "size"
//         +--UInt8 "data"

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class SubstanceArchive extends UnityObject {
    
    private static final Logger L = Logger.getLogger(SubstanceArchive.class.getName());
    
    public byte[] packageData;

    public SubstanceArchive(AssetFormat formatInfo) {
        super(formatInfo);
    }

    @Override
    public void readData() throws IOException {
        super.readData();
        
        packageData = in.readByteArray();
        L.log(Level.FINEST, "packageData = {0} bytes", packageData.length);
    }
}
