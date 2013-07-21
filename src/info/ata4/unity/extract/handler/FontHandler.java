/*
 ** 2013 June 19
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.extract.handler;

import info.ata4.util.io.ByteBufferInput;
import java.io.DataInput;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FontHandler extends RawHandler {

    @Override
    public String getClassName() {
        return "Font";
    }

    @Override
    public void extract(ByteBuffer bb, int id) throws IOException {
        int format = getAssetFormat().getFormat();
        int size;
        
        String assetName = getAssetName(bb);
        
        if (assetName == null) {
            return;
        }
        
        // TODO: use a proper structure class?
        DataInput in = new ByteBufferInput(bb);
        
        // v8:
        // m_AsciiStartOffset, m_Kerning, m_LineSpacing, m_CharacterSpacing, m_CharacterPadding
        // 
        // v9:
        // m_AsciiStartOffset, m_FontCountX, m_FontCountY, m_Kerning, m_LineSpacing
        in.skipBytes(20);
        
        if (format < 9) {
            // m_PerCharacterKerning
            size = in.readInt();
            in.skipBytes(size * 8);
        }
        
        // m_ConvertCase, m_DefaultMaterial
        in.skipBytes(12);
        
        // m_CharacterRects
        size = in.readInt();
        in.skipBytes(size * 40);
        
        if (format >= 9) {
            // bool "flipped"
            in.skipBytes(size * 4);
        }
        
        // m_Texture
        in.skipBytes(8);
        
        // m_KerningValues
        size = in.readInt();
        in.skipBytes(size * 8);
        
        // m_GridFont/m_PixelScale
        in.skipBytes(4);
        
        // m_FontData
        size = in.readInt();
        
        if (size == 0) {
            // bitmap font, can't extract yet
            return;
        }
        
        ByteBuffer bbFont = bb.slice();
        bbFont.limit(size);
        
        // TODO: detect OpenType fonts and use .otf
        extractToFile(bbFont, id, assetName, "ttf");
    }
    
}
