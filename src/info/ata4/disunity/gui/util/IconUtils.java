/*
 ** 2014 December 22
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.gui.util;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class IconUtils {
    
    public static Icon createIcon(String file) {
        try {
            return new ImageIcon(Object.class.getResource("/resources/gui/icons/" + file));
        } catch (Exception ex) {
            return null;
        }
    }

    private IconUtils() {
    }
}
