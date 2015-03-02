/*
 ** 2014 September 29
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.util.progress;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DummyProgress implements Progress {

    @Override
    public void setLimit(long limit) {
    }

    @Override
    public void setLabel(String label) {
    }

    @Override
    public void update(long current) {
    }

    @Override
    public boolean isCanceled() {
        return false;
    }
    
}
