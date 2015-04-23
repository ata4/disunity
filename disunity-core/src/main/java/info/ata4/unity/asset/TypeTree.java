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
import java.util.Map;

/**
 * Class for objects that hold the runtime type information of an asset file.
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
        
        TypeNodeReader nodeReader = new TypeNodeReader(versionInfo);
        
        // Unity 5+ uses a serialized tree structure and string buffers
        if (versionInfo.assetVersion() > 13) {
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
                    nodeReader.read(in, node);
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
                
                TypeNode node = new TypeNode();
                nodeReader.read(in, node);
                baseClass.typeTree(node);
                
                typeMap.put(classID, baseClass);
            }
            
            embedded = numBaseClasses > 0;

            // padding
            if (versionInfo.assetVersion() > 6) {
                in.readInt();
            }
        }
    }
    
    @Override
    public void write(DataWriter out) throws IOException {
        // revision/version for newer formats
        if (versionInfo.assetVersion() > 6) {
            out.writeStringNull(versionInfo.unityRevision().toString());
            out.writeInt(attributes);
        }
        
        TypeNodeWriter nodeWriter = new TypeNodeWriter(versionInfo);
        
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
                nodeWriter.write(out, node);
            }

            // padding
            if (versionInfo.assetVersion() > 6) {
                out.writeInt(0);
            }
        }
    }
}
