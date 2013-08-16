/*
 ** 2013 July 24
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.serdes;

import java.util.LinkedHashMap;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class UnityObject extends LinkedHashMap<String, UnityField> {
    
    private String name;
    private String type;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public <T> T getValue(String name) {
        UnityField f = get(name);

        if (f == null) {
            return null;
        } else {
            return (T) f.getValue();
        }
    }
    
    public <T> void setValue(String name, T value) {
        UnityField f = get(name);
        
        if (f != null) {
            f.setValue(value);
        }
    }
}
