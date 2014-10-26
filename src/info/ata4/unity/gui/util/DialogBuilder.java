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

import info.ata4.log.LogUtils;
import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.StringUtils;

/**
 * Fluent interface for dialog boxes.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DialogBuilder {
    
    private static final Logger L = LogUtils.getLogger();
    
    private enum Type {
        
        ERROR(JOptionPane.ERROR_MESSAGE),
        INFORMATION(JOptionPane.INFORMATION_MESSAGE),
        WARNING(JOptionPane.WARNING_MESSAGE),
        QUESTION(JOptionPane.QUESTION_MESSAGE),
        PLAIN(JOptionPane.PLAIN_MESSAGE);
        
        private final int numeric;
        
        private Type(int numeric) {
            this.numeric = numeric;
        }

        public int getNumeric() {
            return numeric;
        }
    }
    
    private final Component parent;
    private Type type = Type.PLAIN;
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
        type = Type.ERROR;
        return this;
    }
    
    public DialogBuilder info() {
        type = Type.INFORMATION;
        return this;
    }
    
    public DialogBuilder question() {
        type = Type.QUESTION;
        return this;
    }
    
    public DialogBuilder plain() {
        type = Type.PLAIN;
        return this;
    }
    
    public DialogBuilder exception(Exception ex) {
        this.title = "Error";
        this.type = Type.ERROR;
        this.ex = ex;
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
    
    public void show() {
        String msg = message;
        
        if (ex != null) {
            String exMsg;
            if (StringUtils.isBlank(ex.getMessage())) {
                exMsg = ex.toString();
            } else {
                exMsg = ex.getMessage();
            }
            
            if (StringUtils.isBlank(msg)) {
                msg = exMsg;
            } else {
                msg += ":\n" + exMsg;
            }
            
            L.log(Level.WARNING, msg, ex);
        }
        
        JOptionPane.showMessageDialog(parent, msg, title, type.getNumeric());
    }
}
