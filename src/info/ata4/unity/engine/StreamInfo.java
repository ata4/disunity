/*
 ** 2014 July 10
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.engine;

import info.ata4.unity.rtti.FieldNode;
        
// StreamInfo (Unity 4)
//   unsigned int channelMask
//   unsigned int offset
//   UInt8 stride
//   UInt8 dividerOp
//   UInt16 frequency

// StreamInfo (Unity 3)
//   UInt32 channelMask
//   UInt32 offset
//   UInt32 stride
//   UInt32 align

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class StreamInfo extends UnityObject {

    public StreamInfo(FieldNode node) {
        super(node);
    }
    
    public Number getChannelMask() {
        return node.getChildValue("channelMask");
    }
    
    public Number getOffset() {
        return node.getChildValue("offset");
    }
    
    public Number getStride() {
        return node.getChildValue("stride");
    }
    
    public Number getDividerOp() {
        return node.getChildValue("dividerOp");
    }
    
    public Number getFrequency() {
        return node.getChildValue("frequency");
    }
    
    public Number getAlign() {
        return node.getChildValue("align");
    }
}
