/*
 ** 2015 November 23
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.serialize;

import info.ata4.util.io.DataBlock;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 * @unity SerializedFile
 */
public class SerializedFile {

    // struct fields
    private final SerializedFileHeader header = new SerializedFileHeader();
    private final SerializedFileMetadata metadata = new SerializedFileMetadata();

    // data block fields
    private final DataBlock headerBlock = new DataBlock();
    private final DataBlock metadataBlock = new DataBlock();
    private final DataBlock objectDataBlock = new DataBlock();

    // misc fields
    private final List<SerializedObjectData> objectData = new ArrayList<>();
    private ByteBuffer audioBuffer;

    public SerializedFileHeader header() {
        return header;
    }

    public SerializedFileMetadata metadata() {
        return metadata;
    }

    public List<SerializedObjectData> objectData() {
        return objectData;
    }

    public DataBlock headerBlock() {
        return headerBlock;
    }

    public DataBlock metadataBlock() {
        return metadataBlock;
    }

    public DataBlock objectDataBlock() {
        return objectDataBlock;
    }

    public List<DataBlock> dataBlocks() {
        List<DataBlock> blocks = new ArrayList<>();
        blocks.add(headerBlock);
        blocks.addAll(metadata().dataBlocks());
        return blocks;
    }

    public ByteBuffer audioBuffer() {
        return audioBuffer;
    }

    public void audioBuffer(ByteBuffer audioBuffer) {
        this.audioBuffer = audioBuffer;
    }
}
