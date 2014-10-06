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

// ChannelInfo
//   UInt8 stream
//   UInt8 offset
//   UInt8 format
//   UInt8 dimension

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ChannelInfo extends UnityObject {

    public ChannelInfo(FieldNode node) {
        super(node);
    }

    // Index into m_Streams
    public Integer getStream() {
        return node.getChildValue("stream");
    }
    
    // Vertex chunk offset
    public Integer getOffset() {
        return node.getChildValue("offset");
    }
    
    // 0 = full precision, 1 = half precision
    public Integer getFormat() {
        return node.getChildValue("format");
    }
    
    // Number of fields?
    public Integer getDimension() {
        return node.getChildValue("dimension");
    }
}
