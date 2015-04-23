/*
 ** 2015 April 23
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.util;

import info.ata4.unity.asset.TypeNode;
import java.util.Objects;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TypeTreeDatabaseEntry {
    
    private final UnityClass unityClass;
    private final UnityVersion unityVersion;
    private final TypeNode typeNode;

    public TypeTreeDatabaseEntry(UnityClass unityClass, UnityVersion unityVersion, TypeNode typeNode) {
        this.unityClass = unityClass;
        this.unityVersion = unityVersion;
        this.typeNode = typeNode;
    }
    
    public UnityClass unityClass() {
        return unityClass;
    }
    
    public UnityVersion unityVersion() {
        return unityVersion;
    }
    
    public TypeNode typeNode() {
        return typeNode;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.unityClass);
        hash = 37 * hash + Objects.hashCode(this.unityVersion);
        hash = 37 * hash + Objects.hashCode(this.typeNode);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TypeTreeDatabaseEntry other = (TypeTreeDatabaseEntry) obj;
        if (!Objects.equals(this.unityClass, other.unityClass)) {
            return false;
        }
        if (!Objects.equals(this.unityVersion, other.unityVersion)) {
            return false;
        }
        if (!Objects.equals(this.typeNode, other.typeNode)) {
            return false;
        }
        return true;
    }
}
