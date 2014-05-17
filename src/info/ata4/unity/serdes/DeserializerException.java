/*
 ** 2013 August 14
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.serdes;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DeserializerException extends Exception {

    public DeserializerException() {
    }

    public DeserializerException(String msg) {
        super(msg);
    }

    public DeserializerException(Throwable cause) {
        super(cause);
    }

    public DeserializerException(String message, Throwable cause) {
        super(message, cause);
    }
}
