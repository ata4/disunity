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
import info.ata4.unity.asset.Type;
import javax.swing.Icon;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class NodeTreeCellRenderer<E> implements DelegateTreeCellRenderer<E> {
    
    private final Icon boolIcon = IconUtils.createIcon("document-attribute-b.png");
    private final Icon byteIcon = IconUtils.createIcon("document-attribute-b.png");
    private final Icon shortIcon = IconUtils.createIcon("document-attribute-s.png");
    private final Icon intIcon = IconUtils.createIcon("document-attribute-i.png");
    private final Icon floatIcon = IconUtils.createIcon("document-attribute-f.png");
    private final Icon doubleIcon = IconUtils.createIcon("document-attribute-d.png");
    private final Icon defaultIcon = IconUtils.createIcon("document-block.png");
    private final Icon binaryIcon = IconUtils.createIcon("document-binary.png");
    private final Icon stringIcon = IconUtils.createIcon("document-text.png");
    
    protected Icon getIconForType(Type type) {
        switch (type.typeName()) {
            case "bool":
                return boolIcon;

            case "SInt8":
            case "UInt8":
            case "char":
                return byteIcon;

            case "SInt16":
            case "short":
            case "UInt16":
            case "unsigned short":
                return shortIcon;

            case "SInt32":
            case "int":
            case "UInt32":
            case "unsigned int":
                return intIcon;

            case "float":
                return floatIcon;

            case "double":
                return doubleIcon;

            case "string":
                return stringIcon;

            case "TypelessData":
                return binaryIcon;

            default:
                return defaultIcon;
        }
    }
}
