/*
 ** 2014 September 28
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.gui.control;

import info.ata4.io.DataRandomAccess;
import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.unity.assetbundle.BufferedEntry;
import info.ata4.unity.assetbundle.EntryInfo;
import info.ata4.unity.gui.util.DialogUtils;
import info.ata4.unity.rtti.ObjectData;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
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
public class AssetFileTreePopup extends JPopupMenu {

    public AssetFileTreePopup(int selRow, TreePath selPath) {
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
                    extractObjectData(objectData);
                }
            });
            add(item);
        } else if (userObject instanceof BufferedEntry) {
            final BufferedEntry entry = (BufferedEntry) userObject;
            JMenuItem item = new JMenuItem("Extract file");
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    extractAssetBundleEntry(entry);
                }
            });
            add(item);
        }
    }
    
    private void extractObjectData(ObjectData objectData) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(String.format("%s.bin", objectData.getName())));
        
        int result = fileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        Path file = fileChooser.getSelectedFile().toPath();
        ByteBuffer bb = objectData.getBuffer();
        bb.rewind();
        
        try {
            ByteBufferUtils.save(file, bb);
        } catch (IOException ex) {
            DialogUtils.exception(ex, "Error saving file " + file.getFileName());
        }
    }
    
    private void extractAssetBundleEntry(BufferedEntry entry) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(entry.getInfo().getName()));
        
        int result = fileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        Path file = fileChooser.getSelectedFile().toPath();
        
        try {
            Files.copy(entry.getReader().getSocket().getInputStream(), file);
        } catch (IOException ex) {
            DialogUtils.exception(ex, "Error saving file " + file.getFileName());
        }
    }
}
