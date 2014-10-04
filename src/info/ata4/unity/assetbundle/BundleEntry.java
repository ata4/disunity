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

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BundleEntry {
    
    protected final BundleEntryInfo info;
    
    private AssetBundleHeader sourceBundleHeader;
    
    public BundleEntry(BundleEntryInfo info) {
        this.info = info;
    }
    
    public BundleEntryInfo getInfo() {
        return info;
    }

    public AssetBundleHeader getSourceBundleHeader() {
        return sourceBundleHeader;
    }

    public void setSourceBundleHeader(AssetBundleHeader sourceBundleHeader) {
        this.sourceBundleHeader = sourceBundleHeader;
    }

    @Override
    public String toString() {
        return info.toString();
    }
}
