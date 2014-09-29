/*
 ** 2014 September 29
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.gui.util.progress;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class ProgressTask<T, V> extends SwingWorker<Void, Void> {
    
    private final ProgressMonitor monitor;
    protected final Progress progress;
    
    public ProgressTask(Component parent, Object message, String note) {
        monitor = new ProgressMonitor(parent, message, note, 0, 100);
        monitor.setMillisToPopup(1000);

        ProgressListener listener = new ProgressListener();
        addPropertyChangeListener(listener);
        progress = listener;
    }
    
    private class ProgressListener implements Progress, PropertyChangeListener {

        private long limit = 100;

        @Override
        public void setLimit(long limit) {
            this.limit = limit;
        }

        @Override
        public void setLabel(String label) {
            monitor.setNote(label);
        }

        @Override
        public void update(long current) {
            setProgress((int) (current * 100L / limit));
        }

        @Override
        public boolean isCanceled() {
            return monitor.isCanceled();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("progress")) {
                monitor.setProgress(getProgress());
            }
        }
    }
}
