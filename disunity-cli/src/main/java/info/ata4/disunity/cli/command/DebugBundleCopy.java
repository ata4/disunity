/*
 ** 2014 December 18
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import info.ata4.disunity.cli.converters.PathConverter;
import info.ata4.unity.assetbundle.AssetBundleEntry;
import info.ata4.unity.assetbundle.AssetBundleHeader;
import info.ata4.unity.assetbundle.AssetBundleReader;
import info.ata4.unity.assetbundle.AssetBundleWriter;
import info.ata4.util.ObjectDump;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandNames = "debug-bundle-copy",
    commandDescription = "Copies an asset bundle to test format reading and writing."
)
public class DebugBundleCopy extends BundleFileCommand {
    
    @Parameter(
        names = {"-o", "--output"},
        description = "Output file",
        required = true,
        converter = PathConverter.class
    )
    private Path outputFile;

    @Override
    public void handleBundleFile(AssetBundleReader reader) throws IOException {
        PrintWriter out = getOutputWriter();

        out.println(ObjectDump.toString(reader.header()));

        AssetBundleWriter writer = new AssetBundleWriter();
        for (AssetBundleEntry entry : reader) {
            writer.addEntry(entry);
        }

        AssetBundleHeader headerIn = reader.header();
        AssetBundleHeader headerOut = writer.getHeader();
        headerOut.signature(headerIn.signature());
        headerOut.streamVersion(headerIn.streamVersion());
        headerOut.unityVersion(headerIn.unityVersion());
        headerOut.unityRevision(headerIn.unityRevision());

        writer.write(outputFile);

        out.println(ObjectDump.toString(writer.getHeader()));
    }
}
