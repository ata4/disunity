/*
 ** 2014 July 10
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.extract.mesh;

import info.ata4.unity.serdes.UnityObject;

// ChannelInfo
//   UInt8 stream
//   UInt8 offset
//   UInt8 format
//   UInt8 dimension
class ChannelInfo {
    
    // Index into m_Streams
    final Integer stream;
    // Vertex chunk offset
    final Integer offset;
    // 0 = full precision, 1 = half precision
    final Integer format;
    // Number of fields?
    final Integer dimension;

    ChannelInfo(UnityObject obj) {
        stream = obj.getValue("stream");
        offset = obj.getValue("offset");
        format = obj.getValue("format");
        dimension = obj.getValue("dimension");
    }
    
}
