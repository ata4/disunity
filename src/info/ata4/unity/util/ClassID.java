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
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

/**
 * Helper class to translate Unity class names and IDs.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ClassID {
    
    private static final Logger L = LogUtils.getLogger();
    private static final String CLASSID_PATH = "/resources/classid.txt";
    private static final String CHARSET = "ASCII";
    
    private static final Map<Integer, String> ID_TO_NAME;
    private static final Map<String, Integer> NAME_TO_ID;

    static {
        Map<Integer, String> IDToName = new HashMap<>();
        Map<String, Integer> nameToID = new HashMap<>();
        
        try (InputStream is = ClassID.class.getResourceAsStream(CLASSID_PATH)) {
            if (is == null) {
                throw new IOException("Class ID database not found");
            }
            
            List<String> lines = IOUtils.readLines(is, CHARSET);
            for (String line : lines) {
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
        
        ID_TO_NAME = Collections.unmodifiableMap(IDToName);
        NAME_TO_ID = Collections.unmodifiableMap(nameToID);
    }
    
    public static Integer getIDForName(String className, boolean ignoreCase) {
        if (ignoreCase) {
            for (Map.Entry<String, Integer> entry : NAME_TO_ID.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(className)) {
                    return entry.getValue();
                }
            }
            return null;
        } else {
            return NAME_TO_ID.get(className);
        }
    }
    
    public static Integer getIDForName(String className) {
        return getIDForName(className, false);
    }
    
    public static String getNameForID(int classID, boolean safe) {
        String className = ID_TO_NAME.get(classID);
        
        // custom/unknown class? then use placeholder
        if (className == null && safe) {
            className = "Class" + classID;
        }
        
        return className;
    }
    
    public static String getNameForID(int classID) {
        return getNameForID(classID, false);
    }
    
    private ClassID() {
    }
}
