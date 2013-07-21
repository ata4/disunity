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

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class RawHandler extends ExtractHandler {
    
    private String className;
    
    @Override
    public String getClassName() {
        return className;
    }
 
    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public void extract(ByteBuffer bb, int id) throws IOException {
        String assetName = getAssetName(bb);
        bb.rewind();
        extractToFile(bb, id, assetName, "bin");
    }
}
