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
import info.ata4.unity.assetbundle.AssetBundleEntry;
import info.ata4.unity.assetbundle.AssetBundleReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import org.apache.commons.lang3.StringUtils;
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
    
    private static final int PAD_NAME = 64;
    private static final int PAD_SIZE = 10;
    
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
        out.print(StringUtils.rightPad("Path", PAD_NAME));
        out.print(" | ");
        out.print(StringUtils.leftPad("Size", PAD_SIZE));
        out.println();
        out.print(StringUtils.repeat("-", PAD_NAME));
        out.print(" | ");
        out.print(StringUtils.repeat("-", PAD_SIZE));
        out.println();

        for (AssetBundleEntry entry : reader) {
            out.print(StringUtils.rightPad(entry.getName(), PAD_NAME));
            out.print(" | ");
            out.print(StringUtils.leftPad(String.valueOf(entry.getSize()), PAD_SIZE));
            out.println();
        }
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
