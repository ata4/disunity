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

import info.ata4.disunity.gui.util.IconUtils;
import java.nio.file.Path;
import javax.swing.Icon;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class PathTreeCellRenderer implements DelegateTreeCellRenderer<Path> {
    
    private final Icon fileIcon = IconUtils.createIcon("document.png");
    
    @Override
    public Class<Path> getTreeCellType() {
        return Path.class;
    }

    @Override
    public void configureTreeCellRenderer(DefaultTreeCellRenderer renderer,
            Path userData, boolean sel, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
        renderer.setText(userData.getFileName().toString());
        renderer.setIcon(fileIcon);
    }
}
