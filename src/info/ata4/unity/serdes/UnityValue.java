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

/**
 * Simple Unity object wrapper.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class UnityValue<T> implements UnityTag<T> {
    
    private String type;
    private T value;
    
    public UnityValue() {
    }
    
    public UnityValue(String type, T value) {
        this.type = type;
        this.value = value;
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
    public T get() {
        return value;
    }

    @Override
    public void set(T value) {
        this.value = value;
    }
}
