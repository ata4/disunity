/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.asset;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import info.ata4.unity.util.UnityStruct;
import info.ata4.unity.util.UnityVersion;
import java.io.IOException;
import java.util.Map;

/**
 * Class that holds the runtime type information of an asset file.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @unity RTTIClassHierarchyDescriptor, RTTIBaseClassDescriptor2
 */
public class FieldTypeTree extends UnityStruct {

    private final Map<Integer, FieldTypeNode> typeMap;

    private int attributes;
    
    public FieldTypeTree(Map<Integer, FieldTypeNode> typeMap, VersionInfo versionInfo) {
        super(versionInfo);
        this.typeMap = typeMap;
    }
    
    public int getAttributes() {
        return attributes;
    }

    public void setAttributes(int version) {
        this.attributes = version;
    }

    @Override
    public void read(DataReader in) throws IOException {
        // revision/version for newer formats
        if (versionInfo.getAssetVersion() >= 7) {
            versionInfo.setUnityRevision(new UnityVersion(in.readStringNull(255)));
            attributes = in.readInt();
        }
        
        int numBaseClasses = in.readInt();
        for (int i = 0; i < numBaseClasses; i++) {
            int classID = in.readInt();

            FieldTypeNode node = new FieldTypeNode();
            node.read(in);
            
            typeMap.put(classID, node);
        }
        
        // padding
        if (versionInfo.getAssetVersion() >= 7) {
            in.readInt();
        }
    }

    @Override
    public void write(DataWriter out) throws IOException {
        // revision/version for newer formats
        if (versionInfo.getAssetVersion() >= 7) {
            out.writeStringNull(versionInfo.getUnityRevision().toString());
            out.writeInt(attributes);
        }
        
        int fields = typeMap.size();
        out.writeInt(fields);

        for (Map.Entry<Integer, FieldTypeNode> entry : typeMap.entrySet()) {
            int classID = entry.getKey();
            out.writeInt(classID);

            FieldTypeNode node = entry.getValue();
            node.write(out);
        }
        
        // padding
        if (versionInfo.getAssetVersion() >= 7) {
            out.writeInt(0);
        }
    }
}