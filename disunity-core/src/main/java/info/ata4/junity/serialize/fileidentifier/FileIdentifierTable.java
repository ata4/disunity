/*
 ** 2013 August 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.serialize.fileidentifier;

import info.ata4.junity.UnityTableStruct;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FileIdentifierTable<T extends FileIdentifier> extends UnityTableStruct<T> {

    public FileIdentifierTable(Class<T> elementFactory) {
        super(elementFactory);
    }
}
