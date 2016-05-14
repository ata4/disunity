/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.serialize.typetree;

import info.ata4.io.Struct;

/**
 * Class for objects that contain the runtime type of a single field.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @unity TypeTree
 */
public abstract class Type implements Struct {

    public static final int FLAG_FORCE_ALIGN = 0x4000;

    // field type string
    protected String type;

    // field name string
    protected String name;

    // size of the field value in bytes or -1 if the field contains sub-fields only
    protected int size;

    // field index for the associated parent field
    protected int index;

    // set to 1 if "type" is "Array" or "TypelessData"
    protected boolean isArray;

    // type version, starts with 1 and is incremented when the type
    // information is updated in a new Unity release
    //
    // equal to serializedVersion in YAML format files
    protected int version;

    // field flags
    // observed values:
    // 0x1
    // 0x10
    // 0x800
    // 0x4000
    // 0x8000
    // 0x200000
    // 0x400000
    protected int metaFlag;

    public String typeName() {
        return type;
    }

    public void typeName(String type) {
        this.type = type;
    }

    public String fieldName() {
        return name;
    }

    public void fieldName(String name) {
        this.name = name;
    }

    public int size() {
        return size;
    }

    public void size(int size) {
        this.size = size;
    }

    public int index() {
        return index;
    }

    public void index(int index) {
        this.index = index;
    }

    public boolean isArray() {
        return isArray;
    }

    public void isArray(boolean isArray) {
        this.isArray = isArray;
    }

    public int version() {
        return version;
    }

    public void version(int version) {
        this.version = version;
    }

    public int metaFlag() {
        return metaFlag;
    }

    public void metaFlag(int metaFlag) {
        this.metaFlag = metaFlag;
    }
}
