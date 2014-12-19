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

        AssetHeader header = asset.getHeader();
        
        TablePrinter tbl = new TablePrinter(2);
        tbl.addRow("Field", "Value");
        tbl.addRow("metadataSize", header.getMetadataSize());
        tbl.addRow("fileSize", header.getFileSize());
        tbl.addRow("version", header.getVersion());
        tbl.addRow("dataOffset", header.getDataOffset());
        
        if (header.getVersion() >= 9) {
            tbl.addRow("endianness", header.getEndianness());
        }
        
        out.println("Header:");
        tbl.print(out);
        out.println();
        
        tbl = new TablePrinter(2);
        tbl.addRow("Field", "Value");
        tbl.addRow("signature", asset.getVersionInfo().getUnityRevision());
        tbl.addRow("attributes", asset.getTypeTreeAttributes());
        tbl.addRow("numBaseClasses", asset.getTypeTree().size());
        
        out.println("Type tree:");
        tbl.print(out);
        out.println();
        
        tbl = new TablePrinter(4);
        tbl.addRow("File path", "Asset path", "GUID", "Type");
        
        for (FileIdentifier external : asset.getExternals()) {
            tbl.addRow(external.getFilePath(), external.getAssetPath(), external.getGUID(), external.getType());
        }
        
        out.println("Externals:");
        tbl.print(out);
    }

    private void printJSON(AssetFile asset) {
        JSONObject root = new JSONObject();
        
        AssetBundleReader assetBundle = getCurrentAssetBundle();
        if (assetBundle != null) {
            root.put("file", getCurrentFile().resolve(getCurrentAssetBundleEntry().getName()));
        } else {
            root.put("file", getCurrentFile());
        }
        
        AssetHeader header = asset.getHeader();
        
        JSONObject headerJson = new JSONObject();
        headerJson.put("metadataSize", header.getMetadataSize());
        headerJson.put("fileSize", header.getFileSize());
        headerJson.put("version", header.getVersion());
        headerJson.put("dataOffset", header.getDataOffset());
        
        root.put("header", headerJson);
        
        JSONObject typeTreeJson = new JSONObject();
        typeTreeJson.put("signature", asset.getVersionInfo().getUnityRevision());
        typeTreeJson.put("attributes", asset.getTypeTreeAttributes());
        typeTreeJson.put("numBaseClasses", asset.getTypeTree().size());
        
        root.put("typeTree", typeTreeJson);
        
        JSONArray externalsJson = new JSONArray();
        
        for (FileIdentifier external : asset.getExternals()) {
            JSONObject externalJson = new JSONObject();
            externalJson.put("assetPath", external.getAssetPath());
            externalJson.put("guid", external.getGUID());
            externalJson.put("filePath", external.getFilePath());
            externalJson.put("type", external.getType());
            
            externalsJson.put(externalJson);
        }
        
        root.put("externals", externalsJson);
        
        root.write(getOutputWriter(), 2);
    }
}
