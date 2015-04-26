/*
 ** 2014 December 18
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
import info.ata4.unity.asset.ObjectInfo;
import info.ata4.unity.assetbundle.AssetBundleReader;
import info.ata4.unity.rtti.ObjectData;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONObject2;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandNames = "list",
    commandDescription = "Lists all objects inside asset files."
)
public class ListCommand extends AssetFileCommand {

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
        TablePrinter tbl = new TablePrinter(6);
        tbl.addRow("ID", "Class", "Class name", "Offset", "Size", "Object name");
        tbl.setColumnAlignment(3, 1);
        tbl.setColumnAlignment(4, 1);
        
        for (ObjectData data : asset.objects()) {
            ObjectInfo info = data.info();
            
            long pid = data.ID();
            int cid = info.classID();
            String className = info.unityClass().name();
            long ofs = info.offset();
            long len = info.length();
            String objName = data.instance().getString("m_Name");
            
            if (objName == null) {
                objName = "";
            }
            
            tbl.addRow(pid, cid, className, ofs, len, objName);
        }
        
        tbl.print(getOutputWriter());
    }
    
    private void printJSON(AssetFile asset) {
        JSONObject2 root = new JSONObject2();
        
        AssetBundleReader assetBundle = getCurrentAssetBundle();
        if (assetBundle != null) {
            root.put("file", getCurrentFile().resolve(getCurrentAssetBundleEntry().name()));
        } else {
            root.put("file", getCurrentFile());
        }
        
        JSONArray objectsJson = new JSONArray();
        for (ObjectData data : asset.objects()) {
            ObjectInfo info = data.info();
            JSONObject objectJson = new JSONObject();
            
            objectJson.put("id", data.ID());
            
            JSONObject classJson = new JSONObject();
            classJson.put("id", info.classID());
            classJson.put("name", info.unityClass().name());
            
            objectJson.put("class", classJson);
            objectJson.put("offset", info.offset());
            objectJson.put("length", info.length());
            objectJson.put("name", data.instance().getString("m_Name"));
            
            objectsJson.put(objectJson);
        }
        
        root.put("objects", objectsJson);
        
        root.write(getOutputWriter(), 2);
    }
}
