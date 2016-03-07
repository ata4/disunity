/*
 ** 2014 December 03
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.bundle;

import info.ata4.io.DataWriter;
import info.ata4.io.DataWriters;
import info.ata4.io.lzma.LzmaEncoderProps;
import info.ata4.junity.progress.Progress;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.contrapunctus.lzma.LzmaOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.MutablePair;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BundleWriter implements Closeable {

    private final DataWriter out;
    private final Map<BundleEntry, MutablePair<Long, Long>> levelOffsetMap = new LinkedHashMap<>();
    private final Path dataFile;
    private Bundle bundle;

    public BundleWriter(Path file) throws IOException {
        out = DataWriters.forFile(file, CREATE, WRITE, TRUNCATE_EXISTING);
        dataFile = Files.createTempFile(file.getParent(), "uncompressedData", null);
    }

    public void write(Bundle bundle, Progress progress) throws IOException {
        this.bundle = bundle;

        // add offset placeholders
        levelOffsetMap.clear();
        bundle.entries().stream()
            .filter(entry -> {
                if (bundle.entries().size() == 1) {
                    return true;
                }
                String name = entry.name();
                return name.equals("mainData") || name.startsWith("level");
            })
            .forEach(entry -> levelOffsetMap.put(entry, new MutablePair<>(0L, 0L)));

        BundleHeader header = bundle.header();
        header.levelByteEnd().clear();
        header.levelByteEnd().addAll(levelOffsetMap.values());
        header.numberOfLevelsToDownload(levelOffsetMap.size());

        // write header
        out.writeStruct(header);
        header.headerSize((int) out.position());

        // write bundle data
        if (header.compressed()) {
            // write data to temporary file
            try (DataWriter outData = DataWriters.forFile(dataFile,
                        CREATE, WRITE, TRUNCATE_EXISTING)) {
                writeData(outData, progress);
            }

            // configure LZMA encoder
            LzmaEncoderProps props = new LzmaEncoderProps();
            props.setDictionarySize(1 << 23); // 8 MiB
            props.setNumFastBytes(273); // maximum
            props.setUncompressedSize(Files.size(dataFile));
            props.setEndMarkerMode(true);

            // stream the temporary bundle data compressed into the bundle file
            try (OutputStream os = new LzmaOutputStream(new BufferedOutputStream(out.stream()), props)) {
                Files.copy(dataFile, os);
            }

            for (MutablePair<Long, Long> levelOffset : levelOffsetMap.values()) {
                levelOffset.setLeft(out.size());
            }
        } else {
            // write data directly to file
            writeData(out, progress);
        }

        // update header
        int fileSize = (int) out.size();
        header.completeFileSize(fileSize);
        header.minimumStreamedBytes(fileSize);

        out.position(0);
        out.writeStruct(header);
    }

    private void writeData(DataWriter out, Progress progress) throws IOException {
        // write entry list
        List<BundleEntry> entries = bundle.entries();
        long baseOffset = out.position();
        out.writeInt(entries.size());

        List<BundleEntryInfo> entryInfos = new ArrayList<>(entries.size());
        for (BundleEntry entry : entries) {
            BundleEntryInfo entryInfo = new BundleEntryInfo();
            entryInfo.name(entry.name());
            entryInfo.size(entry.size());
            out.writeStruct(entryInfo);
            entryInfos.add(entryInfo);
        }

        // write entry data
        for (int i = 0; i < entries.size(); i++) {
            out.align(4);

            BundleEntry entry = entries.get(i);
            BundleEntryInfo entryInfo = entryInfos.get(i);

            progress.update(Optional.of(entry.name()), i / (double) entries.size());

            entryInfo.offset(out.position() - baseOffset);

            if (i == 0) {
                bundle.header().dataHeaderSize(entryInfo.offset());
            }

            try (
                InputStream is = entry.inputStream();
                OutputStream os = out.stream();
            ) {
                IOUtils.copy(is, os);
            }

            MutablePair<Long, Long> levelOffset = levelOffsetMap.get(entry);
            if (levelOffset != null) {
                long offset = out.position() - baseOffset;
                levelOffset.setLeft(offset);
                levelOffset.setRight(offset);
            }
        }

        // update offsets
        out.position(baseOffset + 4);
        for (BundleEntryInfo entryInfo : entryInfos) {
            out.writeStruct(entryInfo);
        }
    }

    @Override
    public void close() throws IOException {
        out.close();
        Files.deleteIfExists(dataFile);
    }
}
