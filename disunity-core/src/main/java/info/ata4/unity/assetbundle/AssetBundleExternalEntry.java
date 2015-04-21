/*
 ** 2014 December 03
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.assetbundle;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetBundleExternalEntry extends AssetBundleEntry {
    
    private final String name;
    private final Path file;

    public AssetBundleExternalEntry(String name, Path file) {
        this.name = name;
        this.file = file;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public long size() {
        try {
            return Files.size(file);
        } catch (IOException ex) {
            return 0;
        }
    }

    @Override
    public InputStream inputStream() throws IOException {
        return Files.newInputStream(file);
    }
    
}
