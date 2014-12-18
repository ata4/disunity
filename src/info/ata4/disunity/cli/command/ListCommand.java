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
import info.ata4.unity.rtti.ObjectData;
import info.ata4.unity.util.ClassID;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;

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
        
        for (ObjectData data : asset.getObjects()) {
            ObjectInfo info = data.getInfo();
            
            int pid = data.getID();
            int cid = info.getClassID();
            String className = ClassID.getNameForID(cid);
            long ofs = info.getOffset();
            long len = info.getLength();
            String objName = data.getInstance().getChildValue("m_Name");
            
            if (objName == null) {
                objName = "";
            }
            
            tbl.addRow(pid, cid, className, ofs, len, objName);
        }
        
        tbl.print(getOutputWriter());
    }
    
    private void printJSON(AssetFile asset) {
        JSONObject root = new JSONObject();
        root.put("file", asset.getSourceFile());
        
        JSONArray objectsJson = new JSONArray();
        for (ObjectData data : asset.getObjects()) {
            ObjectInfo info = data.getInfo();
            JSONObject objectJson = new JSONObject();
            
            objectJson.put("id", data.getID());
            
            JSONObject classJson = new JSONObject();
            classJson.put("id", info.getClassID());
            classJson.put("name", ClassID.getNameForID(info.getClassID()));
            
            objectJson.put("class", classJson);
            objectJson.put("offset", info.getOffset());
            objectJson.put("length", info.getLength());
            objectJson.put("name", data.getInstance().getChildValue("m_Name"));
            
            objectsJson.put(objectJson);
        }
        
        root.put("objects", objectsJson);
        
        root.write(getOutputWriter(), 2);
    }
}
