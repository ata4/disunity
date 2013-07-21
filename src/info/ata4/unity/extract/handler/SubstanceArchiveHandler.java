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

import info.ata4.unity.struct.asset.SubstanceArchive;
import info.ata4.util.io.ByteBufferInput;
import info.ata4.util.io.DataInputReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class SubstanceArchiveHandler extends ExtractHandler {
    
    private static final Logger L = Logger.getLogger(RawHandler.class.getName());
    
    @Override
    public String getClassName() {
        return "SubstanceArchive";
    }
    
    public String getExtension() {
        return "sbsar";
    }

    @Override
    public void extract(ByteBuffer bb, int id) throws IOException {
        DataInputReader in = new DataInputReader(new ByteBufferInput(bb));
        
        SubstanceArchive sb = new SubstanceArchive(getAssetFormat());
        sb.read(in);
        
        extractToFile(sb.packageData, id, sb.name, getExtension());
    }
}
