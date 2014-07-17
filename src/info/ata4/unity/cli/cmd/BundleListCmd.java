/*
 ** 2014 April 08
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.cmd;

import com.beust.jcommander.Parameters;
import info.ata4.unity.asset.bundle.AssetBundle;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandNames = "bundle-list",
    commandDescription = "List files contained in asset bundles."
)
public class BundleListCmd extends AssetCommand {
    
    private final PrintStream ps;
    
    public BundleListCmd(PrintStream ps) {
        this.ps = ps;
        setProcessAssets(false);
        setProcessBundledAssets(false);
    }

    @Override
    public void processAssetBundle(AssetBundle bundle) throws IOException {
        int p1 = 64;
        int p2 = 10;
        
        ps.print(StringUtils.rightPad("Path", p1));
        ps.print(" | ");
        ps.print(StringUtils.leftPad("Size", p2));
        ps.println();
        
        ps.print(StringUtils.repeat("-", p1));
        ps.print(" | ");
        ps.print(StringUtils.repeat("-", p2));
        ps.println();
        
        for (Map.Entry<String, ByteBuffer> entry : bundle.getEntries().entrySet()) {
            ps.print(StringUtils.rightPad(entry.getKey(), p1));
            ps.print(" | ");
            ps.print(StringUtils.leftPad(String.valueOf(entry.getValue().limit()), p2));
            ps.println();
        }
    }
}
