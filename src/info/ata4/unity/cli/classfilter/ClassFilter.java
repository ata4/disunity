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

/**
 * Inteface for Unity class filters.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public interface ClassFilter {
    
    public boolean accept(ObjectPath path);
    public boolean accept(int classID);
    
}
