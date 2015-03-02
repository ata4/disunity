/*
 ** 2014 April 08
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.assetbundle;

import java.io.IOException;

/**
 * IOException used for asset bundle errors.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetBundleException extends IOException {

    /**
     * Creates a new instance of
     * <code>AssetException</code> without detail message.
     */
    public AssetBundleException() {
    }

    /**
     * Constructs an instance of
     * <code>AssetException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public AssetBundleException(String msg) {
        super(msg);
    }
}
