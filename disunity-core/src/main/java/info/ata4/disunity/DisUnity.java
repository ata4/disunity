/*
 ** 2013 June 17
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity;

/**
 * DisUnity program metadata.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DisUnity {

    public static String getName() {
        return "DisUnity";
    }

    public static String getProgramName() {
        return "disunity";
    }

    public static String getVersion() {
        return "0.5.0";
    }

    public static String getSignature() {
        return getName() + " v" + getVersion();
    }

    private DisUnity() {
    }
}
