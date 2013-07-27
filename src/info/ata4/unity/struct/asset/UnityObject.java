/*
 ** 2013 June 27
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

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class UnityObject extends AssetStruct {
    
    private static final Logger L = Logger.getLogger(UnityObject.class.getName());
    
    public String name;
    
    public UnityObject(AssetFormat formatInfo) {
        super(formatInfo);
    }

    @Override
    public void readData() throws IOException {
        name = in.readString();
        L.log(Level.FINEST, "name = {0}", name);
    }
}
