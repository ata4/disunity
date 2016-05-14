/*
 ** 2015 April 09
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
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class UnityGUID implements Struct {

    private UUID uuid;

    public UUID uuid() {
        return uuid;
    }

    public void uuid(UUID uuid) {
        this.uuid = Objects.requireNonNull(uuid);
    }

    @Override
    public void read(DataReader in) throws IOException {
        // read GUID as big-endian
        ByteOrder order = in.order();
        in.order(ByteOrder.BIG_ENDIAN);
        long guidMost = in.readLong();
        long guidLeast = in.readLong();
        in.order(order);
        uuid = new UUID(guidMost, guidLeast);
    }

    @Override
    public void write(DataWriter out) throws IOException {
        // write GUID as big-endian
        ByteOrder order = out.order();
        out.order(ByteOrder.BIG_ENDIAN);
        out.writeLong(uuid.getMostSignificantBits());
        out.writeLong(uuid.getLeastSignificantBits());
        out.order(order);
    }

    @Override
    public String toString() {
        return uuid().toString();
    }
}
