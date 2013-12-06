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
public class DeserializationException extends Exception {

    public DeserializationException() {
    }

    public DeserializationException(String msg) {
        super(msg);
    }

    public DeserializationException(Throwable cause) {
        super(cause);
    }

    public DeserializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
