/*
 ** 2015 November 23
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity;

import info.ata4.io.Struct;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class UnityStruct<T extends Struct> implements Struct {

    protected final Class<T> elementFactory;

    public UnityStruct(Class<T> elementFactory) {
        this.elementFactory = elementFactory;
    }

    public Class<T> elementFactory() {
        return elementFactory;
    }

    protected T createElement() {
        try {
            return elementFactory.newInstance();
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new RuntimeException(ex);
        }
    }

}
