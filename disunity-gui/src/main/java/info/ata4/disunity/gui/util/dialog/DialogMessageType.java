/*
 ** 2015 April 26
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.gui.util.dialog;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
 enum DialogMessageType {
     
    ERROR(ERROR_MESSAGE),
    INFORMATION(INFORMATION_MESSAGE),
    WARNING(WARNING_MESSAGE),
    QUESTION(QUESTION_MESSAGE),
    PLAIN(PLAIN_MESSAGE);
    
    private final int numeric;

    private DialogMessageType(int numeric) {
        this.numeric = numeric;
    }

    public int numeric() {
        return numeric;
    }
}
