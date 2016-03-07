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

import com.google.common.collect.BiMap;
import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import info.ata4.junity.UnityHash128;
import info.ata4.junity.UnityVersion;
import info.ata4.junity.serialize.SerializedFileException;
import info.ata4.util.collection.Node;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TypeTreeV3<T extends TypeV2> extends TypeTreeV2<T> {

    public TypeTreeV3(Class<T> elementFactory) {
        super(elementFactory);
    }

    @Override
    public void read(DataReader in) throws IOException {
        revision = new UnityVersion(in.readStringNull(255));
        attributes = in.readInt();
        embedded = in.readBoolean();
        int numBaseClasses = in.readInt();

        for (int i = 0; i < numBaseClasses; i++) {
            int classID = in.readInt();

            TypeRoot typeRoot = new TypeRoot();
            typeRoot.classID(classID);

            if (classID < 0) {
                UnityHash128 scriptID = new UnityHash128();
                in.readStruct(scriptID);
                typeRoot.scriptID(scriptID);
            }

            UnityHash128 oldTypeHash = new UnityHash128();
            in.readStruct(oldTypeHash);
            typeRoot.oldTypeHash(oldTypeHash);

            if (embedded) {
                Node<T> node = new Node<>();
                readNode(in, node);
                typeRoot.nodes(node);
            }

            typeMap.put(classID, typeRoot);
        }
    }

    private void readNode(DataReader in, Node<T> node) throws IOException {
        int numFields = in.readInt();
        int stringTableLen = in.readInt();

        // read types
        List<T> types = new ArrayList<>(numFields);
        for (int j = 0; j < numFields; j++) {
            T type = createElement();
            in.readStruct(type);
            types.add(type);
        }

        // read local string table and add common strings
        BiMap<Integer, String> stringTable = StringTable.read(in, stringTableLen);
        stringTable.putAll(StringTable.commonStrings(revision.major()));

        // assign strings
        for (T field : types) {
            int nameOffset = field.nameOffset();
            String name = stringTable.get(nameOffset);

            if (name == null) {
                throw new SerializedFileException("No string table entry found for name index " + nameOffset);
            }

            field.fieldName(name);

            int typeOffset = field.typeOffset();
            String type = stringTable.get(typeOffset);

            if (name == null) {
                throw new SerializedFileException("No string table entry found for type index " + typeOffset);
            }

            field.typeName(type);
        }

        // convert list to tree structure
        Node<T> nodePrev = null;
        for (T type : types) {
            // create root node
            if (nodePrev == null) {
                node.data(type);
                nodePrev = node;
                continue;
            }

            Node<T> nodeCurr = new Node<>(type);

            int levels = nodePrev.data().treeLevel() - type.treeLevel();
            if (levels >= 0) {
                // move down in tree hierarchy if required
                for (int i = 0; i < levels; i++) {
                    nodePrev = nodePrev.parent();
                }

                nodePrev.parent().add(nodeCurr);
            } else {
                // can move only one level up at a time, so simply add the node
                nodePrev.add(nodeCurr);
            }

            nodePrev = nodeCurr;
        }
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeStringNull(revision.toString());
        out.writeInt(attributes);
        out.writeBoolean(embedded);
        out.writeInt(typeMap.size());

        for (Map.Entry<Integer, TypeRoot<T>> entry : typeMap.entrySet()) {
            int classID = entry.getKey();
            TypeRoot typeRoot = entry.getValue();

            out.writeInt(classID);

            if (classID < 0) {
                out.writeStruct(typeRoot.scriptID());
            }

            out.writeStruct(typeRoot.oldTypeHash());

            if (embedded) {
                writeNode(out, typeRoot.nodes());
            }
        }
    }

    private void writeNode(DataWriter out, Node<T> node) throws IOException {
        List<T> types = new ArrayList<>();
        serializeNode(node, types, 0);

        // build string table
        AtomicInteger index = new AtomicInteger();
        Map<String, Integer> localMap = new LinkedHashMap<>();
        Map<String, Integer> commonMap = StringTable.commonStrings(revision.major()).inverse();

        Function<String, Integer> addStringOffset = typeName -> {
            if (commonMap.containsKey(typeName)) {
                return commonMap.get(typeName);
            } else if (localMap.containsKey(typeName)) {
                return localMap.get(typeName);
            } else {
                int stringIndex = index.getAndAdd(typeName.length() + 1);
                localMap.put(typeName, stringIndex);
                return stringIndex;
            }
        };

        // apply string offsets
        types.forEach(type -> {
            type.typeOffset(addStringOffset.apply(type.typeName()));
            type.nameOffset(addStringOffset.apply(type.fieldName()));
        });

        out.writeInt(types.size());
        out.writeInt(index.get());

        for (T type : types) {
            out.writeStruct(type);
        }

        for (String string : localMap.keySet()) {
            out.writeStringNull(string);
        }
    }

    private void serializeNode(Node<T> node, List<T> list, int level) {
        node.data().treeLevel(level);
        list.add(node.data());
        node.forEach(n -> serializeNode(n, list, level + 1));
    }
}
