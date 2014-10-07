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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class AbstractObjectExtractor implements ObjectExtractor {
    
    private final Set<String> classNames;
    protected final List<FileHandle> files = new ArrayList<>();
    
    public AbstractObjectExtractor(String... className) {
        this.classNames = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(className)));
    }

    @Override
    public Set<String> getClassNames() {
        return classNames;
    }

    @Override
    public List<FileHandle> getFiles() {
        return files;
    }
}
