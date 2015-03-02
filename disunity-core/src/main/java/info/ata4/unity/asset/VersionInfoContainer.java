/*
 ** 2014 November 28
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.asset;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class VersionInfoContainer {
    
    protected final VersionInfo versionInfo;

    public VersionInfoContainer(VersionInfo versionInfo) {
        this.versionInfo = versionInfo;
    }
}
