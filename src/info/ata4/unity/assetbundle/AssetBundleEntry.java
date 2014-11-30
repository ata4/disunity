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
public class AssetBundleEntry {
    
    private final String name;
    private final long size;
    private final InputStream is;

    public AssetBundleEntry(String name, long size, InputStream is) {
        this.name = name;
        this.size = size;
        this.is = is;
    }
    
    public String getName() {
        return name;
    }
    
    public long getSize() {
        return size;
    }

    public InputStream getInputStream() {
        return is;
    }

    @Override
    public String toString() {
        return name;
    }
}
