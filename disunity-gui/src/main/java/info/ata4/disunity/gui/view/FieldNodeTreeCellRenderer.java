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
import info.ata4.unity.rtti.FieldNode;
import info.ata4.util.io.FileUtilsExt;
import java.nio.ByteBuffer;
import java.util.List;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FieldNodeTreeCellRenderer extends NodeTreeCellRenderer<FieldNode> {
    
    @Override
    public Class<FieldNode> getTreeCellType() {
        return FieldNode.class;
    }

    @Override
    public void configureTreeCellRenderer(DefaultTreeCellRenderer renderer,
            FieldNode userData, boolean sel, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
        Type type = userData.getType();
        
        renderer.setIcon(getIconForType(type));

        StringBuilder sb = new StringBuilder();
        sb.append(type.typeName());

        Object nodeValue = userData.getValue();
        if (nodeValue instanceof List) {
            sb.append('[');
            sb.append(((List) nodeValue).size());
            sb.append(']');
        } else {
            String fieldName = type.fieldName();
            if (!fieldName.equals("Base") && !fieldName.equals("Array")) {
                sb.append(' ');
                if (fieldName.contains(" ")) {
                    sb.append('"');
                    sb.append(fieldName);
                    sb.append('"');
                } else {
                    sb.append(fieldName);
                }
            }

            if (nodeValue != null) {
                sb.append(": ");
                if (nodeValue instanceof String) {
                    sb.append('"');
                    sb.append(nodeValue);
                    sb.append('"');
                } else if (nodeValue instanceof ByteBuffer) {
                    ByteBuffer buf = (ByteBuffer) nodeValue;
                    sb.append(FileUtilsExt.formatByteCount(buf.capacity()));
                } else {
                    sb.append(nodeValue);
                }
            }
        }

        renderer.setText(StringUtils.abbreviate(sb.toString(), 128));
    }

}
