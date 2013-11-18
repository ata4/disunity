/*
 ** 2013 July 01
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.extract.handler;

import info.ata4.unity.serdes.UnityArray;
import info.ata4.unity.serdes.UnityObject;
import info.ata4.unity.struct.ObjectPath;
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
    public void extract(ObjectPath path, UnityObject obj) throws IOException {
        String name = obj.getValue("m_Name");
        UnityArray movieData = obj.getValue("m_MovieData");
        ByteBuffer movieBuffer = movieData.getRaw();
        
        String ext;
        String fourCC = new String(movieBuffer.array(), 0, 4);
        
        switch (fourCC) {
            case "OggS":
                ext = "ogv";
                break;
                
            default:
                ext = "mov";
                L.log(Level.WARNING, "Unrecognized movie fourCC \"{0}\"", fourCC);
        }
        
        writeFile(movieBuffer, path.pathID, name, ext);
    }
}
