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

import static javax.swing.JOptionPane.CANCEL_OPTION;
import static javax.swing.JOptionPane.CLOSED_OPTION;
import static javax.swing.JOptionPane.NO_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public enum DialogReturnType {
    
    YES(YES_OPTION),
    NO(NO_OPTION),
    CANCEL(CANCEL_OPTION),
    OK(OK_OPTION),
    CLOSED(CLOSED_OPTION);
    
    private final int numeric;

    private DialogReturnType(int numeric) {
        this.numeric = numeric;
    }

    public int numeric() {
        return numeric;
    }

    public static DialogReturnType fromNumeric(int numeric) {
        for (DialogReturnType returnType : values()) {
            if (returnType.numeric() == numeric) {
                return returnType;
            }
        }
        throw new IllegalArgumentException("Wrong numeric type");
    }
}
