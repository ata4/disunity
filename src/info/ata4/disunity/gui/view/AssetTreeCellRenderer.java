/*
 ** 2014 September 23
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.gui.view;

import info.ata4.disunity.gui.util.IconUtils;
import javax.swing.Icon;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetTreeCellRenderer extends DelegatorTreeCellRenderer {
    
    private final Icon fileIcon = IconUtils.createIcon("document.png");
    private final Icon openIconCustom = IconUtils.createIcon("folder-open.png");
    private final Icon closedIconCustom = IconUtils.createIcon("folder.png");
    
    public AssetTreeCellRenderer() {
        setOpenIcon(openIconCustom);
        setClosedIcon(closedIconCustom);
        setLeafIcon(fileIcon);
        
        addDelegate(new FieldTypeNodeTreeCellRenderer());
        addDelegate(new FieldNodeTreeCellRenderer());
        addDelegate(new ObjectDataTreeCellRenderer());
        addDelegate(new FileIdentifierCellRenderer());
        addDelegate(new AssetFileTreeCellRenderer());
        addDelegate(new ExceptionTreeCellRenderer());
    }
}
