/*
 ** 2013 July 01
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.extract;

import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.log.LogUtils;
import info.ata4.unity.engine.MovieTexture;
import info.ata4.unity.rtti.ObjectData;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class MovieTextureHandler extends AbstractObjectExtractor {
    
    private static final Logger L = LogUtils.getLogger();
    
    public MovieTextureHandler() {
        super("MovieTexture");
    }
    
    @Override
    public void process(ObjectData object) throws IOException {
        MovieTexture mtex = new MovieTexture(object.getInstance());
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
        
        files.add(new MutableFileHandle(name, ext, movieData));
    }
}
