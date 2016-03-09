/*
 ** 2015 December 13
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.util.function;

import java.util.function.Predicate;

/**
 * Small predicate utility class.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class Predicates {

    private Predicates() {
    }

    public static <T> Predicate<T> not(Predicate<T> t) {
        return t.negate();
    }
}
