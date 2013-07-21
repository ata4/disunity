/*
 ** 2013 July 01
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.extract.handler;

import info.ata4.unity.struct.asset.MovieTexture;
import info.ata4.util.io.ByteBufferInput;
import info.ata4.util.io.DataInputReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class MovieTextureHandler extends ExtractHandler {
    
    private static final Logger L = Logger.getLogger(MovieTextureHandler.class.getName());

    @Override
    public String getClassName() {
        return "MovieTexture";
    }

    @Override
    public void extract(ByteBuffer bb, int id) throws IOException {
        DataInputReader in = new DataInputReader(new ByteBufferInput(bb));
        
        MovieTexture mt = new MovieTexture(getAssetFormat());
        mt.read(in);
        
        String ext;
        String fourCC = new String(mt.moveData, 0, 4);
        
        switch (fourCC) {
            case "OggS":
                ext = "ogv";
                break;
                
            default:
                ext = "mov";
                L.log(Level.WARNING, "Unrecognized movie fourCC \"{0}\"", fourCC);
        }
        
        extractToFile(mt.moveData, id, mt.name, ext);
    }
    
}
