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

import static javax.swing.JOptionPane.DEFAULT_OPTION;
import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.YES_NO_CANCEL_OPTION;
import static javax.swing.JOptionPane.YES_NO_OPTION;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
 enum DialogOptionType {
     
    DEFAULT(DEFAULT_OPTION),
    YES_NO(YES_NO_OPTION),
    YES_NO_CANCEL(YES_NO_CANCEL_OPTION),
    OK_CANCEL(OK_CANCEL_OPTION);
    
    private final int numeric;

    private DialogOptionType(int numeric) {
        this.numeric = numeric;
    }

    public int numeric() {
        return numeric;
    }
}
