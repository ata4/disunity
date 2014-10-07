/*
 ** 2013 June 19
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.extract;

import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.unity.engine.Font;
import info.ata4.unity.rtti.ObjectData;
import java.nio.ByteBuffer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FontHandler extends AbstractObjectExtractor {
    
    public FontHandler() {
        super("Font");
    }

    @Override
    public void process(ObjectData object) {
        Font font = new Font(object.getInstance());
        ByteBuffer fontData = font.getFontData();

        if (ByteBufferUtils.isEmpty(fontData)) {
            // don't write log message, this seems to be quite common
            return;
        }
        
        files.add(new MutableFileHandle(font.getName(), "ttf", fontData));
    }
}
