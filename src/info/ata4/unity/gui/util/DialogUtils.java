package info.ata4.unity.gui.util;

/*
 ** 2014 September 28
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */

import javax.swing.JOptionPane;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DialogUtils {
    
    private DialogUtils() {
    }
    
    private static String formatException(Exception ex) {
        if (StringUtils.isBlank(ex.getMessage())) {
            return ex.toString();
        } else {
            return ex.getMessage();
        }
    }
    
    public static void error(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    public static void error(String message) {
        error(message, "Error");
    }
    
    public static void exception(Exception ex) {
        error(formatException(ex));
    }
    
    public static void exception(Exception ex, String message) {
        error(message + ": " + formatException(ex));
    }
    
    public static void exception(Exception ex, String message, String title) {
        error(message + ": " + formatException(ex), title);
    }
}
