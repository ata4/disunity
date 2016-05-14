/*
 ** 2015 November 23
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.junity.serialize.fileidentifier;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FileIdentifierV2 extends FileIdentifierV1 {

    // Path to the asset file?
    private String assetPath;

    @Override
    public void read(DataReader in) throws IOException {
        assetPath = in.readStringNull();
        super.read(in);
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeStringNull(assetPath);
        super.write(out);
    }

    public String assetPath() {
        return assetPath;
    }

    public void assetPath(String assetPath) {
        this.assetPath = assetPath;
    }
}
