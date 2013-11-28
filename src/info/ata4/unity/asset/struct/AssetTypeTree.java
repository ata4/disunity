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

import info.ata4.util.io.DataInputReader;
import info.ata4.util.io.DataOutputWriter;
import info.ata4.util.io.Struct;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetTypeTree extends LinkedHashMap<Integer, AssetFieldType> implements Struct {

    private static final Logger L = Logger.getLogger(AssetTypeTree.class.getName());
    
    public String revision;
    public int version;
    
    private int format;
    private boolean standalone = false;
    
    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }
    
    public boolean isStandalone() {
        return standalone;
    }

    public void setStandalone(boolean standalone) {
        this.standalone = standalone;
    }

    @Override
    public void read(DataInputReader in) throws IOException {
        // TODO: validate
        if (format >= 7) {
            revision = in.readStringNull(255);
            L.log(Level.FINEST, "revision = {0}", revision);

            version = in.readInt();
            L.log(Level.FINEST, "version = {0}", version);
        }
        
        int fields = in.readInt();
        L.log(Level.FINEST, "fields = {0}", fields);
        
        for (int i = 0; i < fields; i++) {
            int classID = in.readInt();
            L.log(Level.FINEST, "classID = {0}", classID);

            AssetFieldType fn = new AssetFieldType();
            fn.read(in);
            
            put(classID, fn);
        }
        
        // TODO: validate
        if (format >= 7) {
            in.readInt(); // padding
        }
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        // TODO: validate
        if (format >= 7) {
            out.writeStringNull(revision);
            L.log(Level.FINEST, "revision = {0}", revision);
            
            out.writeInt(version);
            L.log(Level.FINEST, "version = {0}", version);
        }
        
        if (!standalone) {
            int fields = size();
            out.writeInt(fields);
            L.log(Level.FINEST, "fields = {0}", fields);

            for (Map.Entry<Integer, AssetFieldType> entry : entrySet()) {
                int classID = entry.getKey();
                out.writeInt(classID);
                L.log(Level.FINEST, "classID = {0}", classID);

                AssetFieldType fn = entry.getValue();
                fn.write(out);
            }
        } else {
            out.writeInt(0);
        }
        
        // TODO: validate
        if (format >= 7) {
            out.writeInt(0); // padding
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
        final AssetTypeTree other = (AssetTypeTree) obj;
        if (!Objects.equals(this.revision, other.revision)) {
            return false;
        }
        if (this.version != other.version) {
            return false;
        }
        if (this.format != other.format) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 29 * hash + Objects.hashCode(this.revision);
        hash = 29 * hash + this.version;
        hash = 29 * hash + this.format;
        return hash;
    }
}