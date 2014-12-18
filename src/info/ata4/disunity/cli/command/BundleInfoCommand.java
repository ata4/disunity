/*
 ** 2014 Dezember 17
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
import info.ata4.unity.assetbundle.AssetBundleHeader;
import info.ata4.unity.assetbundle.AssetBundleReader;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;

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
        AssetBundleHeader header = reader.getHeader();
        
        TablePrinter tbl = new TablePrinter(2);
        tbl.addRow("Field", "Value");
        tbl.addRow("signature", header.getSignature());
        tbl.addRow("streamVersion", header.getStreamVersion());
        tbl.addRow("unityVersion", header.getUnityVersion());
        tbl.addRow("unityRevision", header.getUnityRevision());
        tbl.addRow("minimumStreamedBytes", header.getMinimumStreamedBytes());
        tbl.addRow("headerSize", header.getHeaderSize());
        tbl.addRow("numberOfLevelsToDownload", header.getNumberOfLevelsToDownload());
        tbl.addRow("numberOfLevels", header.getNumberOfLevels());
        
        List<Pair<Long, Long>> levelByteEnds = header.getLevelByteEnd();
        for (int i = 0; i < levelByteEnds.size(); i++) {
            Pair<Long, Long> levelByteEnd = levelByteEnds.get(i);
            tbl.addRow("levelByteEnd[" + i + "][0]", levelByteEnd.getLeft());
            tbl.addRow("levelByteEnd[" + i + "][1]", levelByteEnd.getRight());
        }
        
        if (header.getStreamVersion() >= 2) {
            tbl.addRow("completeFileSize", header.getCompleteFileSize());
        }
        if (header.getStreamVersion() >= 3) {
            tbl.addRow("dataHeaderSize", header.getDataHeaderSize());
        }
        
        tbl.print(getOutputWriter());
    }
    
    private void printJSON(AssetBundleReader reader) {
        AssetBundleHeader header = reader.getHeader();
        
        JSONObject root = new JSONObject();
        root.put("file", getCurrentFile());
        
        root.put("signature", header.getSignature());
        root.put("streamVersion", header.getStreamVersion());
        root.put("unityVersion", header.getUnityVersion());
        root.put("unityRevision", header.getUnityRevision());
        root.put("minimumStreamedBytes", header.getMinimumStreamedBytes());
        root.put("headerSize", header.getHeaderSize());
        root.put("numberOfLevelsToDownload", header.getNumberOfLevelsToDownload());
        root.put("numberOfLevels", header.getNumberOfLevels());
        
        List<Pair<Long, Long>> levelByteEnds = header.getLevelByteEnd();
        for (int i = 0; i < levelByteEnds.size(); i++) {
            Pair<Long, Long> levelByteEnd = levelByteEnds.get(i);
            root.put("levelByteEnd[" + i + "][0]", levelByteEnd.getLeft());
            root.put("levelByteEnd[" + i + "][1]", levelByteEnd.getRight());
        }
        
        if (header.getStreamVersion() >= 2) {
            root.put("completeFileSize", header.getCompleteFileSize());
        }
        
        if (header.getStreamVersion() >= 3) {
            root.put("dataHeaderSize", header.getDataHeaderSize());
        }
        
        root.write(getOutputWriter(), 2);
    }
}
