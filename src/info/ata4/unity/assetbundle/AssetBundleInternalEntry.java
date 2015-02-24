/*
 ** 2014 December 03
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.assetbundle;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetBundleInternalEntry extends AssetBundleEntry {
    
    private final AssetBundleReader reader;
    private final AssetBundleEntryInfo info;

    public AssetBundleInternalEntry(AssetBundleReader reader, AssetBundleEntryInfo info) {
        this.reader = reader;
        this.info = info;
    }
    
    @Override
    public String getName() {
        return info.getName();
    }
    
    @Override
    public long getSize() {
        return info.getSize();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return reader.getInputStreamForEntry(info);
    }

    @Override
    public String toString() {
        return getName();
    }
}
