/*
 ** 2015 November 25
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.util;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class Formatters {

    private Formatters() {
    }

    public static String hex(Object v) {
        if (!(v instanceof Number)) {
            return String.valueOf(v);
        }
        Number n = (Number) v;
        return String.format("%08x", n.intValue());
    }

    public static String byteCount(Object v) {
        if (!(v instanceof Number)) {
            return String.valueOf(v);
        }

        long bytes = ((Number) v).longValue();

        int unit = 1024;
        if (bytes < unit) {
            return bytes + " B";
        }

        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "KMGTPE".charAt(exp - 1) + "i";
        return String.format("%.2f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
