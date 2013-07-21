/*
 ** 2013 July 02
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.asset;

/**
 * Asset format metadata class.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetFormat {
    
    private final int version;
    private final String revision;
    private final int format;
    
    public AssetFormat(int version, String revision, int format) {
        this.version = version;
        this.revision = revision;
        this.format = format;
    }
    
    public int getFormat() {
        return format;
    }
    
    public int getVersion() {
        return version;
    }
    
    public String getRevision() {
        return revision;
    }
}
