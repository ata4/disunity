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
import info.ata4.unity.asset.bundle.AssetBundle;
import info.ata4.unity.asset.struct.AssetHeader;
import info.ata4.unity.asset.struct.AssetRef;
import info.ata4.unity.asset.struct.ObjectPath;
import info.ata4.unity.asset.struct.TypeTree;
import info.ata4.util.string.StringUtils;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import org.apache.commons.lang3.BooleanUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandNames = "info",
    commandDescription = "Shows basic information about assets and asset bundles."
)
public class InfoCmd extends AssetCommand {
    
    private final PrintStream ps;
    
    public InfoCmd(PrintStream ps) {
        this.ps = ps;
    }

    @Override
    public void processAsset(AssetFile asset) throws IOException {
        List<ObjectPath> paths = asset.getPaths();
        List<AssetRef> refTable = asset.getReferences();
        AssetHeader header = asset.getHeader();
        TypeTree typeTree = asset.getTypeTree();
        
        ps.println("Header");
        ps.println("  File size: " + StringUtils.humanReadableByteCount(header.getFileSize(), true));
        ps.println("  Tree size: " + StringUtils.humanReadableByteCount(header.getTreeSize(), true));
        ps.println("  Format: " + header.getFormat());
        ps.println("  Data offset: " + header.getDataOffset());
        ps.println();
        
        ps.println("Serialized data");
        ps.println("  Revision: " + typeTree.getEngineVersion());
        ps.println("  Version: " + typeTree.getVersion());
        ps.println("  Type tree: " + BooleanUtils.toStringYesNo(!typeTree.getFields().isEmpty()));
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
        ps.println("Format: " + bundle.getFormat());
        ps.println("Player version: " + bundle.getPlayerVersion());
        ps.println("Engine version: " + bundle.getEngineVersion());
        ps.println("Compressed: " + BooleanUtils.toStringYesNo(bundle.isCompressed()));
        ps.println("Entries: " + bundle.getEntries().size());
        ps.println();
    }
}
