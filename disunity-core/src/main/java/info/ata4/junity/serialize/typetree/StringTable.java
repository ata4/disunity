/*
 ** 2015 November 26
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.serialize.typetree;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import info.ata4.io.DataReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class StringTable {

    private static Map<Integer, Map<Integer, String>> commonStringMap = new HashMap<>();

    private StringTable() {
    }

    private static BufferedReader resourceReader(String path) {
        return new BufferedReader(new InputStreamReader(
            StringTable.class.getResourceAsStream(path), StandardCharsets.US_ASCII));
    }

    public static BiMap<Integer, String> commonStrings(int version) throws IOException {
        // load default strings from resource files if required
        if (!commonStringMap.containsKey(version)) {
            AtomicInteger index = new AtomicInteger(1 << 31);
            String resourcePath = "/resources/strings/" + version + ".x.txt";
            try (BufferedReader br = resourceReader(resourcePath)) {
                commonStringMap.put(version, br.lines().collect(Collectors.toMap(
                    value -> index.getAndAdd(value.length() + 1),
                    value -> value
                )));
            } catch (NullPointerException ex) {
                throw new RuntimeException("No common strings file found for version " + version);
            }
        }

        return HashBiMap.create(commonStringMap.get(version));
    }

    public static BiMap<Integer, String> read(DataReader in, int length) throws IOException {
        BiMap<Integer, String> map = HashBiMap.create();

        // load strings from input
        long startPos = in.position();
        long endPos = startPos + length;
        while (in.position() < endPos) {
            int index = (int) (in.position() - startPos);
            String string = in.readStringNull();
            map.put(index, string);
        }

        return map;
    }
}
