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

import info.ata4.util.io.ByteBufferInput;
import info.ata4.util.io.DataInputReader;
import info.ata4.util.io.KMPMatch;
import java.io.DataInput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;

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
public class SubstanceArchiveHandler extends ExtractHandler {
    
    private static final Logger L = Logger.getLogger(RawHandler.class.getName());
    
    private static final byte[] JPEG_START = DatatypeConverter.parseHexBinary("FFD8FFE0");
    private static final byte[] JPEG_END = DatatypeConverter.parseHexBinary("FFD9");

    @Override
    public String getClassName() {
        return "SubstanceArchive";
    }

    @Override
    public void extract(ByteBuffer bb, int id) throws IOException {
        String assetName = getAssetName(bb);
        
        if (assetName == null) {
            return;
        }
        
        DataInput di1 = new ByteBufferInput(bb);
        DataInputReader di2 = new DataInputReader(di1);
        
        di1.skipBytes(4);
        
        String signature = di2.readStringFixed(4);
        
        if (!signature.equals("SBAM")) {
            L.log(Level.WARNING, "Unexpected signature");
        }
        
        // the SBAM format is largely unknown to me, but it seems to contain
        // concatenated JPEG files, so simply search for its signature bytes
        int files = 1;
        while (true) {
            int start = KMPMatch.indexOf(bb, JPEG_START);
            int end = KMPMatch.indexOf(bb, JPEG_END);
            
            if (start == -1 || end == -1) {
                // no more JPEG files
                break;
            }
            
            bb.position(start);
            ByteBuffer bbJpg = bb.duplicate();
            bbJpg.limit(end + 2);
            bb.position(end);
            
            String jpgName;
            
            if (files == 1) {
                jpgName = String.format("%s", assetName);
            } else {
                jpgName = String.format("%s_%d", assetName, files);
            }
            
            files++;
            
            extractToFile(bbJpg, id, jpgName, "jpg");
        }
    }

}
