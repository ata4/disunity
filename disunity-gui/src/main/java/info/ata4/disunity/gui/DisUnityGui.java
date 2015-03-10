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

import info.ata4.log.LogUtils;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DisUnityGui {
    
    private static final Logger L = LogUtils.getLogger();
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        LogUtils.configure(Level.FINE);
        
        // set the system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            L.warning("Failed to set SystemLookAndFeel");
        }
        
        DisUnityWindow gui = new DisUnityWindow();
        gui.setVisible(true);
        
        if (args.length > 0) {
            gui.openFile(new File(args[0]));
        }
    }
}
