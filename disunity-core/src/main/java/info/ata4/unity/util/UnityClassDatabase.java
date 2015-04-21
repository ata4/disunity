/*
 ** 2013 June 15
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.util;

import info.ata4.log.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database class to translate Unity class names and IDs.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class UnityClassDatabase {
    
    private static final Logger L = LogUtils.getLogger();
    private static final String CLASSID_PATH = "/resources/classes.txt";
    private static final String CHARSET = "ASCII";
    
    private final Map<Integer, String> IDToName = new HashMap<>();
    private final Map<String, Integer> nameToID = new HashMap<>();

    public UnityClassDatabase() {
        try (BufferedReader reader = databaseReader()) {
            for (String line; (line = reader.readLine()) != null;) {
                // skip comments and blank lines
                if (line.startsWith("#") || line.startsWith("//") || line.trim().isEmpty()) {
                    continue;
                }
                
                String[] parts = line.split("\\W+");

                if (parts.length == 2) {
                    int id = Integer.parseInt(parts[0]);
                    String name = parts[1];

                    IDToName.put(id, name);
                    nameToID.put(name, id);
                }
            }
        } catch (Exception ex) {
            L.log(Level.WARNING, "Can't load class ID database", ex);
        }
    }
    
    private BufferedReader databaseReader() throws IOException {
        InputStream is = UnityClass.class.getResourceAsStream(CLASSID_PATH);
        
        if (is == null) {
            throw new IOException("Class ID database not found");
        }
        
        return new BufferedReader(new InputStreamReader(is, CHARSET));
    }
    
    public Integer IDForName(String name) {
        return nameToID.get(name);
    }
    
    public String nameForID(Integer id) {
        return IDToName.get(id);
    }
}
