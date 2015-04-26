/*
 ** 2014 September 28
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.gui.control;

import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.disunity.gui.util.dialog.DialogBuilder;
import info.ata4.unity.rtti.FieldNode;
import info.ata4.unity.rtti.ObjectData;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetTreePopupMenu extends JPopupMenu {

    public AssetTreePopupMenu(int selRow, TreePath selPath) {
        Object lastComponent = selPath.getLastPathComponent();
        if (!(lastComponent instanceof DefaultMutableTreeNode)) {
            return;
        }
        
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) lastComponent;
        
        Object userObject = treeNode.getUserObject();
        if (userObject instanceof ObjectData) {
            final ObjectData objectData = (ObjectData) userObject;
            JMenuItem item = new JMenuItem("Extract raw object data");
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    extractByteBuffer(objectData.buffer(), objectData.name());
                }
            });
            add(item);
        } else if (userObject instanceof FieldNode) {
            final FieldNode fieldNode = (FieldNode) userObject;
            Object fieldValue = fieldNode.getValue();
            if (fieldValue instanceof ByteBuffer) {
                final ByteBuffer bb = (ByteBuffer) fieldValue;
                JMenuItem item = new JMenuItem("Extract array data");
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        extractByteBuffer(bb, fieldNode.getType().fieldName());
                    }
                });
                add(item);
            }
        }
    }
    
    private void extractByteBuffer(ByteBuffer bb, String name) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(String.format("%s.bin", name)));
        
        int result = fileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        Path file = fileChooser.getSelectedFile().toPath();
        bb.rewind();
        
        try {
            ByteBufferUtils.save(file, bb);
        } catch (IOException ex) {
            new DialogBuilder(this)
                    .exception(ex)
                    .withMessage("Error saving file " + file.getFileName())
                    .show();
        }
    }
}
