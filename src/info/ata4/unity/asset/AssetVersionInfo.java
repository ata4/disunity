/*
 ** 2014 October 31
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.asset;

import info.ata4.unity.util.UnityVersion;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetVersionInfo {
    
    private UnityVersion unityVersion;
    private UnityVersion unityRevision;
    
    // 5 = 1.2 - 2.0
    // 6 = 2.1 - 2.6
    // 8 = 3.1 - 3.4
    // 9 = 3.5 - 4.5
    // 14 = 5.0
    private int assetVersion;

    public UnityVersion getUnityVersion() {
        return unityVersion;
    }

    public void setUnityVersion(UnityVersion unityVersion) {
        this.unityVersion = unityVersion;
    }

    public UnityVersion getUnityRevision() {
        return unityRevision;
    }

    public void setUnityRevision(UnityVersion unityRevision) {
        this.unityRevision = unityRevision;
    }

    public int getAssetVersion() {
        return assetVersion;
    }

    public void setAssetVersion(int assetVersion) {
        this.assetVersion = assetVersion;
    }
    
    public boolean swapRequired() {
        // older formats use big endian
        if (assetVersion <= 5) {
            return false;
        } else {
            return true;
        }
    }
}
