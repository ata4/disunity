/*
 ** 2013 June 19
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
public class FontHandler extends AssetExtractHandler {
    
    @Override
    public void extract(UnityObject obj) throws IOException {
        String name = obj.getValue("m_Name");
        ByteBuffer fontBuffer = obj.getValue("m_FontData");
        if (fontBuffer != null && fontBuffer.capacity() > 0) {
            setOutputFileName(name);
            // TODO: detect OpenType fonts and use "otf" in these cases
            setOutputFileExtension("ttf");
            writeData(fontBuffer);
        }
    }
}
