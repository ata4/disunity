/*
 ** 2013 July 02
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
public class PPtr extends AssetStruct {
    
    private static final Logger L = Logger.getLogger(PPtr.class.getName());
    
    public int fileID;
    public int pathID;

    public PPtr(AssetFormat formatInfo) {
        super(formatInfo);
    }

    @Override
    public void readData() throws IOException {
        fileID = in.readInt();
        L.log(Level.FINEST, "fileID = {0} ", fileID);
        
        pathID = in.readInt();
        L.log(Level.FINEST, "pathID = {0} ", pathID);
    }
}
