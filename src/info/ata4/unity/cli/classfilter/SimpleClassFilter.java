/*
 ** 2013 December 25
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.classfilter;

import info.ata4.unity.asset.struct.ObjectPath;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple Unity class filter based on fixed ID sets.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class SimpleClassFilter implements ClassFilter {
    
    private final Set<Integer> acceptedIDs;
    private final Set<Integer> rejectedIDs;
    
    public SimpleClassFilter(Collection<Integer> accepted, Collection<Integer> rejected) {
         acceptedIDs = new HashSet<>(accepted);
         rejectedIDs = new HashSet<>(rejected);
    }

    @Override
    public boolean accept(ObjectPath path) {
        return accept(path.getClassID());
    }

    @Override
    public boolean accept(int classID) {
        return !rejectedIDs.contains(classID) && (acceptedIDs.isEmpty() || acceptedIDs.contains(classID));
    }
}
