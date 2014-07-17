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
import info.ata4.unity.cli.dump.AssetDumper;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandNames = "dump",
    commandDescription = "Dumps object data to console."
)
public class DumpCmd extends AssetCommand {
    
    private boolean dumpStructs = false;
    private boolean dumpToFiles = false;
    
    public boolean isDumpStructs() {
        return dumpStructs;
    }

    public DumpCmd setDumpStructs(boolean structs) {
        this.dumpStructs = structs;
        return this;
    }
    
    public boolean isDumpToFiles() {
        return dumpToFiles;
    }

    public DumpCmd setDumpToFiles(boolean dumpToFiles) {
        this.dumpToFiles = dumpToFiles;
        return this;
    }

    @Override
    public void processAsset(AssetFile asset) throws IOException {
        AssetDumper dmp = new AssetDumper(asset);
        dmp.setClassFilter(getOptions().getClassFilter());
        if (dumpToFiles) {
            dmp.setOutputDir(getOutputDir());
        }
        if (dumpStructs) {
            dmp.dumpStruct();
        } else {
            dmp.dumpData();
        }
    }
}
