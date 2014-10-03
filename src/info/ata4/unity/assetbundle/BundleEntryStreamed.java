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

import java.io.InputStream;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BundleEntryStreamed extends BundleEntry {
    
    private InputStream is;

    public BundleEntryStreamed(BundleEntryInfo info) {
        super(info);
    }

    public InputStream getInputStream() {
        return is;
    }

    public void setInputStream(InputStream is) {
        this.is = is;
    }
}
