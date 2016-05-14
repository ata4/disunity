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
import info.ata4.junity.serialize.fileidentifier.FileIdentifier;
import info.ata4.junity.serialize.fileidentifier.FileIdentifierTable;
import info.ata4.junity.serialize.fileidentifier.FileIdentifierV2;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandDescription = "List external references."
)
public class AssetExternalRefs extends AssetTableCommand {

    @Override
    protected TableModel tableModel(SerializedFile serialized) {
        FileIdentifierTable<FileIdentifier> externals = serialized.metadata().externals();
        Class<FileIdentifier> factory = externals.elementFactory();

        boolean v2 = FileIdentifierV2.class.isAssignableFrom(factory);

        TableBuilder table = new TableBuilder();
        table.row("File Path");

        if (v2) {
            table.append("Asset Path");
        }

        table.append("GUID", "Type");

        externals.elements().forEach(external -> {
            table.row(external.filePath());

            if (v2) {
                table.append(((FileIdentifierV2) external).assetPath());
            }

            table.append(external.guid(), external.type());
        });

        return new TableModel("External References", table.get());
    }

}
