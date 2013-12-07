/*
 ** 2013 December 07
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.serdes;

/**
 * Unity object with an associated immutable type string.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class UnityType {
    
    private final String type;

    public UnityType(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
}
