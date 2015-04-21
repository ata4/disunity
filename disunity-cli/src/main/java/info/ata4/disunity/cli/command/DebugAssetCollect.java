/*
 ** 2014 December 17
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
import info.ata4.io.DataReader;
import info.ata4.io.DataReaders;
import info.ata4.log.LogUtils;
import info.ata4.unity.assetbundle.AssetBundleEntry;
import info.ata4.unity.assetbundle.AssetBundleReader;
import info.ata4.unity.assetbundle.AssetBundleUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

/**
 * Helper that collects mainData files from asset bundles and stores them in
 * a directory. Used in conjunction with debug-asset-test for debugging.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandNames = "debug-asset-collect",
    commandDescription = "Grabs mainData files from asset bundles and copies them to a directory."
)
public class DebugAssetCollect extends BundleFileCommand {
    
    private static final Logger L = LogUtils.getLogger();
    
    @Parameter(
        names = {"-o", "--output"},
        description = "Output directory",
        required = true,
        converter = PathConverter.class
    )
    private Path outputDir;

    @Override
    public void handleBundleFile(AssetBundleReader reader) throws IOException {
        for (AssetBundleEntry entry : reader) {
            if (entry.name().equals("mainData")) {
                addMainData(entry);
                break;
            }
        }
    }
    
    private void addMainData(AssetBundleEntry entry) throws IOException {
        try (DataReader in = AssetBundleUtils.dataReaderForEntry(entry)) {
            String outName = getMD5Checksum(in);
            Path outFile = outputDir.resolve(outName);
            
            in.position(0);
            Files.copy(in.stream(), outFile);
            
            L.log(Level.INFO, "{0} = {1}", new Object[]{outName, getCurrentFile()});
        } catch (NoSuchAlgorithmException ex) {
            L.log(Level.SEVERE, "MD5 is not supported", ex);
        }
    }
    
    private String getMD5Checksum(DataReader in) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (DigestInputStream dis = new DigestInputStream(in.stream(), md)) {
            IOUtils.copy(dis, new NullOutputStream());
        }

        byte[] digest = md.digest();
        return DatatypeConverter.printHexBinary(digest);
    }
}
