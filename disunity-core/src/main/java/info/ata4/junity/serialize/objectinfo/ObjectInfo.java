/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.serialize.objectinfo;

import info.ata4.io.Struct;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @unity SerializedFile::ObjectInfo
 */
public abstract class ObjectInfo implements Struct {

    // Object data offset
    protected long byteStart;

    // Object data size
    protected long byteSize;

    // Type ID, equal to classID if it's not a MonoBehaviour
    protected int typeID;

    // Class ID, probably something else in asset format <=5
    protected int classID;

    public long offset() {
        return byteStart;
    }

    public void offset(long offset) {
        this.byteStart = offset;
    }

    public long length() {
        return byteSize;
    }

    public void length(long length) {
        this.byteSize = length;
    }

    public boolean isScript() {
        return typeID < 0;
    }

    public int typeID() {
        return typeID;
    }

    public void typeID(int typeID) {
        this.typeID = typeID;
    }

    public int classID() {
        return classID;
    }

    public void classID(int classID) {
        this.classID = classID;
    }
}
