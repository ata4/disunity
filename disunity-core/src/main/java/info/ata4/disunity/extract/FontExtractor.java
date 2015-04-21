/*
 ** 2014 December 27
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.extract;

import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.unity.engine.Font;
import info.ata4.unity.rtti.ObjectData;
import info.ata4.unity.util.UnityClass;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FontExtractor extends AbstractAssetExtractor {
    
    @Override
    public UnityClass getUnityClass() {
        return new UnityClass("Font");
    }

    @Override
    public void extract(ObjectData objectData) throws IOException {
        Font font = new Font(objectData.instance());
        ByteBuffer fontData = font.getFontData();

        if (ByteBufferUtils.isEmpty(fontData)) {
            // don't write log message, this seems to be quite common
            return;
        }
        
        writeFile(font.getName(), "ttf", fontData);
    }
}
