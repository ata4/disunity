/*
 ** 2015 December 03
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.serialize;

import info.ata4.io.DataWriter;
import info.ata4.junity.serialize.objectinfo.ObjectInfo;
import info.ata4.log.LogUtils;
import info.ata4.util.io.DataBlock;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class SerializedFileWriter implements Closeable {

    private static final Logger L = LogUtils.getLogger();

    private static final int META_PADDING = 4096;
    private static final int META_ALIGN = 16;

    private final DataWriter out;
    private SerializedFile serialized;

    public SerializedFileWriter(DataWriter out) {
        this.out = out;
    }

    public void write(SerializedFile serialized) throws IOException {
        this.serialized = serialized;

        // header is always big endian
        out.order(ByteOrder.BIG_ENDIAN);

        writeHeader(out);

        SerializedFileHeader header = serialized.header();

        // newer formats use little endian for the rest of the file
        if (header.version() > 5) {
            out.order(ByteOrder.LITTLE_ENDIAN);
        }

        // older formats store the object data before the structure data
        if (header.version() < 9) {
            header.dataOffset(0);

            writeObjects(out);
            out.writeUnsignedByte(header.version() > 5 ? 0 : 1);

            writeMetadata(out);
            out.writeUnsignedByte(0);
        } else {
            writeMetadata(out);

            long dataOffset = out.position();

            // calculate padding
            if (dataOffset < META_PADDING) {
                dataOffset = META_PADDING;
            } else {
                dataOffset += META_ALIGN - (dataOffset % META_ALIGN);
            }

            header.dataOffset(dataOffset);

            out.position(dataOffset);
            writeObjects(out);

            // write updated path table
            out.position(serialized.metadata().objectInfoBlock().offset());
            out.writeStruct(serialized.metadata().objectInfoTable());
        }

        // update header
        header.fileSize(out.size());

        // FIXME: the metadata size is slightly off in comparison to original files
        int metadataOffset = header.version() < 9 ? 2 : 1;

        header.metadataSize(serialized.metadataBlock().length() + metadataOffset);

        // write updated header
        out.order(ByteOrder.BIG_ENDIAN);
        out.position(serialized.headerBlock().offset());
        out.writeStruct(header);
    }

    private void writeHeader(DataWriter out) throws IOException {
        DataBlock headerBlock = serialized.headerBlock();
        headerBlock.markBegin(out);
        out.writeStruct(serialized.header());
        headerBlock.markEnd(out);
        L.log(Level.FINER, "headerBlock: {0}", headerBlock);
    }

    private void writeMetadata(DataWriter out) throws IOException {
        SerializedFileMetadata metadata = serialized.metadata();
        SerializedFileHeader header = serialized.header();

        DataBlock metadataBlock = serialized.metadataBlock();
        metadataBlock.markBegin(out);
        metadata.version(header.version());
        out.writeStruct(metadata);
        metadataBlock.markEnd(out);
        L.log(Level.FINER, "metadataBlock: {0}", metadataBlock);
    }

    private void writeObjects(DataWriter out) throws IOException {
        long ofsMin = Long.MAX_VALUE;
        long ofsMax = Long.MIN_VALUE;

        for (SerializedObjectData data : serialized.objectData()) {
            ByteBuffer bb = data.buffer();
            bb.rewind();

            out.align(8);

            ofsMin = Math.min(ofsMin, out.position());
            ofsMax = Math.max(ofsMax, out.position() + bb.remaining());

            ObjectInfo info = data.info();
            info.offset(out.position() - serialized.header().dataOffset());
            info.length(bb.remaining());

            out.writeBuffer(bb);
        }

        DataBlock objectDataBlock = serialized.objectDataBlock();
        objectDataBlock.offset(ofsMin);
        objectDataBlock.endOffset(ofsMax);
        L.log(Level.FINER, "objectDataBlock: {0}", objectDataBlock);
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

}
