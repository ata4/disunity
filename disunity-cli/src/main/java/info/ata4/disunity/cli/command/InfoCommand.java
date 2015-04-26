/*
 ** 2014 December 19
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.command;

import com.beust.jcommander.Parameters;
import info.ata4.disunity.cli.util.TablePrinter;
import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.asset.AssetHeader;
import info.ata4.unity.asset.FileIdentifier;
import info.ata4.unity.assetbundle.AssetBundleReader;
import java.io.IOException;
import java.io.PrintWriter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONObject2;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandNames = "info",
    commandDescription = "Shows basic information about asset files."
)
public class InfoCommand extends AssetFileCommand {

    @Override
    public void handleAssetFile(AssetFile asset) throws IOException {
        switch (getOptions().getOutputFormat()) {                    
            case JSON:
                printJSON(asset);
                break;

            default:
                printText(asset);
        }
    }
    
    private void printText(AssetFile asset) {
        PrintWriter out = getOutputWriter();

        AssetHeader header = asset.header();
        
        TablePrinter tbl = new TablePrinter(2);
        tbl.addRow("Field", "Value");
        tbl.addRow("metadataSize", header.metadataSize());
        tbl.addRow("fileSize", header.fileSize());
        tbl.addRow("version", header.version());
        tbl.addRow("dataOffset", header.dataOffset());
        
        if (header.version() >= 9) {
            tbl.addRow("endianness", header.endianness());
        }
        
        out.println("Header:");
        tbl.print(out);
        out.println();
        
        tbl = new TablePrinter(2);
        tbl.addRow("Field", "Value");
        tbl.addRow("signature", asset.versionInfo().unityRevision());
        tbl.addRow("attributes", asset.typeTreeAttributes());
        tbl.addRow("numBaseClasses", asset.typeTree().size());
        
        out.println("Type tree:");
        tbl.print(out);
        out.println();
        
        tbl = new TablePrinter(4);
        tbl.addRow("File path", "Asset path", "GUID", "Type");
        
        for (FileIdentifier external : asset.externals()) {
            tbl.addRow(external.filePath(), external.assetPath(), external.guid(), external.type());
        }
        
        out.println("Externals:");
        tbl.print(out);
    }

    private void printJSON(AssetFile asset) {
        JSONObject2 root = new JSONObject2();
        
        AssetBundleReader assetBundle = getCurrentAssetBundle();
        if (assetBundle != null) {
            root.put("file", getCurrentFile().resolve(getCurrentAssetBundleEntry().name()));
        } else {
            root.put("file", getCurrentFile());
        }
        
        AssetHeader header = asset.header();
        
        JSONObject headerJson = new JSONObject();
        headerJson.put("metadataSize", header.metadataSize());
        headerJson.put("fileSize", header.fileSize());
        headerJson.put("version", header.version());
        headerJson.put("dataOffset", header.dataOffset());
        
        root.put("header", headerJson);
        
        JSONObject typeTreeJson = new JSONObject();
        typeTreeJson.put("signature", asset.versionInfo().unityRevision());
        typeTreeJson.put("attributes", asset.typeTreeAttributes());
        typeTreeJson.put("numBaseClasses", asset.typeTree().size());
        
        root.put("typeTree", typeTreeJson);
        
        JSONArray externalsJson = new JSONArray();
        
        for (FileIdentifier external : asset.externals()) {
            JSONObject externalJson = new JSONObject();
            externalJson.put("assetPath", external.assetPath());
            externalJson.put("guid", external.guid());
            externalJson.put("filePath", external.filePath());
            externalJson.put("type", external.type());
            
            externalsJson.put(externalJson);
        }
        
        root.put("externals", externalsJson);
        
        root.write(getOutputWriter(), 2);
    }
}
