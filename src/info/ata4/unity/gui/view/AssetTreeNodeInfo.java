/*
 ** 2014 October 03
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.gui.view;

import info.ata4.io.Struct;
import info.ata4.io.util.ObjectToString;
import info.ata4.unity.gui.model.StructNode;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetTreeNodeInfo implements TreeSelectionListener {
    
    private final JTextPane textPane;

    public AssetTreeNodeInfo(JTextPane text) {
        this.textPane = text;
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        textPane.setText(null);

        if (e.getNewLeadSelectionPath() == null) {
            return;
        }

        Object obj = e.getNewLeadSelectionPath().getLastPathComponent();
        
        if (obj instanceof StructNode) {
            StructNode structNode = (StructNode) obj;
            
            List<Struct> structs = new ArrayList<>();
            
            structNode.getStructs(structs);
            
            StringBuilder sb = new StringBuilder();
            
            for (Struct struct : structs) {
                if (struct != null) {
                    sb.append(ObjectToString.toString(struct, false));
                    sb.append("\n");
                }
            }
            
            textPane.setText(sb.toString());
        }
    }
}
