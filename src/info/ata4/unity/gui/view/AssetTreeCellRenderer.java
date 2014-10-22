/*
 ** 2014 September 23
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.gui.view;

import info.ata4.unity.asset.Reference;
import info.ata4.unity.assetbundle.BundleEntry;
import info.ata4.unity.rtti.FieldNode;
import info.ata4.unity.rtti.FieldType;
import info.ata4.unity.rtti.FieldTypeNode;
import info.ata4.unity.rtti.ObjectData;
import java.awt.Color;
import java.awt.Component;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetTreeCellRenderer extends DefaultTreeCellRenderer {
    
    private static Icon createIcon(String file) {
        try {
            return new ImageIcon(Object.class.getResource("/resources/gui/icons/" + file));
        } catch (Exception ex) {
            return null;
        }
    }

    private final Icon fileIcon = createIcon("document.png");
    private final Icon openIconCustom = createIcon("folder-open.png");
    private final Icon closedIconCustom = createIcon("folder.png");
    
    private final Icon boolIcon = createIcon("document-attribute-b.png");
    private final Icon byteIcon = createIcon("document-attribute-b.png");
    private final Icon shortIcon = createIcon("document-attribute-s.png");
    private final Icon intIcon = createIcon("document-attribute-i.png");
    private final Icon floatIcon = createIcon("document-attribute-f.png");
    private final Icon doubleIcon = createIcon("document-attribute-d.png");
    private final Icon defaultIcon = createIcon("document-block.png");
    private final Icon binaryIcon = createIcon("document-binary.png");
    private final Icon stringIcon = createIcon("document-text.png");
    
    private final Map<Object, String> textCache = new HashMap<>();

    public AssetTreeCellRenderer() {
        setOpenIcon(openIconCustom);
        setClosedIcon(closedIconCustom);
        setLeafIcon(fileIcon);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        
        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
            Object userObject = treeNode.getUserObject();
            
            if (userObject instanceof Path) {
                formatPath((Path) userObject);
            } else if (userObject instanceof FieldNode) {
                formatFieldNode((FieldNode) userObject);
            } else if (userObject instanceof FieldTypeNode) {
                formatFieldTypeNode((FieldTypeNode) userObject);
            } else if (userObject instanceof ObjectData) {
                formatObjectData((ObjectData) userObject);
            } else if (userObject instanceof BundleEntry) {
                formatAssetBundleEntry((BundleEntry) userObject);
            } else if (userObject instanceof Reference) {
                formatReference((Reference) userObject);
            } else if (userObject instanceof Exception) {
                formatException((Exception) userObject);
            }
        }

        return this;
    }
    
    private void formatPath(Path path) {
        setText(path.getFileName().toString());
        setIcon(fileIcon);
    }
    
    private void formatFieldNode(FieldNode node) {
        FieldType type = node.getType();
        
        setIconForType(type);
        
        String text = textCache.get(node);
        
        if (text == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(type.getTypeName());

            Object value = node.getValue();
            if (value instanceof List) {
                sb.append('[');
                sb.append(((List) value).size());
                sb.append(']');
            } else {
                String fieldName = type.getFieldName();
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

                if (value != null) {
                    sb.append(": ");
                    if (value instanceof String) {
                        sb.append('"');
                        sb.append(value);
                        sb.append('"');
                    } else if (value instanceof ByteBuffer) {
                        ByteBuffer buf = (ByteBuffer) value;
                        sb.append(FileUtils.byteCountToDisplaySize(buf.capacity()));
                    } else {
                        sb.append(value);
                    }
                }
            }
            
            text = StringUtils.abbreviate(sb.toString(), 128);
            
            textCache.put(node, text);
        }
         
        setText(text);
    }
    
    private void formatFieldTypeNode(FieldTypeNode node) {
        FieldType type = node.getType();
        
        setIconForType(type);
        
        String text = textCache.get(node);
        
        if (text == null) {
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
            
            text = sb.toString();
            
            textCache.put(node, text);
        }
        
        setText(text);
    }
    
    private void setIconForType(FieldType type) {
        switch (type.getTypeName()) {
            case "bool":
                setIcon(boolIcon);
                break;

            case "SInt8":
            case "UInt8":
            case "char":
                setIcon(byteIcon);
                break;

            case "SInt16":
            case "short":
            case "UInt16":
            case "unsigned short":
                setIcon(shortIcon);
                break;

            case "SInt32":
            case "int":
            case "UInt32":
            case "unsigned int":
                setIcon(intIcon);
                break;

            case "float":
                setIcon(floatIcon);
                break;

            case "double":
                setIcon(doubleIcon);
                break;

            case "string":
                setIcon(stringIcon);
                break;

            case "TypelessData":
                setIcon(binaryIcon);
                break;

            default:
                setIcon(defaultIcon);
        }
    }
    
    private void formatObjectData(ObjectData objectData) {
        String text = textCache.get(objectData);
        
        if (text == null) {
            text = objectData.getName();
            textCache.put(objectData, text);
        }

        setText(text);
    }

    private void formatAssetBundleEntry(BundleEntry entry) {
        setText(FilenameUtils.getName(entry.getInfo().getName()));
    }
    
    private void formatReference(Reference reference) {
        setText(reference.getFilePath());
    }
    
    private void formatException(Exception ex) {
        setForeground(Color.RED);
        setText("Error: " + ex.toString());
    }
    
}
