/*
 ** 2014 December 15
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
import info.ata4.unity.assetbundle.AssetBundleEntry;
import info.ata4.unity.assetbundle.AssetBundleReader;
import info.ata4.util.io.FileUtilsExt;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONObject2;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandNames = "bundle-list",
    commandDescription = "Lists files contained in asset bundles."
)
public class BundleListCommand extends BundleFileCommand {

    @Override
    public void handleBundleFile(AssetBundleReader reader) throws IOException {
        switch (getOptions().getOutputFormat()) {                    
            case JSON:
                printJSON(reader);
                break;

            default:
                printText(reader);
        }
    }
    
    private void printText(AssetBundleReader reader) {
        TablePrinter tbl = new TablePrinter(2);
        tbl.setColumnAlignment(1, 1);
        tbl.addRow("Name", "Size");

        for (AssetBundleEntry entry : reader) {
            tbl.addRow(entry.name(), FileUtilsExt.formatByteCount(entry.size()));
        }
        
        tbl.print(getOutputWriter());
    }
    
    private void printJSON(AssetBundleReader reader) {
        JSONObject2 root = new JSONObject2();
        root.put("file", getCurrentFile());
        
        JSONArray entriesJson = new JSONArray();
        for (AssetBundleEntry entry : reader) {
            JSONObject entryJson = new JSONObject();
            entryJson.put("name", entry.name());
            entryJson.put("size", entry.size());
            entriesJson.put(entryJson);
        }
        root.put("entries", entriesJson);
        
        root.write(getOutputWriter(), 2);
    }
}
