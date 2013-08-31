/*
 ** 2013 August 18
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public enum DisUnityCommand {

    DUMP,
    DUMP_STRUCT,    
    EXTRACT,
    EXTRACT_RAW,
    LEARN,
    INFO,
    STATS,
    UNBUNDLE,
    FIXREFS,
    SPLIT;
    
    public static DisUnityCommand fromString(String value) {
        return valueOf(value.toUpperCase().replace("-", "_"));
    }
   
    @Override
    public String toString() {
        return super.toString().toLowerCase().replace("_", "-");
    }
}
