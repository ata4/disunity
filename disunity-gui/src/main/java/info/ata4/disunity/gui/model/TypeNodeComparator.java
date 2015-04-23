/*
 ** 2014 October 21
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.gui.model;

import info.ata4.unity.asset.TypeNode;
import java.util.Comparator;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TypeNodeComparator implements Comparator<TypeNode> {
    
    @Override
    public int compare(TypeNode o1, TypeNode o2) {
        return o1.type().typeName().compareTo(o2.type().typeName());
    }
}
