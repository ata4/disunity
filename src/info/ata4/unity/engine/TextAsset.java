/*
 ** 2014 October 06
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.engine;

import info.ata4.unity.rtti.FieldNode;
import java.nio.ByteBuffer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TextAsset extends UnityObject {

    public TextAsset(FieldNode node) {
        super(node);
    }
    
    public String getScript() {
        return node.getChildValue("m_Script");
    }
    
    public ByteBuffer getScriptRaw() {
        return node.getChildArray("m_Script");
    }
}
