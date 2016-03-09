/*
 ** 2015 December 01
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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class UnityTableStruct<T extends Struct> extends UnityStruct<T> {

    private final List<T> elements = new ArrayList<>();

    public UnityTableStruct(Class<T> elementFactory) {
        super(elementFactory);
    }

    public List<T> elements() {
        return elements;
    }

    @Override
    public void read(DataReader in) throws IOException {
        elements.clear();
        int entries = in.readInt();
        for (int i = 0; i < entries; i++) {
            elements.add(readEntry(in));
        }
    }

    protected T readEntry(DataReader in) throws IOException {
        T element = createElement();
        in.readStruct(element);
        return element;
    }

    @Override
    public void write(DataWriter out) throws IOException {
        int entries = elements.size();
        out.writeInt(entries);
        for (T element : elements) {
            writeEntry(out, element);
        }
    }

    protected void writeEntry(DataWriter out, T element) throws IOException {
        out.writeStruct(element);
    }
}
