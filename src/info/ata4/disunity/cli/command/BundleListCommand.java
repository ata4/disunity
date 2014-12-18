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
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandNames = "bundle-list",
    commandDescription = "Lists files contained in asset bundles."
)
public class BundleListCommand extends BundleFileCommand {
    
    private final PrintStream out;
    
    public BundleListCommand(PrintStream out) {
        this.out = out;
    }

    @Override
    public void handleBundleFile(Path file) throws IOException {
        try (AssetBundleReader reader = new AssetBundleReader(file)) {
            switch (getOptions().getOutputFormat()) {                    
                case JSON:
                    printJSON(reader, file);
                    break;
                    
                default:
                    printText(reader);
            }
        }
    }
    
    private void printText(AssetBundleReader reader) {
        TablePrinter tbl = new TablePrinter(2);
        tbl.setColumnAlignment(1, 1);
        tbl.addRow("Name", "Size");

        for (AssetBundleEntry entry : reader) {
            tbl.addRow(entry.getName(), FileUtilsExt.formatByteCount(entry.getSize()));
        }
        
        tbl.print(new PrintWriter(out));
    }
    
    private void printJSON(AssetBundleReader reader, Path file) {
        JSONObject root = new JSONObject();
        root.put("file", file);
        
        JSONArray entriesJson = new JSONArray();
        for (AssetBundleEntry entry : reader) {
            JSONObject entryJson = new JSONObject();
            entryJson.put("name", entry.getName());
            entryJson.put("size", entry.getSize());
            entriesJson.put(entryJson);
        }
        root.put("entries", entriesJson);
        
        out.println(root.toString(2));
    }
}
