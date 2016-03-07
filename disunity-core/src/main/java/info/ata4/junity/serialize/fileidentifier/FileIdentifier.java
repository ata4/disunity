/*
 ** 2013 July 12
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.serialize.fileidentifier;

import info.ata4.io.Struct;
import info.ata4.junity.UnityGUID;
import java.util.UUID;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
  * @unity FileIdentifier
 */
public abstract class FileIdentifier implements Struct {

    // Globally unique identifier of the referred asset. Unity displays these
    // as simple 16 byte hex strings with each byte swapped, but they can also
    // be represented according to the UUID standard.
    protected final UnityGUID guid = new UnityGUID();

    // Path to the asset file. Only used if "type" is 0.
    protected String filePath;

    // Reference type. Possible values are probably 0 to 3.
    protected int type;

    public UUID guid() {
        return guid.uuid();
    }

    public void guid(UUID guid) {
        this.guid.uuid(guid);
    }

    public String filePath() {
        return filePath;
    }

    public void filePath(String filePath) {
        this.filePath = filePath;
    }

    public int type() {
        return type;
    }

    public void type(int type) {
        this.type = type;
    }
}
