package info.ata4.disunity.gui.util.dialog;

/*
 ** 2014 September 28
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */

import info.ata4.log.LogUtils;
import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

/**
 * Fluent interface for dialog boxes.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DialogBuilder {
    
    private static final Logger L = LogUtils.getLogger();
    
    private final Component parent;
    private DialogMessageType messageType = DialogMessageType.PLAIN;
    private DialogOptionType optionType = DialogOptionType.DEFAULT;
    private String title;
    private String message;
    private Exception ex;
    
    public DialogBuilder(Component parent) {
        this.parent = parent;
    }
    
    public DialogBuilder() {
        this.parent = null;
    }
    
    public DialogBuilder error() {
        messageType = DialogMessageType.ERROR;
        return this;
    }
    
    public DialogBuilder info() {
        messageType = DialogMessageType.INFORMATION;
        return this;
    }
    
    public DialogBuilder question() {
        messageType = DialogMessageType.QUESTION;
        return this;
    }
    
    public DialogBuilder plain() {
        messageType = DialogMessageType.PLAIN;
        return this;
    }
    
    public DialogBuilder exception(Exception ex) {
        this.title = "Error";
        this.messageType = DialogMessageType.ERROR;
        this.ex = ex;
        return this;
    }
    
    public DialogBuilder withYesNo() {
        optionType = DialogOptionType.YES_NO;
        return this;
    }
    
    public DialogBuilder withYesNoCancel() {
        optionType = DialogOptionType.YES_NO_CANCEL;
        return this;
    }
    
    public DialogBuilder withOkCancel() {
        optionType = DialogOptionType.OK_CANCEL;
        return this;
    }
    
    public DialogBuilder withTitle(String title) {
        this.title = title;
        return this;
    }
    
    public DialogBuilder withMessage(String message) {
        this.message = message;
        return this;
    }
    
    public DialogReturnType show() {
        String msg = message;
        
        if (ex != null) {
            String exMsg;
            if (StringUtils.isBlank(ex.getMessage())) {
                exMsg = ex.toString();
            } else {
                exMsg = ex.getMessage();
            }
            
            // wrap long messages
            exMsg = WordUtils.wrap(exMsg, 50);
            
            if (StringUtils.isBlank(msg)) {
                msg = exMsg;
            } else {
                msg += ":\n" + exMsg;
            }
            
            L.log(Level.WARNING, msg, ex);
        }
        
        if (messageType == DialogMessageType.QUESTION) {
            int value = JOptionPane.showConfirmDialog(parent, msg, title, optionType.numeric(), messageType.numeric());
            return DialogReturnType.fromNumeric(value);
        } else {
            JOptionPane.showMessageDialog(parent, msg, title, messageType.numeric());
            return DialogReturnType.OK;
        }
    }
}
