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

import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Log handler for console output.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ConsoleHandler extends Handler {

    private boolean doneHeader;
        
    public ConsoleHandler() {
        setFormatter(new ConsoleFormatter());
    }

    private void doHeaders() {
        if (!doneHeader) {
            System.out.print(getFormatter().getHead(this));
            System.err.print(getFormatter().getHead(this));
            doneHeader = true;
        }
    }

    @Override
    public void publish(LogRecord record) {
        String msg;
        try {
            msg = getFormatter().format(record);
        } catch (Exception ex) {
            reportError(null, ex, ErrorManager.FORMAT_FAILURE);
            return;
        }

        try {
            doHeaders();
            // print errors and warnings to System.err
            if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                System.err.print(msg);
            } else {
                System.out.print(msg);
            }
        } catch (Exception ex) {
            reportError(null, ex, ErrorManager.WRITE_FAILURE);
        }
    }

    @Override
    public void flush() {
        try {
            System.out.flush();
            System.err.flush();
        } catch (Exception ex) {
            reportError(null, ex, ErrorManager.FLUSH_FAILURE);
        }
    }

    @Override
    public void close() throws SecurityException {
        // don't close the system streams!
        doHeaders();
        System.out.print(getFormatter().getTail(this));
        System.err.print(getFormatter().getTail(this));
        flush();
    }
}
