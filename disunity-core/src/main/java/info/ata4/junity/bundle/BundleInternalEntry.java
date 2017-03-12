/*
 ** 2014 December 03
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.bundle;

import info.ata4.util.function.IOFunction;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BundleInternalEntry extends BundleEntry {

    private final BundleEntryInfo info;
    private final IOFunction<BundleEntryInfo, InputStream> inputStreamFactory;

    public BundleInternalEntry(BundleEntryInfo info,
            IOFunction<BundleEntryInfo, InputStream> isFactory) {
        this.info = info;
        this.inputStreamFactory = isFactory;
    }

    @Override
    public String name() {
        return info.name();
    }

    @Override
    public long size() {
        return info.size();
    }

    @Override
    public InputStream inputStream() throws IOException {
        return inputStreamFactory.apply(info);
    }

    @Override
    public String toString() {
        return name();
    }
}
