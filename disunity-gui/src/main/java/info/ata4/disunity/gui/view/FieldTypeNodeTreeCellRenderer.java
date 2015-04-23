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

import info.ata4.unity.asset.Type;
import info.ata4.unity.asset.TypeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FieldTypeNodeTreeCellRenderer extends NodeTreeCellRenderer<TypeNode> {
    
    @Override
    public Class<TypeNode> getTreeCellType() {
        return TypeNode.class;
    }

    @Override
    public void configureTreeCellRenderer(DefaultTreeCellRenderer renderer,
            TypeNode userData, boolean sel, boolean expanded, boolean leaf,
            int row, boolean hasFocus) {
        Type type = userData.type();
        
        renderer.setIcon(getIconForType(type));

        String fieldName = type.fieldName();

        StringBuilder sb = new StringBuilder();
        sb.append(type.typeName());

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
