/*
 ** 2014 April 08
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.action;

import info.ata4.unity.assetbundle.AssetBundle;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BundleListAction extends PrintAction {
    
    public BundleListAction(PrintStream ps) {
        super(ps);
    }

    @Override
    public boolean supportsAssets() {
        return false;
    }

    @Override
    public boolean supportsAssetBundes() {
        return true;
    }
    
    @Override
    public boolean requiresOutputDir() {
        return false;
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
