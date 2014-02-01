/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.asset.struct;

import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
import info.ata4.io.Struct;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetClassType implements Struct {

    private Map<Integer, AssetFieldType> typeTree = new LinkedHashMap<>();
    private String revision;
    private int version;
    private int format;

    public Map<Integer, AssetFieldType> getTypeTree() {
        return typeTree;
    }
    
    public boolean hasTypeTree() {
        return !typeTree.isEmpty();
    }
    
    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
    
    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    @Override
    public void read(DataInputReader in) throws IOException {
        // revision/version for newer formats
        if (format >= 7) {
            revision = in.readStringNull(255);
            version = in.readInt();
        }
        
        // older formats use big endian
        if (format <= 5) {
            in.setSwap(false);
        }
        
        int fields = in.readInt();
        for (int i = 0; i < fields; i++) {
            int classID = in.readInt();

            AssetFieldType fn = new AssetFieldType();
            fn.read(in);
            
            typeTree.put(classID, fn);
        }
        
        // padding
        if (format >= 7) {
            in.readInt();
        }
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        // revision/version for newer formats
        if (format >= 7) {
            out.writeStringNull(revision);
            out.writeInt(version);
        }
        
        // older formats use big endian
        if (format <= 5) {
            out.setSwap(false);
        }
        
        if (hasTypeTree()) {
            int fields = typeTree.size();
            out.writeInt(fields);

            for (Map.Entry<Integer, AssetFieldType> entry : typeTree.entrySet()) {
                int classID = entry.getKey();
                out.writeInt(classID);
                
                AssetFieldType fn = entry.getValue();
                fn.write(out);
            }
        } else {
            out.writeInt(0);
        }
        
        // padding
        if (format >= 7) {
            out.writeInt(0);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AssetClassType other = (AssetClassType) obj;
        if (!Objects.equals(this.typeTree, other.typeTree)) {
            return false;
        }
        if (!Objects.equals(this.revision, other.revision)) {
            return false;
        }
        if (this.version != other.version) {
            return false;
        }
        if (this.format != other.format) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.typeTree);
        hash = 29 * hash + Objects.hashCode(this.revision);
        hash = 29 * hash + this.version;
        hash = 29 * hash + this.format;
        return hash;
    }
}