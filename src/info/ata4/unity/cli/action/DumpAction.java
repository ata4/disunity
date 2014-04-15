/*
 ** 2014 January 08
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.action;

import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.cli.dump.AssetDumper;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DumpAction extends Action {
    
    private boolean dumpStructs;
    
    public boolean isDumpStructs() {
        return dumpStructs;
    }

    public DumpAction setDumpStructs(boolean structs) {
        this.dumpStructs = structs;
        return this;
    }

    @Override
    public boolean supportsAssets() {
        return true;
    }

    @Override
    public boolean supportsAssetBundes() {
        return false;
    }
    
    @Override
    public boolean requiresOutputDir() {
        return true;
    }

    @Override
    public void processAsset(AssetFile asset) throws IOException {
        AssetDumper dmp = new AssetDumper(asset);
        dmp.setClassFilter(getClassFilter());
        dmp.setOutputDir(getOutputDir());
        if (dumpStructs) {
            dmp.dumpStruct();
        } else {
            dmp.dumpData();
        }
    }

}
