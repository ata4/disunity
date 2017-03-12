/*
 ** 2015 December 22
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.command.asset;

import com.beust.jcommander.Parameters;
import info.ata4.disunity.cli.util.TableBuilder;
import info.ata4.disunity.cli.util.TableModel;
import info.ata4.junity.serialize.SerializedFile;
import info.ata4.junity.serialize.SerializedFileHeader;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandDescription = "Show file header info."
)
public class AssetHeader extends AssetTableCommand {

    @Override
    protected TableModel tableModel(SerializedFile serialized) {
        SerializedFileHeader header = serialized.header();

        TableBuilder table = new TableBuilder();
        table.row("Field", "Value");

        table.row("metadataSize", header.metadataSize());
        table.row("fileSize", header.fileSize());
        table.row("version", header.version());
        table.row("dataOffset", header.dataOffset());

        if (header.version() >= 9) {
            table.row("endianness", header.endianness());
        }

        return new TableModel("Header", table.get());
    }

}
