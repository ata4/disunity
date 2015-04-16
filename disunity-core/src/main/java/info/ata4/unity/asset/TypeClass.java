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

import info.ata4.unity.util.UnityGUID;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TypeClass {
    
    private int classID;
    private UnityGUID classGUID;
    private UnityGUID scriptGUID;
    private TypeNode typeTree;

    public int getClassID() {
        return classID;
    }

    public void setClassID(int classID) {
        this.classID = classID;
    }

    public UnityGUID getClassGUID() {
        return classGUID;
    }

    public void setClassGUID(UnityGUID classGUID) {
        this.classGUID = classGUID;
    }

    public UnityGUID getScriptGUID() {
        return scriptGUID;
    }

    public void setScriptGUID(UnityGUID scriptGUID) {
        this.scriptGUID = scriptGUID;
    }

    public TypeNode getTypeTree() {
        return typeTree;
    }

    public void setTypeTree(TypeNode typeTree) {
        this.typeTree = typeTree;
    }
}
