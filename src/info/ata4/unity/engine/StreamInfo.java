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

import info.ata4.unity.serdes.UnityObject;

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

public class StreamInfo {
    
    public final Long channelMask;
    public final Long offset;
    public final Integer stride;
    public final Integer dividerOp;
    public final Integer frequency;
    public final Long align;

    public StreamInfo(UnityObject obj) {
        channelMask = obj.getValue("channelMask");
        offset = obj.getValue("offset");
        stride = obj.getNumber("stride").intValue();
        dividerOp = obj.getNumber("dividerOp").intValue();
        frequency = obj.getNumber("frequency").intValue();
        align = obj.getValue("align");
    }
    
}
