/*
 ** 2014 December 22
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.util;

/**
 * Unity class ID container.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class UnityClass {
    
    private static final UnityClassDatabase DB = new UnityClassDatabase();

    private final int id;
    
    public UnityClass(int id) {
        this.id = id;
    }
    
    public UnityClass(String name) {
        Integer lid = DB.IDForName(name);
        
        // the ID must be valid
        if (lid == null) {
            throw new IllegalArgumentException("Unknown class name: " + name);
        }
        
        this.id = lid;
    }
    
    public int ID() {
        return id;
    }
    
    public String name() {
        return DB.nameForID(id);
    }

    @Override
    public int hashCode() {
        return 78 * id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UnityClass other = (UnityClass) obj;
        return this.id == other.id;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Class ");
        sb.append(id);
        
        String name = name();
        if (name != null) {
            sb.append(" (");
            sb.append(name);
            sb.append(")");
        }
        
        return sb.toString();
    }
}
