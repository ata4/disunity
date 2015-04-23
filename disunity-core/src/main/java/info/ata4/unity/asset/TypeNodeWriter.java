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

import info.ata4.io.DataWriter;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TypeNodeWriter {
    
    private final VersionInfo versionInfo;

    public TypeNodeWriter(VersionInfo versionInfo) {
        this.versionInfo = versionInfo;
    }
    
    public void write(DataWriter out, TypeNode node) throws IOException {
        if (versionInfo.assetVersion() > 13) {
            // TODO
            throw new UnsupportedOperationException();
        } else {
            writeOld(out, node);
        }
    }
    
    private void writeOld(DataWriter out, TypeNode node) throws IOException {
        Type type = node.type();
        type.write(out);
        
        int numChildren = node.size();
        out.writeInt(numChildren);
        for (TypeNode childNode : node) {
            writeOld(out, childNode);
        }
    }
}
