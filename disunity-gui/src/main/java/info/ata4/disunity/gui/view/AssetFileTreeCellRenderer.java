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

import info.ata4.unity.asset.AssetFile;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetFileTreeCellRenderer implements DelegateTreeCellRenderer<AssetFile> {
    
    private final PathTreeCellRenderer pathRenderer = new PathTreeCellRenderer();

    @Override
    public Class<AssetFile> getTreeCellType() {
        return AssetFile.class;
    }

    @Override
    public void configureTreeCellRenderer(DefaultTreeCellRenderer renderer,
            AssetFile userData, boolean sel, boolean expanded, boolean leaf,
            int row, boolean hasFocus) {
        pathRenderer.configureTreeCellRenderer(renderer, userData.getSourceFile(),
                sel, expanded, leaf, row, hasFocus);
    }

}
