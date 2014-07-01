/*
 ** 2014 July 01
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.converters;

import com.beust.jcommander.IStringConverter;
import info.ata4.log.LogUtils;
import info.ata4.unity.util.ClassID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ClassIDConverter implements IStringConverter<Integer> {
    
    private static final Logger L = LogUtils.getLogger();

    @Override
    public Integer convert(String value) {
        Integer classID;

        try {
            classID = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            classID = ClassID.getIDForName(value, true);
        }

        if (classID == null) {
            L.log(Level.WARNING, "Invalid class name or ID: {0}", value);
        }
        
        return classID;
    }

}
