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

import info.ata4.unity.asset.FieldType;
import info.ata4.unity.asset.FieldTypeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FieldTypeNodeTreeCellRenderer extends NodeTreeCellRenderer<FieldTypeNode> {
    
    @Override
    public Class<FieldTypeNode> getTreeCellType() {
        return FieldTypeNode.class;
    }

    @Override
    public void configureTreeCellRenderer(DefaultTreeCellRenderer renderer,
            FieldTypeNode userData, boolean sel, boolean expanded, boolean leaf,
            int row, boolean hasFocus) {
        FieldType type = userData.getType();
        
        renderer.setIcon(getIconForType(type));

        String fieldName = type.getFieldName();

        StringBuilder sb = new StringBuilder();
        sb.append(type.getTypeName());

        if (!fieldName.equals("Base")) {
            sb.append(' ');
            if (fieldName.contains(" ")) {
                sb.append('"');
                sb.append(fieldName);
                sb.append('"');
            } else {
                sb.append(fieldName);
            }
        }

        renderer.setText(sb.toString());
    }
}
