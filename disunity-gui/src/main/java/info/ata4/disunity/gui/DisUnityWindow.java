/*
 ** 2014 Mai 05
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.gui;

import info.ata4.disunity.gui.control.AssetTreePopupMenuListener;
import info.ata4.disunity.gui.model.AssetFileNode;
import info.ata4.disunity.gui.model.FieldTypeDatabaseNode;
import info.ata4.disunity.gui.util.dialog.DialogBuilder;
import info.ata4.disunity.gui.util.dialog.DialogReturnType;
import info.ata4.disunity.gui.util.FileExtensionFilter;
import info.ata4.disunity.gui.view.AssetTreeCellRenderer;
import info.ata4.unity.DisUnity;
import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.assetbundle.AssetBundleUtils;
import info.ata4.unity.util.TypeTreeUtils;
import info.ata4.util.progress.Progress;
import info.ata4.util.progress.ProgressMonitorWrapper;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javax.swing.JFileChooser;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultTreeModel;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DisUnityWindow extends javax.swing.JFrame {
    
    private File file;
    
    /**
     * Creates new form DisUnityWindow
     */
    public DisUnityWindow() {
        initComponents();
        initComponentsCustom();
    }
    
    private void initComponentsCustom() {
        dataTree.setCellRenderer(new AssetTreeCellRenderer());
        dataTree.addMouseListener(new AssetTreePopupMenuListener(dataTree));
    }
    
    public void openFile(File file) {
        try {
            // check if the file is an asset bundle, these can't be opened or edited
            // directly and need to be extracted
            if (AssetBundleUtils.isAssetBundle(file.toPath())) {
                DialogReturnType result = new DialogBuilder()
                        .question()
                        .withYesNo()
                        .withTitle("Extract asset bundle")
                        .withMessage("The selected file is an asset bundle and "
                                + "first needs to be extracted to open its asset files.\n"
                                + "Do you want to extract it now?")
                        .show();
                
                if (result == DialogReturnType.YES) {
                    extractAssetBundles(true, file);
                }
                
                return;
            }
            
            AssetFile asset = new AssetFile();
            asset.load(file.toPath());
            asset.loadExternals();

            dataTree.setModel(new DefaultTreeModel(new AssetFileNode(dataTree, asset)));
            
            this.file = file; // for the file chooser
        } catch (Exception ex) {
            new DialogBuilder(this)
                    .exception(ex)
                    .withMessage("Can't open " + file.getName())
                    .show();
        }
    }
    
    private void chooseAssetFile(File currentDirectory) {
        JFileChooser fc = new JFileChooser(currentDirectory);
        fc.addChoosableFileFilter(new FileExtensionFilter("Unity scene", "unity"));
        fc.addChoosableFileFilter(new FileExtensionFilter("Unity asset", "asset", "assets", "sharedAssets"));
        
        int result = fc.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        openFile(fc.getSelectedFile());
    }
    
    private void chooseAssetBundles(File currentDirectory) {
        JFileChooser fc = new JFileChooser(currentDirectory);
        fc.setDialogTitle("Select asset bundle file");
        fc.setFileFilter(new FileExtensionFilter("Unity asset bundle", "unity3d"));
        fc.setMultiSelectionEnabled(true);
        
        int result = fc.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        extractAssetBundles(false, fc.getSelectedFiles());
    }
    
    private void extractAssetBundles(final boolean openAssetFileChooser, final File ... bundleFiles) {
        JFileChooser fc = new JFileChooser(bundleFiles[bundleFiles.length - 1]);
        fc.setDialogTitle("Select output directory");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = fc.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        final File outputDir = fc.getSelectedFile();
        
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Component parent = DisUnityWindow.this;
                
                Path lastOutputPath = null;
                
                for (File bundleFile : bundleFiles) {                    
                    ProgressMonitor monitor = new ProgressMonitor(parent, "Extracting " + bundleFile, null, 0, 0);
                    monitor.setMillisToDecideToPopup(0);
                    monitor.setMillisToPopup(0);

                    Progress progress = new ProgressMonitorWrapper(monitor);
                    
                    String bundleName = FilenameUtils.removeExtension(bundleFile.getName());
                    Path bundlePath = bundleFile.toPath();
                    Path outputPath = outputDir.toPath().resolve(bundleName);
                    
                    lastOutputPath = outputPath;

                    try {
                        AssetBundleUtils.extract(bundlePath, outputPath, progress);
                    } catch (Exception ex) {
                        new DialogBuilder(parent)
                                .exception(ex)
                                .withMessage("Can't extract file " + bundleFile)
                                .show();
                    }
                }
                
                // open file chooser in the last directory if requested
                if (openAssetFileChooser && lastOutputPath != null) {
                    chooseAssetFile(lastOutputPath.toFile());
                }

                return null;
            }
        }.execute();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dataTreeScrollPane = new javax.swing.JScrollPane();
        dataTree = new javax.swing.JTree();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        openTypeDatabaseItem = new javax.swing.JMenuItem();
        extractAssetBundleMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(DisUnity.getSignature());

        dataTree.setModel(null);
        dataTreeScrollPane.setViewportView(dataTree);

        fileMenu.setMnemonic('f');
        fileMenu.setText("File");

        openMenuItem.setMnemonic('o');
        openMenuItem.setText("Open");
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openMenuItem);

        saveMenuItem.setMnemonic('s');
        saveMenuItem.setText("Save");
        saveMenuItem.setEnabled(false);
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setMnemonic('a');
        saveAsMenuItem.setText("Save As ...");
        saveAsMenuItem.setDisplayedMnemonicIndex(5);
        saveAsMenuItem.setEnabled(false);
        fileMenu.add(saveAsMenuItem);

        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        jMenu1.setText("Tools");

        openTypeDatabaseItem.setText("Open type tree database");
        openTypeDatabaseItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openTypeDatabaseItemActionPerformed(evt);
            }
        });
        jMenu1.add(openTypeDatabaseItem);

        extractAssetBundleMenuItem.setText("Extract asset bundle");
        extractAssetBundleMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extractAssetBundleMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(extractAssetBundleMenuItem);

        menuBar.add(jMenu1);

        helpMenu.setMnemonic('h');
        helpMenu.setText("Help");

        aboutMenuItem.setMnemonic('a');
        aboutMenuItem.setText("About");
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dataTreeScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 614, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dataTreeScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        chooseAssetFile(file);
    }//GEN-LAST:event_openMenuItemActionPerformed

    private void openTypeDatabaseItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openTypeDatabaseItemActionPerformed
        dataTree.setModel(new DefaultTreeModel(new FieldTypeDatabaseNode()));
    }//GEN-LAST:event_openTypeDatabaseItemActionPerformed

    private void extractAssetBundleMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extractAssetBundleMenuItemActionPerformed
        chooseAssetBundles(file);
    }//GEN-LAST:event_extractAssetBundleMenuItemActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JTree dataTree;
    private javax.swing.JScrollPane dataTreeScrollPane;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenuItem extractAssetBundleMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem openTypeDatabaseItem;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    // End of variables declaration//GEN-END:variables

}
