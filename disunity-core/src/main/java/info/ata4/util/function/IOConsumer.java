/*
 ** 2015 November 29
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.util.function;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

/**
 * Interface for consumers that throw IOExceptions, which are wrapped to
 * UncheckedIOExceptions.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@FunctionalInterface
public interface IOConsumer<T> {

    void accept(T t) throws IOException;

    public static <T> Consumer<T> uncheck(IOConsumer<T> consumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        };
    }
}
