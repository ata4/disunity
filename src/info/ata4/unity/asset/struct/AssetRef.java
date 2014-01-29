/*
 ** 2013 July 12
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
import java.util.UUID;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetRef implements Struct {
    
    // Globally unique identifier of the referred asset. Unity displays these
    // as simple 16 byte hex strings with each byte swapped, but they can also
    // be represented according to the UUID standard.
    private UUID guid;
    
    // Path to the asset file. Only used if "type" is 0.
    private String filePath;
    
    // Path to the asset file? Unused in asset format >= 7.
    private String assetPath;
    
    // Reference type. Possible values are probably 0 to 3.
    private int type;

    @Override
    public void read(DataInputReader in) throws IOException {
        // read GUID as big-endian
        boolean swap = in.isSwap();
        in.setSwap(false);
        long guidMost = in.readLong();
        long guidLeast = in.readLong();
        in.setSwap(swap);
        
        guid = new UUID(guidMost, guidLeast);
        type = in.readInt();
        filePath = in.readStringNull();
        assetPath = in.readStringNull();
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        // write GUID as big-endian
        boolean swap = out.isSwap();
        out.setSwap(false);
        out.writeLong(guid.getMostSignificantBits());
        out.writeLong(guid.getLeastSignificantBits());
        out.setSwap(swap);
        
        out.writeInt(type);
        out.writeStringNull(filePath);
        out.writeStringNull(assetPath);
    }

    public UUID getGUID() {
        return guid;
    }

    public void setGUID(UUID guid) {
        this.guid = guid;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getAssetPath() {
        return assetPath;
    }

    public void setAssetPath(String assetPath) {
        this.assetPath = assetPath;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
