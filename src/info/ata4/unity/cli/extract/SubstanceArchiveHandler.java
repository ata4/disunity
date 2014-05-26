/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.extract;

import info.ata4.unity.serdes.UnityObject;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class SubstanceArchiveHandler extends AssetExtractHandler {

    public SubstanceArchiveHandler() {
        setOutputFileExtension("sbsar");
    }

    @Override
    public void extract(UnityObject obj) throws IOException {
        String name = obj.getValue("m_Name");
        ByteBuffer packageBuffer = obj.getValue("m_PackageData");
        
        setOutputFileName(name);
        writeData(packageBuffer);
    }
}
