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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class to translate Unity class names and IDs.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ClassID {
    
    private static final Logger L = Logger.getLogger(ClassID.class.getName());
    
    private static final Map<Integer, String> ID_TO_NAME = new HashMap<>();
    private static final Map<String, Integer> NAME_TO_ID = new HashMap<>();

    static {
        try (
            InputStream is = ClassID.class.getResource("ClassID.txt").openStream();
        ) {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            for (String line; (line = br.readLine()) != null;) {
                // skip comments
                if (line.startsWith("#") || line.startsWith("//")) {
                    continue;
                }
                
                String[] parts = line.split("\t");

                if (parts.length == 2) {
                    int id = Integer.parseInt(parts[0]);
                    String name = parts[1];

                    ID_TO_NAME.put(id, name);
                    NAME_TO_ID.put(name, id);
                }
            }
        } catch (Exception ex) {
            L.log(Level.WARNING, null, ex);
        }
    }
    
    public static Integer getIDForName(String className) {
        return NAME_TO_ID.get(className);
    }
    
    public static String getNameForID(int classID) {
        return ID_TO_NAME.get(classID);
    }
    
    public static String getSafeNameForID(int classID) {
        String className = getNameForID(classID);
        // custom/unknown class? then use placeholder
        if (className == null) {
            className = "Class" + classID;
        }
        return className;
    }
}
