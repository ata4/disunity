/*
 ** 2015 April 23
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.asset;

import info.ata4.io.DataReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TypeNodeReader {
    
    private final VersionInfo versionInfo;
    
    public TypeNodeReader(VersionInfo versionInfo) {
        this.versionInfo = versionInfo;
    }
    
    public void read(DataReader in, TypeNode node) throws IOException {
        if (versionInfo.assetVersion() > 13) {
            StringTable stInt = new StringTable();
            readNew(in, node, stInt);
        } else {
            readOld(in, node, 0);
        }
    }
    
    private void readNew(DataReader in, TypeNode node, StringTable stInt) throws IOException {
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
        TypeNode nodePrev = null;
        for (Type type : types) {
            if (nodePrev == null) {
                node.type(type);
                nodePrev = node;
                continue;
            }
            
            TypeNode nodeCurr = new TypeNode();
            nodeCurr.type(type);
            
            int levels = nodePrev.type().treeLevel() - type.treeLevel();
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
    
    private void readOld(DataReader in, TypeNode node, int level) throws IOException {
        Type type = new Type(versionInfo);
        type.read(in);
        type.treeLevel(level);
        
        node.type(type);
        
        int numChildren = in.readInt();
        for (int i = 0; i < numChildren; i++) {
            TypeNode childNode = new TypeNode();
            readOld(in, childNode, level + 1);
            node.add(childNode);
        }        
    }
}
