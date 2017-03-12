/*
 ** 2015 November 27
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.serialize;

import info.ata4.io.DataReader;
import info.ata4.io.DataReaders;
import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.junity.serialize.objectinfo.ObjectInfo;
import info.ata4.junity.serialize.typetree.Type;
import info.ata4.junity.serialize.typetree.TypeRoot;
import info.ata4.log.LogUtils;
import info.ata4.util.io.DataBlock;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.READ;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class SerializedFileReader implements Closeable {

    private static final Logger L = LogUtils.getLogger();

    private final DataReader in;
    private SerializedFile serialized;
    private ByteBuffer resourceBuffer;

    public SerializedFileReader(Path file) throws IOException {
        String fileName = file.getFileName().toString();
        String fileExt = FilenameUtils.getExtension(fileName);

        // load audio buffer if existing
        readResourceStream(file.resolveSibling(fileName + ".streamingResourceImage"));
        readResourceStream(file.resolveSibling(fileName + ".resS"));

        // join split serialized files before loading
        if (fileExt.startsWith("split")) {
            L.fine("Found split serialized file");

            fileName = FilenameUtils.removeExtension(fileName);
            List<Path> parts = new ArrayList<>();
            int splitIndex = 0;

            // collect all files with .split0 to .splitN extension
            while (true) {
                String splitName = String.format("%s.split%d", fileName, splitIndex);
                Path part = file.resolveSibling(splitName);
                if (Files.notExists(part)) {
                    break;
                }

                L.log(Level.FINE, "Adding splinter {0}", part.getFileName());

                splitIndex++;
                parts.add(part);
            }

            // load all parts to one byte buffer
            in = DataReaders.forByteBuffer(ByteBufferUtils.load(parts));
        } else {
            in = DataReaders.forFile(file, READ);
        }
    }

    public SerializedFileReader(DataReader in) {
        this.in = in;
    }

    public SerializedFile read() throws IOException {
        this.serialized = new SerializedFile();

        // header is always big endian
        in.order(ByteOrder.BIG_ENDIAN);

        readHeader(in);

        SerializedFileHeader header = serialized.header();

        // older formats store the object data before the structure data
        if (header.version() < 9) {
            in.position(header.fileSize() - header.metadataSize() + 1);
        }

        // newer formats use little endian for the rest of the file
        if (header.version() > 5) {
            in.order(ByteOrder.LITTLE_ENDIAN);
        }

        readMetadata(in);
        readObjects(in);

        serialized.audioBuffer(resourceBuffer);

        return serialized;
    }

    private void readHeader(DataReader in) throws IOException {
        DataBlock headerBlock = serialized.headerBlock();
        headerBlock.markBegin(in);
        in.readStruct(serialized.header());
        headerBlock.markEnd(in);
        L.log(Level.FINER, "headerBlock: {0}", headerBlock);
    }

    private void readMetadata(DataReader in) throws IOException {
        SerializedFileMetadata metadata = serialized.metadata();
        SerializedFileHeader header = serialized.header();

        DataBlock metadataBlock = serialized.metadataBlock();
        metadataBlock.markBegin(in);
        metadata.version(header.version());
        in.readStruct(metadata);
        metadataBlock.markEnd(in);
        L.log(Level.FINER, "metadataBlock: {0}", metadataBlock);
    }

    private void readObjects(DataReader in) throws IOException {
        long ofsMin = Long.MAX_VALUE;
        long ofsMax = Long.MIN_VALUE;

        SerializedFileHeader header = serialized.header();
        SerializedFileMetadata metadata = serialized.metadata();

        Map<Long, ObjectInfo> objectInfoMap = metadata.objectInfoTable().infoMap();
        Map<Integer, TypeRoot<Type>> typeTreeMap = metadata.typeTree().typeMap();
        List<SerializedObjectData> objectData = serialized.objectData();

        for (Map.Entry<Long, ObjectInfo> infoEntry : objectInfoMap.entrySet()) {
            ObjectInfo info = infoEntry.getValue();
            long id = infoEntry.getKey();
            long ofs = header.dataOffset() + info.offset();

            ofsMin = Math.min(ofsMin, ofs);
            ofsMax = Math.max(ofsMax, ofs + info.length());

            SerializedObjectData object = new SerializedObjectData(id);
            object.info(info);

            // create and read object data buffer
            ByteBuffer buf = ByteBufferUtils.allocate((int) info.length());

            in.position(ofs);
            in.readBuffer(buf);

            object.buffer(buf);

            // get type tree if possible
            TypeRoot typeRoot = typeTreeMap.get(info.typeID());
            if (typeRoot != null) {
                object.typeTree(typeRoot.nodes());
            }

            objectData.add(object);
        }

        DataBlock objectDataBlock = serialized.objectDataBlock();
        objectDataBlock.offset(ofsMin);
        objectDataBlock.endOffset(ofsMax);
        L.log(Level.FINER, "objectDataBlock: {0}", objectDataBlock);
    }

    private void readResourceStream(Path streamFile) throws IOException {
        if (Files.exists(streamFile)) {
            L.log(Level.FINE, "Found resource stream file {0}", streamFile.getFileName());
            resourceBuffer = ByteBufferUtils.openReadOnly(streamFile);
        }
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
