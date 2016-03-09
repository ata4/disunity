/*
 ** 2015 November 29
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.bundle;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class Bundle {

    private final BundleHeader header = new BundleHeader();
    private final List<BundleEntry> entries = new ArrayList<>();
    private final List<BundleEntryInfo> entryInfos = new ArrayList<>();

    public BundleHeader header() {
        return header;
    }

    public List<BundleEntry> entries() {
        return entries;
    }

    public List<BundleEntryInfo> entryInfos() {
        return entryInfos;
    }
}
