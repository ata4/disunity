/*
 ** 2014 May 19
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.serdes;

import info.ata4.log.LogUtils;
import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Unity object that can contain one or more named fields.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class UnityObject implements UnityTag<Map<String, UnityTag>> {
    
    private static final Logger L = LogUtils.getLogger();
    
    private String name;
    private String type;
    private Map<String, UnityTag> fields = new LinkedHashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }
    
    @Override
    public Map<String, UnityTag> get() {
        return fields;
    }

    @Override
    public void set(Map<String, UnityTag> value) {
        this.fields = value;
    }
    
    public <T> T getValue(String name, boolean unwrap) {
        UnityTag f = fields.get(name);

        // return null if the field doesn't exist
        if (f == null) {
            return null;
        }
        
        if (unwrap) {
            // unwrap UnityTag
            while (f.get() instanceof UnityTag) {
                f = (UnityTag) f.get();
            }
        }
        
        return (T) f.get();
    }
    
    public <T> T getValue(String name) {
        return getValue(name, true);
    }
    
    public <T> void setValue(String name, T value, boolean unwrap) {
        UnityTag f = fields.get(name);
        
        if (f == null) {
            return;
        }
        
        if (unwrap) {
            // unwrap UnityTag
            while (f.get() instanceof UnityTag) {
                f = (UnityTag) f.get();
            }
        }
        
        f.set(value);
    }
    
    public <T> void setValue(String name, T value) {
        setValue(name, value, true);
    }
    
    public <T> T getObject(String name, Class<T> clazz) {
        if (!hasValue(name)) {
            return null;
        }
        
        UnityObject obj = getValue(name, false);
        
        try {
            Constructor<T> cnst = clazz.getConstructor(UnityObject.class);
            return cnst.newInstance(obj);
        } catch (ReflectiveOperationException ex) {
            L.log(Level.SEVERE, "Can't instantiate object from class " + clazz.getName(), ex);
        }
        
        return null;
    }

    public boolean hasValue(String name) {
        return fields.containsKey(name);
    }
}
