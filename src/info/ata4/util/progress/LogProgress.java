/*
 ** 2014 December 02
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.util.progress;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class LogProgress implements Progress {
    
    private final Logger logger;
    
    public LogProgress(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void setLabel(String label) {
        logger.log(Level.INFO, "Processing {0}", label);
    }

    @Override
    public void setLimit(long limit) {
    }

    @Override
    public void update(long current) {
    }

    @Override
    public boolean isCanceled() {
        return false;
    }
    
}
