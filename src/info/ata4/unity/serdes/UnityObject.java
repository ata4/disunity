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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class UnityObject extends UnityBase {
    
    private Map<String, UnityField> fields = new LinkedHashMap<>();
    
    public Collection<UnityField> getFields() {
        return fields.values();
    }
    
    public UnityField getField(String name) {
        return fields.get(name);
    }
    
    public UnityField addField(UnityField field) {
        return fields.put(field.getName(), field);
    }
    
    public UnityField removeField(UnityField field) {
        return fields.remove(field.getName());
    }

    public <T> T getValue(String name) {
        UnityField f = getField(name);

        if (f == null) {
            return null;
        } else {
            return (T) f.getValue();
        }
    }
    
    public <T> void setValue(String name, T value) {
        UnityField f = getField(name);
        
        if (f != null) {
            f.setValue(value);
        }
    }
}
