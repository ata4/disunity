/*
 ** 2015 December 06
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.test.junity;

import com.google.common.io.CountingOutputStream;
import info.ata4.junity.bundle.Bundle;
import info.ata4.junity.bundle.BundleHeader;
import info.ata4.junity.bundle.BundleReader;
import info.ata4.test.ParameterizedUtils;
import static info.ata4.util.function.IOConsumer.uncheck;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@RunWith(Parameterized.class)
public class BundleTest {

    @Parameterized.Parameters
    public static List<Path[]> data() throws IOException {
        List<Path[]> params = new ArrayList<>();

        // public set
        Path bundleDir = Paths.get("src", "test", "resources", "bundle");
        params.addAll(ParameterizedUtils.getPathParameters(bundleDir));

        return params;
    }

    private final Path file;
    private Bundle bundle;
    private final BundleReader reader;

    public BundleTest(Path file) throws IOException {
        this.file = file;
        this.reader = new BundleReader(file);
        System.out.println(file);
    }

    @Before
    public void setUp() throws IOException {
        bundle = reader.read();
    }

    @After
    public void tearDown() throws IOException {
        reader.close();
    }

    @Test
    public void headerValid() throws IOException {
        BundleHeader header = bundle.header();
        long fileSize = Files.size(file);

        assertTrue("Number of levels to download must be equal to number of levels or 1",
                header.numberOfLevelsToDownload() == header.numberOfLevels() || header.numberOfLevelsToDownload() == 1);

        assertTrue("Signatures should be valid", header.hasValidSignature());

        assertTrue("Minimum streamed bytes must be smaller than or equal to file size",
                header.minimumStreamedBytes() <= fileSize);

        assertTrue("Header size must be smaller than file size",
                header.headerSize() < fileSize);

        if (header.streamVersion() >= 2) {
            assertEquals("Header file size and actual file size must be equal",
                    header.completeFileSize(), fileSize);
        }

        assertEquals("Number of levels must match number of level end offsets",
                header.numberOfLevelsToDownload(), header.levelByteEnd().size());

        header.levelByteEnd().forEach(lbe -> {
            assertTrue("Compressed offset must be smaller or equal to uncompressed offset",
                    lbe.getLeft() <= lbe.getRight());
        });
    }

    @Test
    public void entriesValid() {
        assertEquals("Bundle entry lists must match in size",
                bundle.entries().size(), bundle.entryInfos().size());

        bundle.entries().forEach(uncheck(entry -> {
            CountingOutputStream cos = new CountingOutputStream(new NullOutputStream());
            IOUtils.copy(entry.inputStream(), cos);
            assertEquals("Entry size must match size of InputStream", entry.size(), cos.getCount());
        }));
    }
}
