/*
 ** 2013 July 12
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
import info.ata4.io.Struct;
import info.ata4.unity.util.UnityGUID;
import java.io.IOException;
import java.util.UUID;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @unity FileIdentifier 
 */
public class FileIdentifier implements Struct {
    
    private VersionInfo versionInfo;
    
    // Path to the asset file? Unused in asset format <= 5.
    private String assetPath;
    
    // Globally unique identifier of the referred asset. Unity displays these
    // as simple 16 byte hex strings with each byte swapped, but they can also
    // be represented according to the UUID standard.
    private final UnityGUID guid = new UnityGUID();
    
    // Path to the asset file. Only used if "type" is 0.
    private String filePath;
    
    // Reference type. Possible values are probably 0 to 3.
    private int type;
    
    private AssetFile asset;
    
    @Override
    public void read(DataReader in) throws IOException {
        if (versionInfo.getAssetVersion() > 5) {
            assetPath = in.readStringNull();
        }
        
        guid.read(in);
        type = in.readInt();
        filePath = in.readStringNull();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        if (versionInfo.getAssetVersion() > 5) {
            out.writeStringNull(assetPath);
        }
        
        guid.write(out);
        out.writeInt(type);
        out.writeStringNull(filePath);
    }

    public UUID getGUID() {
        return guid.getUUID();
    }

    public void setGUID(UUID guid) {
        this.guid.setUUID(guid);
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

    void setVersionInfo(VersionInfo versionInfo) {
        this.versionInfo = versionInfo;
    }

    public AssetFile getAssetFile() {
        return asset;
    }

    void setAssetFile(AssetFile asset) {
        this.asset = asset;
    }
}
