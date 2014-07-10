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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

// VertexData
//   unsigned int m_CurrentChannels
//   unsigned int m_VertexCount
//   vector m_Channels
//   vector m_Streams
//   TypelessData m_DataSize
public class VertexData {
    
    public final Long currentChannels;
    public final Long vertexCount;
    public final List<ChannelInfo> channels;
    public final List<StreamInfo> streams;
    public final ByteBuffer dataSize;

    VertexData(UnityObject obj) {
        currentChannels = obj.getValue("m_CurrentChannels");
        vertexCount = obj.getValue("m_VertexCount");
        List<UnityObject> channelObjects = obj.getValue("m_Channels");
        channels = new ArrayList<>();
        for (UnityObject channelObject : channelObjects) {
            channels.add(new ChannelInfo(channelObject));
        }
        List<UnityObject> streamObjects = obj.getValue("m_Streams");
        streams = new ArrayList<>();
        for (UnityObject streamObject : streamObjects) {
            streams.add(new StreamInfo(streamObject));
        }
        dataSize = obj.getValue("m_DataSize");
    }
    
}
