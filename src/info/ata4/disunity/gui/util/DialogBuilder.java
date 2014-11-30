package info.ata4.disunity.gui.util;

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
    
    private enum MessageType {
        
        ERROR(JOptionPane.ERROR_MESSAGE),
        INFORMATION(JOptionPane.INFORMATION_MESSAGE),
        WARNING(JOptionPane.WARNING_MESSAGE),
        QUESTION(JOptionPane.QUESTION_MESSAGE),
        PLAIN(JOptionPane.PLAIN_MESSAGE);
        
        private final int numeric;
        
        private MessageType(int numeric) {
            this.numeric = numeric;
        }

        public int getNumeric() {
            return numeric;
        }
    }
    
    private enum OptionType {
        
        DEFAULT(JOptionPane.DEFAULT_OPTION),
        YES_NO(JOptionPane.YES_NO_OPTION),
        YES_NO_CANCEL(JOptionPane.YES_NO_CANCEL_OPTION),
        OK_CANCEL(JOptionPane.OK_CANCEL_OPTION);
        
        private final int numeric;
        
        private OptionType(int numeric) {
            this.numeric = numeric;
        }

        public int getNumeric() {
            return numeric;
        }
    }
    
    public enum ReturnType {
        
        YES(JOptionPane.YES_OPTION),
        NO(JOptionPane.NO_OPTION),
        CANCEL(JOptionPane.CANCEL_OPTION),
        OK(JOptionPane.OK_OPTION),
        CLOSED(JOptionPane.CLOSED_OPTION);
        
        private final int numeric;
        
        private ReturnType(int numeric) {
            this.numeric = numeric;
        }
        
        public int getNumeric() {
            return numeric;
        }

        public static ReturnType fromNumeric(int numeric) {
            for (ReturnType returnType : values()) {
                if (returnType.getNumeric() == numeric) {
                    return returnType;
                }
            }
            
            throw new IllegalArgumentException("Wrong numeric type");
        }
    }
    
    private final Component parent;
    private MessageType messageType = MessageType.PLAIN;
    private OptionType optionType = OptionType.DEFAULT;
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
        messageType = MessageType.ERROR;
        return this;
    }
    
    public DialogBuilder info() {
        messageType = MessageType.INFORMATION;
        return this;
    }
    
    public DialogBuilder question() {
        messageType = MessageType.QUESTION;
        return this;
    }
    
    public DialogBuilder plain() {
        messageType = MessageType.PLAIN;
        return this;
    }
    
    public DialogBuilder exception(Exception ex) {
        this.title = "Error";
        this.messageType = MessageType.ERROR;
        this.ex = ex;
        return this;
    }
    
    public DialogBuilder withYesNo() {
        optionType = OptionType.YES_NO;
        return this;
    }
    
    public DialogBuilder withYesNoCancel() {
        optionType = OptionType.YES_NO_CANCEL;
        return this;
    }
    
    public DialogBuilder withOkCancel() {
        optionType = OptionType.OK_CANCEL;
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
    
    public ReturnType show() {
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
        
        if (messageType == MessageType.QUESTION) {
            int value = JOptionPane.showConfirmDialog(parent, msg, title, optionType.getNumeric(), messageType.getNumeric());
            return ReturnType.fromNumeric(value);
        } else {
            JOptionPane.showMessageDialog(parent, msg, title, messageType.getNumeric());
            return ReturnType.OK;
        }
    }
}
