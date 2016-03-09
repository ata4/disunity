/*
 ** 2015 November 22
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.progress;

import java.util.Optional;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public interface Progress {

    void update(Optional<String> stage, double complete);
}
