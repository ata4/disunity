/*
 ** 2013 July 01
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.extract;

import info.ata4.unity.engine.TextAsset;
import info.ata4.unity.rtti.ObjectData;
import java.nio.ByteBuffer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TextAssetHandler extends AbstractObjectExtractor {
    
    private final String ext;
    
    public TextAssetHandler(String ext) {
        super("TextAsset");
        this.ext = ext;
    }

    @Override
    public void process(ObjectData object) throws Exception {
        TextAsset text = new TextAsset(object.getInstance());
        String name = text.getName();
        String script = text.getScript();
        
        ByteBuffer scriptData = ByteBuffer.wrap(script.getBytes("UTF-8"));
        
        files.add(new MutableFileHandle(name, ext, scriptData));
    }
}
