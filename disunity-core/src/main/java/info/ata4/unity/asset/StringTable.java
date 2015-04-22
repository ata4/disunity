/*
 ** 2015 April 12
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.asset;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class StringTable {
    
    private static final int FLAG_INTERNAL = 1 << 31;
   
    private final Map<Integer, String> strings = new HashMap<>();
    
    public StringTable() throws IOException {
        byte[] data;
        try (InputStream is = getClass().getResourceAsStream("/resources/strings.dat")) {
            data = IOUtils.toByteArray(is);
        }
        loadStrings(data, true);
    }
    
    public StringTable(byte[] data) throws IOException {
        loadStrings(data, false);
    }

    private void loadStrings(byte[] data, boolean internal) {
        for (int i = 0, n = 0; i < data.length; i++) {
            if (data[i] == 0) {
                String string = new String(data, n, i - n, StandardCharsets.US_ASCII);
                
                if (internal) {
                    n |= FLAG_INTERNAL;
                }
                
                strings.put(n, string);

                n = i + 1;
            }
        }
    }
    
    public void loadStrings(byte[] data) {
        loadStrings(data, false);
    }
    
    public String getString(int offset) {
        return strings.get(offset);
    }
}
