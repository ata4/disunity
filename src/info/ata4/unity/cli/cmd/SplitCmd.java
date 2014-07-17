/*
 ** 2014 January 08
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.cmd;

import com.beust.jcommander.Parameters;
import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.cli.extract.AssetExtractor;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandNames = "split",
    commandDescription = "Splits asset files into sub-asset files."
)
public class SplitCmd extends AssetCommand {
    
    @Override
    public void processAsset(AssetFile asset) throws IOException {
        AssetExtractor ae = new AssetExtractor(asset);
        ae.setClassFilter(getOptions().getClassFilter());
        ae.setOutputDir(getOutputDir());
        ae.split();
    }
}
