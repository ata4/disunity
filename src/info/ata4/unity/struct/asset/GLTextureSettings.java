/*
 ** 2013 June 19
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

//    +--GLTextureSettings "Base" (4)
//       |--int "m_FilterMode"
//       |--int "m_Aniso"
//       |--float "m_MipBias"
//       +--int "m_WrapMode"

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class GLTextureSettings extends AssetStruct {
    
    private static final Logger L = Logger.getLogger(GLTextureSettings.class.getName());
    
    public int filterMode;
    public int aniso;
    public float mipBias;
    public int wrapMode;
    
    public GLTextureSettings(AssetFormat formatInfo) {
        super(formatInfo);
    }

    @Override
    public void readData() throws IOException {
        filterMode = in.readInt();
        L.log(Level.FINEST, "filterMode = {0}", filterMode);
        
        aniso = in.readInt();
        L.log(Level.FINEST, "aniso = {0}", aniso);
        
        mipBias = in.readFloat();
        L.log(Level.FINEST, "mipBias = {0}", mipBias);
        
        wrapMode = in.readInt();
        L.log(Level.FINEST, "wrapMode = {0}", wrapMode);
    }
}
