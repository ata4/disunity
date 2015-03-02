/*
 ** 2014 October 27
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.util.progress;

import javax.swing.ProgressMonitor;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ProgressMonitorWrapper implements Progress {
    
    private final ProgressMonitor monitor;
    
    public ProgressMonitorWrapper(ProgressMonitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void setLabel(String label) {
        monitor.setNote(label);
    }

    @Override
    public void setLimit(long limit) {
        monitor.setMaximum((int) Math.min(limit, Integer.MAX_VALUE));
    }

    @Override
    public void update(long current) {
        monitor.setProgress((int) Math.min(current, Integer.MAX_VALUE));
    }

    @Override
    public boolean isCanceled() {
        return monitor.isCanceled();
    }

}
