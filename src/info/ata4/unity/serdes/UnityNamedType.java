/*
 ** 2013 August 25
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.serdes;

/**
 * Unity object with an associated name and immutable type string.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class UnityNamedType extends UnityType {
    
    private String name;
    
    public UnityNamedType(String type) {
        super(type);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
