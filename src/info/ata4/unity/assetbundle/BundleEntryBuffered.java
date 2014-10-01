/*
 ** 2014 September 29
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.assetbundle;

import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
import info.ata4.io.DataRandomAccess;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BundleEntryBuffered extends BundleEntry {
    
    private DataRandomAccess ra;

    public BundleEntryBuffered(BundleEntryInfo info) {
        super(info);
    }

    public DataRandomAccess getRandomAccess() {
        return ra;
    }

    public void setRandomAccess(DataRandomAccess ra) {
        this.ra = ra;
    }
    
    public DataInputReader getReader() {
        return ra.getReader();
    }
    
    public DataOutputWriter getWriter() {
        return ra.getWriter();
    }

}
