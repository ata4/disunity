/*
 ** 2014 October 21
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.rtti;

import java.util.Comparator;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FieldTypeNodeComparator implements Comparator<FieldTypeNode> {
    @Override
    public int compare(FieldTypeNode o1, FieldTypeNode o2) {
        return o1.getType().getTypeName().compareTo(o2.getType().getTypeName());
    }
}
