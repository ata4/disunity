/*
 ** 2014 November 15
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.log.LogUtils;
import info.ata4.unity.asset.bundle.AssetBundle;
import info.ata4.unity.asset.bundle.codec.XianjianCodec;
import info.ata4.unity.cli.converters.PathConverter;
import info.ata4.unity.util.UnityVersion;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Command for compressing existing files into an asset bundle.
 *
 * Warning: When used with -r (recursive), may easily run out of
 * memory if a lot of (large) files are included..
 *
 * @author saneki
 */
@Parameters(
    commandNames = "bundle",
    commandDescription = "Create an asset bundle."
)
public class BundleCmd extends FileCommand {

    private static final Logger L = LogUtils.getLogger();

    @Parameter(
        names = "--format",
        description = "Bundle file format to use"
    )
    private int bundleFormat = 3;

    @Parameter(
        names = "--player",
        description = "Player version to use (2 for 2.x.x, 3 for 3.x.x)"
    )
    private int player = 3;

    @Parameter(
        names = "--engine",
        description = "Engine version to use"
    )
    private String engine = "4.1.5f1"; // I just took this from a UnityWeb file

    @Parameter(
        names = "--no-compress",
        description = "Don't compress the bundle content"
    )
    private boolean noCompress = false;

    @Parameter(
        names = "--xianjian",
        description = "Save using the Xianjian XOR codec"
    )
    private boolean xianjian = false;

    @Parameter(
        names = "--raw-signature",
        description = "Use the UnityRaw signature instead of UnityWeb"
    )
    private boolean rawSignature = false;

    private AssetBundle assetBundle = null;

    @Override
    protected void processFile(Path path) throws IOException {
        Map<String, ByteBuffer> entries = assetBundle.getEntries();
        String name = path.getFileName().toString();

        // If data already exists for this filename, skip
        if(entries.get(name) != null) {
            L.log(Level.INFO, "Skipping {0}, data already set in bundle entries", path);
            return;
        }

        entries.put(name, ByteBufferUtils.openReadOnly(path));
    }

    protected Path getDefaultOutputPath() {
        List<Path> files = getFiles();
        Path first = files.get(0);
        Path parent = first.getParent(); // Assume getParent() not null
        return parent.resolve(first.getFileName().toString() + ".unity3d");
    }

    @Override
    public void run() {
        if(bundleFormat < 1 || bundleFormat > 3) {
            L.log(Level.WARNING, "Bad bundle format, should be within range [1,3]: {0}", bundleFormat);
            return;
        }

        if(player < 2 || player > 3) {
            L.log(Level.WARNING, "Bad player version, should be within range [2,3]: {0}", player);
            return;
        }

        // Make sure specified engine is valid
        UnityVersion engineVersion = new UnityVersion(engine);
        if(!engineVersion.isValid()) {
            L.log(Level.WARNING, "Invalid player version: {0}", engine);
            return;
        }

        AssetBundle bundle = new AssetBundle();

        if(xianjian) {
            bundle.getSaveCodecs().add(new XianjianCodec());
        }

        UnityVersion playerVersion = null;
        if(player == 2) {
            playerVersion = new UnityVersion("2.x.x");
        }
        else {
            playerVersion = new UnityVersion("3.x.x");
        }

        if(rawSignature) {
            bundle.setSignatureRaw();
        }
        else {
            bundle.setSignatureWeb();
        }

        bundle.setFormat((byte)bundleFormat);
        bundle.setPlayerVersion(playerVersion);
        bundle.setEngineVersion(engineVersion);
        bundle.setCompressed(!noCompress);

        assetBundle = bundle;

        super.run();

        // Write bundle
        try {
            assetBundle.save(getDefaultOutputPath());
        }
        catch(IOException e) {
            L.log(Level.WARNING, "Exception occurred while saving asset bundle", e);
        }
    }
}
