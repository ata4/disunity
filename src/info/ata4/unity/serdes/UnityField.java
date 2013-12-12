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

/**
 * Unity object that can carry a value.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class UnityField<T> extends UnityNamedType {
    
    private T value;
    
    public UnityField(String type) {
        super(type);
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
