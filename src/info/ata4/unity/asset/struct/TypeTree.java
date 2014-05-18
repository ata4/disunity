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
import info.ata4.unity.util.UnityVersion;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Class that holds the runtime type information of an asset file.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TypeTree implements Struct {

    private final Map<Integer, TypeField> classes = new LinkedHashMap<>();
    private UnityVersion engineVersion;
    private int treeVersion;
    private int treeFormat;

    public Map<Integer, TypeField> getFields() {
        return classes;
    }
    
    public UnityVersion getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(UnityVersion engineVersion) {
        this.engineVersion = engineVersion;
    }

    public int getVersion() {
        return treeVersion;
    }

    public void setVersion(int version) {
        this.treeVersion = version;
    }
    
    public int getFormat() {
        return treeFormat;
    }

    public void setFormat(int format) {
        this.treeFormat = format;
    }

    @Override
    public void read(DataInputReader in) throws IOException {
        // revision/version for newer formats
        if (treeFormat >= 7) {
            engineVersion = new UnityVersion(in.readStringNull(255));
            treeVersion = in.readInt();
        }
        
        // older formats use big endian
        if (treeFormat <= 5) {
            in.setSwap(false);
        }
        
        int fields = in.readInt();
        for (int i = 0; i < fields; i++) {
            int classID = in.readInt();

            TypeField fn = new TypeField();
            fn.read(in);
            
            classes.put(classID, fn);
        }
        
        // padding
        if (treeFormat >= 7) {
            in.readInt();
        }
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        // revision/version for newer formats
        if (treeFormat >= 7) {
            out.writeStringNull(engineVersion.toString());
            out.writeInt(treeVersion);
        }
        
        // older formats use big endian
        if (treeFormat <= 5) {
            out.setSwap(false);
        }
        
        if (classes.isEmpty()) {
            int fields = classes.size();
            out.writeInt(fields);

            for (Map.Entry<Integer, TypeField> entry : classes.entrySet()) {
                int classID = entry.getKey();
                out.writeInt(classID);
                
                TypeField fn = entry.getValue();
                fn.write(out);
            }
        } else {
            out.writeInt(0);
        }
        
        // padding
        if (treeFormat >= 7) {
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
        final TypeTree other = (TypeTree) obj;
        if (!Objects.equals(this.classes, other.classes)) {
            return false;
        }
        if (!Objects.equals(this.engineVersion, other.engineVersion)) {
            return false;
        }
        if (this.treeVersion != other.treeVersion) {
            return false;
        }
        if (this.treeFormat != other.treeFormat) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.classes);
        hash = 29 * hash + Objects.hashCode(this.engineVersion);
        hash = 29 * hash + this.treeVersion;
        hash = 29 * hash + this.treeFormat;
        return hash;
    }
}