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
import info.ata4.io.util.ObjectToString;
import info.ata4.unity.assetbundle.AssetBundleEntry;
import info.ata4.unity.assetbundle.AssetBundleHeader;
import info.ata4.unity.assetbundle.AssetBundleReader;
import info.ata4.unity.assetbundle.AssetBundleWriter;
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
    public void handleBundleFile(Path file) throws IOException {
        PrintWriter out = getOutputWriter();
        try (AssetBundleReader reader = new AssetBundleReader(file)) {
            out.println(ObjectToString.toString(reader.getHeader()));
            
            AssetBundleWriter writer = new AssetBundleWriter();
            for (AssetBundleEntry entry : reader) {
                writer.addEntry(entry);
            }

            AssetBundleHeader headerIn = reader.getHeader();
            AssetBundleHeader headerOut = writer.getHeader();
            headerOut.setSignature(headerIn.getSignature());
            headerOut.setStreamVersion(headerIn.getStreamVersion());
            headerOut.setUnityVersion(headerIn.getUnityVersion());
            headerOut.setUnityRevision(headerIn.getUnityRevision());

            writer.write(outputFile);
            
            out.println(ObjectToString.toString(writer.getHeader()));
        }
    }
    
}
