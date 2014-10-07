/*
 ** 2014 October 06
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.extract;

import info.ata4.unity.rtti.ObjectData;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public interface ObjectExtractor {
    
    Set<String> getClassNames();
    
    void process(ObjectData object) throws Exception;
    
    List<FileHandle> getFiles();
}
