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

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import info.ata4.unity.util.UnityHash128;
import info.ata4.unity.util.UnityStruct;
import info.ata4.unity.util.UnityVersion;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @unity RTTIClassHierarchyDescriptor, RTTIBaseClassDescriptor2, TypeTree
 */
public class TypeTree extends UnityStruct {
    
    private final Map<Integer, BaseClass> typeMap;
    private int attributes;
    private boolean embedded;

    public TypeTree(VersionInfo versionInfo, Map<Integer, BaseClass> typeMap) {
        super(versionInfo);
        this.typeMap = typeMap;
    }
    
    public int attributes() {
        return attributes;
    }
    
    public void attributes(int attributes) {
        this.attributes = attributes;
    }
    
    public boolean embedded() {
        return embedded;
    }
    
    public void embedded(boolean embedded) {
        this.embedded = embedded;
    }

    @Override
    public void read(DataReader in) throws IOException {
        // revision/version for newer formats
        if (versionInfo.assetVersion() > 6) {
            versionInfo.unityRevision(new UnityVersion(in.readStringNull(255)));
            attributes = in.readInt();
        }
        
        // Unity 5+ uses a serialized tree structure and string buffers
        if (versionInfo.assetVersion() > 13) {
            StringTable stInt = new StringTable();
            
            embedded = in.readBoolean();
            int numBaseClasses = in.readInt();

            for (int i = 0; i < numBaseClasses; i++) {
                int classID = in.readInt();
                
                BaseClass baseClass = new BaseClass();
                baseClass.classID(classID);
                
                if (classID < 0) {
                    UnityHash128 scriptID = new UnityHash128(versionInfo);
                    scriptID.read(in);
                    baseClass.scriptID(scriptID);
                }

                UnityHash128 oldTypeHash = new UnityHash128(versionInfo);
                oldTypeHash.read(in);
                baseClass.oldTypeHash(oldTypeHash);
                
                if (embedded) {
                    TypeNode node = new TypeNode();
                    readTypeNode(in, node, stInt);
                    baseClass.typeTree(node);
                }
                
                typeMap.put(classID, baseClass);
            }
        } else {
            int numBaseClasses = in.readInt();
            for (int i = 0; i < numBaseClasses; i++) {
                int classID = in.readInt();

                BaseClass baseClass = new BaseClass();
                baseClass.classID(classID);
                
                TypeNode typeNode = new TypeNode();
                readTypeNodeOld(in, typeNode, 0);
                baseClass.typeTree(typeNode);
                
                typeMap.put(classID, baseClass);
            }
            
            embedded = numBaseClasses > 0;

            // padding
            if (versionInfo.assetVersion() > 6) {
                in.readInt();
            }
        }
    }
    
    private void readTypeNode(DataReader in, TypeNode node, StringTable stInt) throws IOException {
        int numFields = in.readInt();
        int stringTableLen = in.readInt();

        // read types
        List<Type> types = new ArrayList<>(numFields);
        for (int j = 0; j < numFields; j++) {
            Type type = new Type(versionInfo);
            type.read(in);
            types.add(type);
        }

        // read string table
        byte[] stringTable = new byte[stringTableLen];
        in.readBytes(stringTable);

        // assign strings
        StringTable stExt = new StringTable();
        stExt.loadStrings(stringTable);
        for (Type field : types) {
            int nameOffset = field.nameOffset();
            String name = stExt.getString(nameOffset);
            if (name == null) {
                name = stInt.getString(nameOffset);
            }
            field.fieldName(name);
            
            int typeOffset = field.typeOffset();
            String type = stExt.getString(typeOffset);
            if (type == null) {
                type = stInt.getString(typeOffset);
            }
            field.typeName(type);
        }
        
        // convert list to tree structure
        TypeNode currentNode = null;
        for (Type type : types) {
            if (currentNode == null) {
                node.type(type);
                currentNode = node;
                continue;
            }
            
            int treeLevel = type.treeLevel();
            int currentTreeLevel = currentNode.type().treeLevel();
            
            TypeNode childNode = new TypeNode();
            childNode.type(type);
            
            currentNode.add(childNode);
            
            if (treeLevel > currentTreeLevel) {
                // move one level up
                currentNode = childNode;
            } else if (treeLevel < currentTreeLevel) {
                // move levels down
                for (; treeLevel < currentTreeLevel; currentTreeLevel--) {
                    currentNode = currentNode.parent();
                }
            }
        }
    }
    
    private void readTypeNodeOld(DataReader in, TypeNode node, int level) throws IOException {
        Type type = new Type(versionInfo);
        type.read(in);
        type.treeLevel(level);
        
        node.type(type);
        
        int numChildren = in.readInt();
        for (int i = 0; i < numChildren; i++) {
            TypeNode childNode = new TypeNode();
            readTypeNodeOld(in, childNode, level + 1);
            node.add(childNode);
        }        
    }
    
    @Override
    public void write(DataWriter out) throws IOException {
        // revision/version for newer formats
        if (versionInfo.assetVersion() > 6) {
            out.writeStringNull(versionInfo.unityRevision().toString());
            out.writeInt(attributes);
        }
        
        if (versionInfo.assetVersion() > 13) {
            // TODO
            throw new UnsupportedOperationException();
        } else {
            int numBaseClasses = typeMap.size();
            out.writeInt(numBaseClasses);

            for (BaseClass bc : typeMap.values()) {
                int classID = bc.classID();
                out.writeInt(classID);

                TypeNode node = bc.typeTree();
                writeFieldTypeNodeOld(out, node);
            }

            // padding
            if (versionInfo.assetVersion() > 6) {
                out.writeInt(0);
            }
        }
    }
    
    private void writeFieldTypeNodeOld(DataWriter out, TypeNode node) throws IOException {
        Type type = node.type();
        type.write(out);
        
        int numChildren = node.size();
        out.writeInt(numChildren);
        for (TypeNode childNode : node) {
            writeFieldTypeNodeOld(out, childNode);
        }
    }
}
