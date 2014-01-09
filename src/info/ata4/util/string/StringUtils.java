/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.util.string;

/**
 * Utility class for some string functions.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class StringUtils {
    
    private StringUtils() {
    }
    
    /**
     * Simulates the MAKEID macro from the Source SDK. It makes a 4-byte
     * "packed ID" int out of a 4 character string.
     * 
     * @param id 4 character string
     * @return packed integer ID
     */
    public static int makeID(String id) {
        if (id.length() != 4) {
            throw new IllegalArgumentException("String must be exactly 4 characters long");
        }
        
        byte[] bytes = id.getBytes();
        return (bytes[3] << 24) | (bytes[2] << 16) | (bytes[1] << 8) | bytes[0];
    }
    
    /**
     * Decodes a "packed ID" into a 4 character string that was made by 
     * the MAKEID macro from the Source SDK.
     * 
     * @param id packed integer ID
     * @return 4 character string
     */
    public static String unmakeID(int id) {
        byte[] bytes = new byte[] {
            (byte) id,
            (byte) (id >>> 8),
            (byte) (id >>> 16),
            (byte) (id >>> 24)
        };
        return new String(bytes);
    }
    
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
