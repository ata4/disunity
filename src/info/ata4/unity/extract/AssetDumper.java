/*
 ** 2013 August 18
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.extract;

import info.ata4.unity.asset.Asset;
import info.ata4.unity.struct.FieldNode;
import info.ata4.unity.struct.TypeTree;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Set;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetDumper {
    
    private static final Logger L = Logger.getLogger(AssetDumper.class.getName());
    
    private final Asset asset;
    private boolean structOnly;
    private StringBuilder indent = new StringBuilder(256);
    private boolean stdout = false;
    private File outputDir;

    public AssetDumper(Asset asset) {
        this.asset = asset;
    }
    
    public void dump() throws FileNotFoundException {
        TypeTree typeTree = asset.getTypeTree();
        
        if (structOnly) {
            if (typeTree.isStandalone()) {
                L.info("No type tree available");
                return;
            }
            
            Set<Integer> classIDs = asset.getClassIDs();
            for (Integer classID : classIDs) {
                FieldNode classField = typeTree.get(classID);
                if (classField == null) {
                    continue;
                }
            }
        } else {
            
        }
    }
}
