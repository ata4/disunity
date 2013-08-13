/*
 ** 2013 August 14
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.serialization;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class UnityDeserializerException extends Exception {

    public UnityDeserializerException() {
    }

    public UnityDeserializerException(String msg) {
        super(msg);
    }

    public UnityDeserializerException(Throwable cause) {
        super(cause);
    }

    public UnityDeserializerException(String message, Throwable cause) {
        super(message, cause);
    }
}
