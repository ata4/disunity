/*
 ** 2015 November 23
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.serialize.typetree;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import info.ata4.util.collection.Node;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TypeTreeV1<T extends TypeV1> extends TypeTree<T> {

    public TypeTreeV1(Class<T> elementFactory) {
        super(elementFactory);
    }

    @Override
    public void read(DataReader in) throws IOException {
        int numBaseClasses = in.readInt();
        for (int i = 0; i < numBaseClasses; i++) {
            int classID = in.readInt();

            TypeRoot baseClass = new TypeRoot();
            baseClass.classID(classID);

            Node<T> node = new Node<>();
            readNode(in, node);
            baseClass.nodes(node);

            typeMap.put(classID, baseClass);
        }

        embedded = numBaseClasses > 0;
    }

    private void readNode(DataReader in, Node<T> node) throws IOException {
        T type = createElement();
        in.readStruct(type);

        node.data(type);

        int numChildren = in.readInt();
        for (int i = 0; i < numChildren; i++) {
            Node<T> childNode = new Node<>();
            readNode(in, childNode);
            node.add(childNode);
        }
    }

    @Override
    public void write(DataWriter out) throws IOException {
        // write empty type tree if types are not embedded
        if (!embedded) {
            out.writeInt(0);
            return;
        }

        int numBaseClasses = typeMap.size();
        out.writeInt(numBaseClasses);

        for (TypeRoot bc : typeMap.values()) {
            int classID = bc.classID();
            out.writeInt(classID);

            Node<T> node = bc.nodes();

            writeNode(out, node);
        }
    }

    private void writeNode(DataWriter out, Node<T> node) throws IOException {
        T type = node.data();
        out.writeStruct(type);

        int numChildren = node.size();
        out.writeInt(numChildren);

        for (Node child : node) {
            writeNode(out, child);
        }
    }

}
