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
 * Wrapper to tag objects with a Unity type string.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public interface UnityTag<T> {
    
    public T get();

    public void set(T value);

    public String getType();

    public void setType(String type);
}
