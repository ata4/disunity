/*
 ** 2014 December 17
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
import info.ata4.unity.assetbundle.AssetBundleEntryInfo;
import info.ata4.unity.assetbundle.AssetBundleHeader;
import info.ata4.unity.assetbundle.AssetBundleReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONObject2;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandNames = "bundle-info",
    commandDescription = "Prints info for asset bundle files."
)
public class BundleInfoCommand extends BundleFileCommand {

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
        AssetBundleHeader header = reader.header();
        PrintWriter out = getOutputWriter();
        out.println("Header:");
        
        TablePrinter tbl = new TablePrinter(2);
        tbl.addRow("Field", "Value");
        tbl.addRow("signature", header.signature());
        tbl.addRow("streamVersion", header.streamVersion());
        tbl.addRow("unityVersion", header.unityVersion());
        tbl.addRow("unityRevision", header.unityRevision());
        tbl.addRow("minimumStreamedBytes", header.minimumStreamedBytes());
        tbl.addRow("headerSize", header.headerSize());
        tbl.addRow("numberOfLevelsToDownload", header.numberOfLevelsToDownload());
        tbl.addRow("numberOfLevels", header.numberOfLevels());
        
        List<Pair<Long, Long>> levelByteEnds = header.levelByteEnd();
        for (int i = 0; i < levelByteEnds.size(); i++) {
            Pair<Long, Long> levelByteEnd = levelByteEnds.get(i);
            tbl.addRow("levelByteEnd[" + i + "][0]", levelByteEnd.getLeft());
            tbl.addRow("levelByteEnd[" + i + "][1]", levelByteEnd.getRight());
        }
        
        if (header.streamVersion() >= 2) {
            tbl.addRow("completeFileSize", header.completeFileSize());
        }
        if (header.streamVersion() >= 3) {
            tbl.addRow("dataHeaderSize", header.dataHeaderSize());
        }
        
        tbl.print(out);
        
        out.println();
        out.println("Entries:");
        
        tbl = new TablePrinter(3);
        tbl.setColumnAlignment(1, 1);
        tbl.setColumnAlignment(2, 1);
        tbl.addRow("Name", "Offset", "Size");
        
        List<AssetBundleEntryInfo> entryInfos = reader.entryInfos();
        for (AssetBundleEntryInfo entryInfo : entryInfos) {
            tbl.addRow(entryInfo.name(), entryInfo.offset(), entryInfo.size());
        }
        
        tbl.print(out);
    }
    
    private void printJSON(AssetBundleReader reader) {
        AssetBundleHeader header = reader.header();
        
        JSONObject2 root = new JSONObject2();
        root.put("file", getCurrentFile());
        
        root.put("signature", header.signature());
        root.put("streamVersion", header.streamVersion());
        root.put("unityVersion", header.unityVersion());
        root.put("unityRevision", header.unityRevision());
        root.put("minimumStreamedBytes", header.minimumStreamedBytes());
        root.put("headerSize", header.headerSize());
        root.put("numberOfLevelsToDownload", header.numberOfLevelsToDownload());
        root.put("numberOfLevels", header.numberOfLevels());
        
        JSONArray levelByteEndsJson = new JSONArray();
        
        List<Pair<Long, Long>> levelByteEnds = header.levelByteEnd();
        for (int i = 0; i < levelByteEnds.size(); i++) {
            JSONArray levelByteEndJson = new JSONArray();
            
            Pair<Long, Long> levelByteEnd = levelByteEnds.get(i);
            levelByteEndJson.put(levelByteEnd.getLeft());
            levelByteEndJson.put(levelByteEnd.getRight());
            
            levelByteEndsJson.put(levelByteEndJson);
        }
        
        root.put("levelByteEnd", levelByteEndsJson);
        
        if (header.streamVersion() >= 2) {
            root.put("completeFileSize", header.completeFileSize());
        }
        
        if (header.streamVersion() >= 3) {
            root.put("dataHeaderSize", header.dataHeaderSize());
        }
        
        JSONArray entryInfosJson = new JSONArray();
        
        List<AssetBundleEntryInfo> entryInfos = reader.entryInfos();
        for (AssetBundleEntryInfo entryInfo : entryInfos) {
            JSONObject entryInfoJson = new JSONObject();
            entryInfoJson.put("name", entryInfo.name());
            entryInfoJson.put("offset", entryInfo.offset());
            entryInfoJson.put("length", entryInfo.size());
            
            entryInfosJson.put(entryInfoJson);
        }
        
        root.put("entries", entryInfosJson);
        
        root.write(getOutputWriter(), 2);
    }
}
