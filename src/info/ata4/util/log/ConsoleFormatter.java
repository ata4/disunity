/*
 ** 2011 April 22
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.util.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Log formatter for console output.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ConsoleFormatter extends Formatter {
    
    private static final Map<Level, String> LEVEL_PREFIX;
    
    static {
        Map<Level, String> levelPrefix = new HashMap<>();
        levelPrefix.put(Level.CONFIG,  "[config]");
        levelPrefix.put(Level.FINE,    "[debug]");
        levelPrefix.put(Level.FINER,   "[debug]");
        levelPrefix.put(Level.FINEST,  "[trace]");
        levelPrefix.put(Level.INFO,    "[info]");
        levelPrefix.put(Level.SEVERE,  "[error]");
        levelPrefix.put(Level.WARNING, "[warning]");
        
        LEVEL_PREFIX = Collections.unmodifiableMap(levelPrefix);
    }

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append(LEVEL_PREFIX.get(record.getLevel()));
        sb.append(' ');

        String[] classNameParts = record.getLoggerName().split("\\.");

        // add class name for non-info records
        if (record.getLevel() != Level.INFO && classNameParts.length != 0) {
            sb.append(classNameParts[classNameParts.length - 1]);
            sb.append(": ");
        }

        sb.append(formatMessage(record));

        // print stack trace if given
        Throwable thrown = record.getThrown();
        if (thrown != null) {
            sb.append(", caused by ");
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                thrown.printStackTrace(pw);
            }
            sb.append(sw.toString());
        }
        
        sb.append("\n");

        return sb.toString();
    }
}
