/*
 ** 2013 July 01
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.extract.handler;

import info.ata4.unity.struct.asset.TextAsset;
import info.ata4.util.io.ByteBufferInput;
import info.ata4.util.io.DataInputReader;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TextAssetHandler extends ExtractHandler {

    @Override
    public String getClassName() {
        return "TextAsset";
    }
    
    public String getExtension() {
        return "txt";
    }

    @Override
    public void extract(ByteBuffer bb, int id) throws IOException {
        DataInputReader in = new DataInputReader(new ByteBufferInput(bb));
        
        TextAsset ta = new TextAsset(getAssetFormat());
        ta.read(in);
        
        extractToFile(ta.script, id, ta.name, getExtension());
    }
    
}
