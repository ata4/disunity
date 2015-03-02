/*
 ** 2014 December 25
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.extract;

import info.ata4.unity.rtti.ObjectData;
import java.io.IOException;
import java.nio.file.Path;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public interface AssetExtractor {
    
    boolean isEligible(ObjectData objectData);
    
    void extract(ObjectData objectData) throws IOException;
    
    Path getOutputDirectory();
    
    void setOutputDirectory(Path dir);
}
