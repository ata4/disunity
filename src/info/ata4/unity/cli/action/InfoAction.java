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
import info.ata4.unity.asset.struct.AssetClassType;
import info.ata4.unity.asset.struct.AssetHeader;
import info.ata4.unity.asset.struct.AssetObjectPath;
import info.ata4.unity.asset.struct.AssetRef;
import info.ata4.unity.assetbundle.AssetBundle;
import info.ata4.unity.assetbundle.struct.AssetBundleHeader;
import info.ata4.util.string.StringUtils;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class InfoAction extends PrintAction {
    
    public InfoAction(PrintStream ps) {
        super(ps);
    }

    @Override
    public boolean supportsAssets() {
        return true;
    }

    @Override
    public boolean supportsAssetBundes() {
        return true;
    }

    @Override
    public void processAsset(AssetFile asset) throws IOException {
        List<AssetObjectPath> paths = asset.getPaths();
        List<AssetRef> refTable = asset.getReferences();
        AssetHeader header = asset.getHeader();
        AssetClassType classType = asset.getClassType();
        
        ps.println("Header");
        ps.println("  File size: " + StringUtils.humanReadableByteCount(header.getFileSize(), true));
        ps.println("  Tree size: " + StringUtils.humanReadableByteCount(header.getTreeSize(), true));
        ps.println("  Format: " + header.getFormat());
        ps.println("  Data offset: " + header.getDataOffset());
        ps.println();
        
        ps.println("Serialized data");
        ps.println("  Revision: " + classType.getRevision());
        ps.println("  Version: " + classType.getVersion());
        ps.println("  Type tree: " + (classType.hasTypeTree() ? "yes" : "no"));
        ps.println("  Objects: " + paths.size());
        ps.println();
        
        if (!refTable.isEmpty()) {
            ps.println("External references");
            for (AssetRef ref : refTable) {
                if (!ref.getAssetPath().isEmpty()) {
                    ps.printf("  Asset path: \"%s\"\n", ref.getAssetPath());
                }
                if (!ref.getFilePath().isEmpty()) {
                    ps.printf("  File path: \"%s\"\n", ref.getFilePath());
                }
                ps.printf("  GUID: %s\n", ref.getGUID());
                ps.printf("  Type: %d\n", ref.getType());
                ps.println();
            }
        }
    }

    @Override
    public void processAssetBundle(AssetBundle bundle) throws IOException {
        AssetBundleHeader header = bundle.getHeader();
        ps.println("Format: " + header.getFormat());
        ps.println("Player version: " + header.getPlayerVersion());
        ps.println("Engine version: " + header.getEngineVersion());
        ps.println("Compressed: " + (header.isCompressed() ? "yes" : "no"));
        ps.println("Entries: " + bundle.getEntries().size());
        ps.println();
    }
}
