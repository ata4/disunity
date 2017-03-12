/*
 ** 2014 Juni 28
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.converters;

import com.beust.jcommander.IStringConverter;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class PathConverter implements IStringConverter<Path> {

    @Override
    public Path convert(String value) {
        return Paths.get(value);
    }

}
