/*
 ** 2014 December 22
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.gui.view;

import info.ata4.unity.asset.FileIdentifier;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FileIdentifierCellRenderer implements DelegateTreeCellRenderer<FileIdentifier> {

    @Override
    public Class<FileIdentifier> getTreeCellType() {
        return FileIdentifier.class;
    }
    
    @Override
    public void configureTreeCellRenderer(DefaultTreeCellRenderer renderer,
            FileIdentifier userData, boolean sel, boolean expanded, boolean leaf,
            int row, boolean hasFocus) {
        String text;
        
        if (!StringUtils.isBlank(userData.getFilePath())) {
            text = userData.getFilePath();
        } else if (!StringUtils.isBlank(userData.getAssetPath())) {
            text = userData.getAssetPath();
        } else {
            text = userData.getGUID().toString();
        }
        
        renderer.setText(text);
    }
    
}
