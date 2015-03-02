/*
 ** 2013 July 12
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.asset;

import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetException extends IOException {

    /**
     * Creates a new instance of
     * <code>AssetException</code> without detail message.
     */
    public AssetException() {
    }

    /**
     * Constructs an instance of
     * <code>AssetException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public AssetException(String msg) {
        super(msg);
    }
}
