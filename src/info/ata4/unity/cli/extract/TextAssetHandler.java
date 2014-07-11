/*
 ** 2013 July 01
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.extract;

import info.ata4.unity.serdes.UnityObject;
import info.ata4.unity.serdes.UnityString;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TextAssetHandler extends AssetExtractHandler {
    
    public TextAssetHandler(String ext) {
        setOutputFileExtension(ext);
    }

    @Override
    public void extract(UnityObject obj) throws IOException {
        String name = obj.getValue("m_Name");
        UnityString script = obj.getValue("m_Script", false);
        
        setOutputFileName(name);
        writeData(script.getRaw());
    }
}
