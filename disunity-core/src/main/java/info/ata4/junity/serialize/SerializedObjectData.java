/*
 ** 2014 September 20
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.serialize;

import info.ata4.junity.serialize.objectinfo.ObjectInfo;
import info.ata4.junity.serialize.typetree.Type;
import info.ata4.util.collection.Node;
import java.nio.ByteBuffer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class SerializedObjectData {

    private final long id;
    private ObjectInfo info;
    private ByteBuffer buffer;
    private Node<Type> typeTree;

    public SerializedObjectData(long id) {
        this.id = id;
    }

    public long id() {
        return id;
    }

    public ObjectInfo info() {
        return info;
    }

    public void info(ObjectInfo info) {
        this.info = info;
    }

    public ByteBuffer buffer() {
        return buffer;
    }

    public void buffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public Node<Type> typeTree() {
        return typeTree;
    }

    public void typeTree(Node<Type> typeTree) {
        this.typeTree = typeTree;
    }
}
