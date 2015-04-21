/*
 ** 2014 December 25
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.extract;

import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.log.LogUtils;
import info.ata4.unity.engine.TextAsset;
import info.ata4.unity.rtti.ObjectData;
import info.ata4.unity.util.UnityClass;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TextAssetExtractor extends AbstractAssetExtractor {
    
    private static final Logger L = LogUtils.getLogger();
    
    @Override
    public UnityClass getUnityClass() {
        return new UnityClass("TextAsset");
    }

    @Override
    public void extract(ObjectData objectData) throws IOException {
        TextAsset shader = new TextAsset(objectData.instance());
        String name = shader.getName();
        ByteBuffer script = shader.getScriptRaw();
        
        String ext;
        String assetType;
        
        if (objectData.info().unityClass().name().equals("Shader")) {
            assetType = "Shader";
            ext = "shader";
        } else {
            assetType = "Text asset";
            ext = "txt";
        }
        
        // skip empty buffers
        if (ByteBufferUtils.isEmpty(script)) {
            L.log(Level.WARNING, "{0} {1} is empty", new Object[]{assetType, name});
            return;
        }
        
        writeFile(name, ext, script);
    }
    
}
