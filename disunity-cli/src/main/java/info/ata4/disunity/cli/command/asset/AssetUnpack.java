/*
 ** 2015 December 01
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.command.asset;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.io.util.PathUtils;
import info.ata4.junity.serialize.SerializedFile;
import info.ata4.junity.serialize.SerializedObjectData;
import info.ata4.log.LogUtils;
import info.ata4.util.io.DataBlock;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandDescription = "Split asset file into data blocks."
)
public class AssetUnpack extends AssetCommand {

    private static final Logger L = LogUtils.getLogger();

    @Parameter(
        names = {"-l", "--level"},
        description = "Unpacking level"
    )
    private int level = 1;

    @Override
    protected void runSerializedFile(Path file, SerializedFile asset) {
        try {
            Path outputDir = PathUtils.removeExtension(file);

            if (Files.isRegularFile(outputDir)) {
                outputDir = PathUtils.append(outputDir, "_");
            }

            if (Files.notExists(outputDir)) {
                Files.createDirectory(outputDir);
            }

            try (FileChannel fc = FileChannel.open(file)) {
                DataBlock headerBlock = asset.headerBlock();
                Path headerFile = outputDir.resolve("header.block");
                copyBlock(headerBlock, headerFile, fc);

                if (level > 0) {
                    DataBlock typeTreeBlock = asset.metadata().typeTreeBlock();
                    Path typeTreeFile = outputDir.resolve("type_tree.block");
                    copyBlock(typeTreeBlock, typeTreeFile, fc);

                    DataBlock objectInfoBlock = asset.metadata().objectInfoBlock();
                    Path objectInfoFile = outputDir.resolve("object_info.block");
                    copyBlock(objectInfoBlock, objectInfoFile, fc);

                    DataBlock objectIDBlock = asset.metadata().objectIDBlock();
                    if (objectIDBlock.length() > 0) {
                        Path objectIDFile = outputDir.resolve("object_ids.block");
                        copyBlock(objectIDBlock, objectIDFile, fc);
                    }

                    DataBlock fileIdentBlock = asset.metadata().externalsBlock();
                    Path fileIdentFile = outputDir.resolve("linked_files.block");
                    copyBlock(fileIdentBlock, fileIdentFile, fc);
                } else {
                    DataBlock metadataBlock = asset.metadataBlock();
                    Path metadataFile = outputDir.resolve("metadata.block");
                    copyBlock(metadataBlock, metadataFile, fc);
                }

                if (level < 2) {
                    DataBlock objectDataBlock = asset.objectDataBlock();
                    Path objectDataFile = outputDir.resolve("object_data.block");
                    copyBlock(objectDataBlock, objectDataFile, fc);
                }
            }

            if (level > 1) {
                Path objectDataDir = outputDir.resolve("object_data");

                if (Files.notExists(objectDataDir)) {
                    Files.createDirectory(objectDataDir);
                }

                for (SerializedObjectData od : asset.objectData()) {
                    String objectDataName = String.format("%010d", od.id());
                    Path objectDataFile = objectDataDir.resolve(objectDataName + ".block");
                    ByteBuffer objectDataBuffer = od.buffer();
                    objectDataBuffer.rewind();

                    ByteBufferUtils.save(objectDataFile, objectDataBuffer);
                }
            }
        } catch (IOException ex) {
            L.log(Level.WARNING, "Can't unpack asset file " + file, ex);
        }
    }

    private void copyBlock(DataBlock block, Path file, FileChannel fc) throws IOException {
        try (FileChannel fcOut = FileChannel.open(file, CREATE, WRITE)) {
            fc.transferTo(block.offset(), block.length(), fcOut);
        }
    }
}
