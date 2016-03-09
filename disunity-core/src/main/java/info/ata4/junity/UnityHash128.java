/*
 ** 2015 April 20
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import info.ata4.io.Struct;
import java.io.IOException;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class UnityHash128 implements Struct {

    private final byte[] hash = new byte[16];

    public byte[] hash() {
        return hash;
    }

    @Override
    public void read(DataReader in) throws IOException {
        in.readBytes(hash);
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeBytes(hash);
    }

    @Override
    public String toString() {
        return DatatypeConverter.printHexBinary(hash);
    }
}
