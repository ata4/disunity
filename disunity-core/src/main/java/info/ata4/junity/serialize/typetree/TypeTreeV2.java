/*
 ** 2015 November 23
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.serialize.typetree;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import info.ata4.junity.UnityVersion;
import java.io.IOException;
import java.util.Objects;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TypeTreeV2<T extends TypeV1> extends TypeTreeV1<T> {

    protected UnityVersion revision = new UnityVersion();
    protected int attributes;

    public TypeTreeV2(Class<T> elementFactory) {
        super(elementFactory);
    }

    public UnityVersion revision() {
        return revision;
    }

    public void revision(UnityVersion revision) {
        this.revision = Objects.requireNonNull(revision);
    }

    public int attributes() {
        return attributes;
    }

    public void attributes(int attributes) {
        this.attributes = attributes;
    }

    @Override
    public void read(DataReader in) throws IOException {
        revision = new UnityVersion(in.readStringNull(255));
        attributes = in.readInt();

        super.read(in);

        // padding
        in.readInt();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeStringNull(revision.toString());
        out.writeInt(attributes);

        super.write(out);

        // padding
        out.writeInt(0);
    }
}
