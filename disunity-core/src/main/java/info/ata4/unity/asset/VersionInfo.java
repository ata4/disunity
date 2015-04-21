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
import java.nio.ByteOrder;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class VersionInfo {
    
    private UnityVersion unityVersion;
    private UnityVersion unityRevision;
    
    // 5 = 1.2 - 2.0
    // 6 = 2.1 - 2.6
    // 7 = 3.0 (?)
    // 8 = 3.1 - 3.4
    // 9 = 3.5 - 4.5
    // 11 = pre-5.0
    // 12 = pre-5.0
    // 13 = pre-5.0
    // 14 = 5.0
    // 15 = 5.0 (p3 and newer)
    private int assetVersion;

    public UnityVersion unityVersion() {
        return unityVersion;
    }

    public void unityVersion(UnityVersion unityVersion) {
        this.unityVersion = unityVersion;
    }

    public UnityVersion unityRevision() {
        return unityRevision;
    }

    public void unityRevision(UnityVersion unityRevision) {
        this.unityRevision = unityRevision;
    }

    public int assetVersion() {
        return assetVersion;
    }

    public void assetVersion(int assetVersion) {
        this.assetVersion = assetVersion;
    }
    
    public ByteOrder order() {
        // older formats use big endian
        return assetVersion > 5 ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
    }
}
