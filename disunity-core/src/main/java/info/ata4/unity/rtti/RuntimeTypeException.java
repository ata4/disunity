/*
 ** 2014 September 21
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.rtti;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class RuntimeTypeException extends RuntimeException {

    /**
     * Creates a new instance of <code>SerializationException</code> without
     * detail message.
     */
    public RuntimeTypeException() {
    }

    /**
     * Constructs an instance of <code>SerializationException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public RuntimeTypeException(String msg) {
        super(msg);
    }

    public RuntimeTypeException(Throwable cause) {
        super(cause);
    }

    public RuntimeTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
