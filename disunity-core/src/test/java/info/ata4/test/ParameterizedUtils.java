/*
 ** 2015 December 08
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ParameterizedUtils {

    private ParameterizedUtils() {
    }

    public static List<Path[]> getPathParameters(Path dir) throws IOException {
        return Files.walk(dir)
            .filter(Files::isRegularFile)
            .map(file -> new Path[] {file})
            .collect(Collectors.toList());
    }
}
