/*
 ** 2015 April 15
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.asset;

import info.ata4.unity.util.UnityHash128;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @unity SerializedFile::Type
 */
public class BaseClass {
    
    private int classID;
    private UnityHash128 scriptID;
    private UnityHash128 oldTypeHash;
    private TypeNode typeTree;

    public int classID() {
        return classID;
    }

    public void classID(int classID) {
        this.classID = classID;
    }

    public UnityHash128 scriptID() {
        return scriptID;
    }

    public void scriptID(UnityHash128 scriptID) {
        this.scriptID = scriptID;
    }

    public UnityHash128 oldTypeHash() {
        return oldTypeHash;
    }

    public void oldTypeHash(UnityHash128 oldTypeHash) {
        this.oldTypeHash = oldTypeHash;
    }

    public TypeNode typeTree() {
        return typeTree;
    }

    public void typeTree(TypeNode typeTree) {
        this.typeTree = typeTree;
    }
}
