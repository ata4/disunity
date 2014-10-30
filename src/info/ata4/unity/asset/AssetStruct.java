/*
 ** 2014 October 29
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.asset;

import info.ata4.io.Struct;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class AssetStruct implements Struct {
    
    protected int assetVersion;
    
    public int getAssetVersion() {
        return assetVersion;
    }

    public void setAssetVersion(int assetVersion) {
        this.assetVersion = assetVersion;
    }

}
