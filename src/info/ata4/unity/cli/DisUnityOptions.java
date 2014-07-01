/*
 ** 2013 August 11
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli;

import com.beust.jcommander.Parameter;
import info.ata4.unity.cli.classfilter.ClassFilter;
import info.ata4.unity.cli.classfilter.SimpleClassFilter;
import info.ata4.unity.cli.converters.ClassIDConverter;
import java.util.ArrayList;
import java.util.List;

/**
 * DisUnity configuration class.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DisUnityOptions {
        
    @Parameter(
        names = {"-h", "--help"},
        description = "Print this help.",
        help = true
    )
    private boolean help;
    
    @Parameter(
        names = { "-v", "--verbose" },
        description = "Show more verbose log output."
    )
    private boolean verbose;
    
    @Parameter(
        names = {"-r", "--recursive"},
        description = "Find all matching files recursively in all subdirectories"
    )
    private boolean recursive;
    
    @Parameter(
        names = { "-f", "--include" },
        description = "Only process objects that use these classes. Expects a string with class names or IDs, separated by commas.",
        converter = ClassIDConverter.class
    )
    private final List<Integer> classListInclude = new ArrayList<>();
    
    @Parameter(
        names = { "-x", "--exclude" },
        description = "Exclude objects from processing that use these classes. Expects a string with class names or IDs, separated by commas.",
        converter = ClassIDConverter.class
    )
    private final List<Integer> classListExclude = new ArrayList<>();

    public boolean isHelp() {
        return help;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public ClassFilter getClassFilter() {
        return new SimpleClassFilter(classListInclude, classListExclude);
    }
}
