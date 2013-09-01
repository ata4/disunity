/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.extract.handler;

import info.ata4.unity.serdes.UnityArray;
import info.ata4.unity.serdes.UnityObject;
import info.ata4.unity.struct.ObjectPath;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class SubstanceArchiveHandler extends ExtractHandler {
    
    @Override
    public String getClassName() {
        return "SubstanceArchive";
    }
    
    public String getExtension() {
        return "sbsar";
    }

    @Override
    public void extract(ObjectPath path, UnityObject obj) throws IOException {
        String name = obj.getValue("m_Name");
        UnityArray packageData = obj.getValue("m_PackageData");
        ByteBuffer packageBuffer = packageData.getRaw();
        
        writeFile(packageBuffer, path.pathID, name);
    }
}
