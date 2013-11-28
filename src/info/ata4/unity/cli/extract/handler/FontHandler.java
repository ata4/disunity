/*
 ** 2013 June 19
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.extract.handler;

import info.ata4.unity.asset.struct.AssetObjectPath;
import info.ata4.unity.serdes.UnityArray;
import info.ata4.unity.serdes.UnityObject;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FontHandler extends ExtractHandler {

    @Override
    public String getClassName() {
        return "Font";
    }

    @Override
    public String getFileExtension() {
        // TODO: detect OpenType fonts and use "otf" in these cases
        return "ttf";
    }

    @Override
    public void extract(AssetObjectPath path, UnityObject obj) throws IOException {
        String name = obj.getValue("m_Name");
        UnityArray fontData = obj.getValue("m_FontData");
        ByteBuffer fontBuffer = fontData.getRaw();
        if (fontBuffer.capacity() > 0) {
            writeFile(fontBuffer, path.pathID, name);
        }
    }
}
