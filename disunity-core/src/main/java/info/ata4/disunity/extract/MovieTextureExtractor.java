/*
 ** 2014 December 27
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.extract;

import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.log.LogUtils;
import info.ata4.unity.engine.MovieTexture;
import info.ata4.unity.rtti.ObjectData;
import info.ata4.unity.util.UnityClass;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class MovieTextureExtractor extends AbstractAssetExtractor {
    
    private static final Logger L = LogUtils.getLogger();

    @Override
    public UnityClass getUnityClass() {
        return new UnityClass("MovieTexture");
    }
    
    @Override
    public void extract(ObjectData objectData) throws IOException {
        MovieTexture mtex = new MovieTexture(objectData.instance());
        String name = mtex.getName();
        ByteBuffer movieData = mtex.getMovieData();
        
        // skip empty buffers
        if (ByteBufferUtils.isEmpty(movieData)) {
            L.log(Level.WARNING, "Movie texture clip {0} is empty", name);
            return;
        }
        
        String fourCC;
        byte[] fourCCRaw = new byte[4];
        String ext;
        
        movieData.rewind();
        movieData.get(fourCCRaw);
        
        fourCC = new String(fourCCRaw, "ASCII");
        
        switch (fourCC) {
            case "OggS":
                ext = "ogv";
                break;
                
            default:
                ext = "mov";
                L.log(Level.WARNING, "Unrecognized movie fourCC \"{0}\"", fourCC);
        }
        
        writeFile(name, ext, movieData);
    }
}
