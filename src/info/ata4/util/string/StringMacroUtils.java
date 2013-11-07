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
 * Utility class to simulate some string related C++ macros.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class StringMacroUtils {
    
    private StringMacroUtils() {
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
}
