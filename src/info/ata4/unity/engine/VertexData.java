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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

// VertexData (Unity 4)
//   unsigned int m_CurrentChannels
//   unsigned int m_VertexCount
//   vector m_Channels
//   vector m_Streams
//   TypelessData m_DataSize

// VertexData (Unity 3)
//    UInt32 m_CurrentChannels
//    UInt32 m_VertexCount
//    StreamInfo m_Streams[0]
//    StreamInfo m_Streams[1]
//    StreamInfo m_Streams[2]
//    StreamInfo m_Streams[3]
//    TypelessData m_DataSize

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class VertexData extends UnityObject {

    public VertexData(FieldNode node) {
        super(node);
    }

    public Long getCurrentChannels() {
        return node.getChildValue("m_CurrentChannels");
    }

    public Long getVertexCount() {
        return node.getChildValue("m_VertexCount");
    }

    public List<ChannelInfo> getChannels() {
        FieldNode channels = node.getChild("m_Channels");
        List<ChannelInfo> channelsList = new ArrayList<>();
        
        if (channels != null) {
            for (FieldNode channel : channels) {
                channelsList.add(new ChannelInfo(channel));
            }
        }
        
        return channelsList;
    }

    public List<StreamInfo> getStreams() {        
        FieldNode streams = node.getChild("m_Streams");
        List<StreamInfo> streamsList = new ArrayList<>();
        
        if (streams != null) {
            for (FieldNode stream : streams) {
                streamsList.add(new StreamInfo(stream));
            }
        } else {
            // older format with array indices as field names
            for (int i = 0; i < 4; i++) {
                FieldNode stream = node.getChild("m_Streams[" + i + "]");
                if (stream != null) {
                    streamsList.add(new StreamInfo(stream));
                }
            }
        }
        
        return streamsList;
    }

    public ByteBuffer getDataSize() {
        return node.getChildValue("m_DataSize");
    }
}
