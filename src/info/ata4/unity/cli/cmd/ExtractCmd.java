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
    commandNames = "extract",
    commandDescription = "Extracts content from asset files."
)
public class ExtractCmd extends AssetCommand {
    
    private boolean raw;
    
    public boolean isRaw() {
        return raw;
    }

    public ExtractCmd setRaw(boolean raw) {
        this.raw = raw;
        return this;
    }

    @Override
    protected void processAsset(AssetFile asset) throws IOException {
        AssetExtractor ae = new AssetExtractor(asset);
        ae.setClassFilter(getOptions().getClassFilter());
        ae.setOutputDir(getOutputDir());
        ae.extract(isRaw());
    }
}
