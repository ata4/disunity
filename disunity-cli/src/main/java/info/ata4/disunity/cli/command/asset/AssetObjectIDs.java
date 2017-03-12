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

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandDescription = "List object IDs (Unity 5+ only)."
)
public class AssetObjectIDs extends AssetTableCommand {

    @Override
    protected TableModel tableModel(SerializedFile serialized) {
        TableBuilder table = new TableBuilder();
        table.row("Asset Index", "ID in file");

        serialized.metadata().objectIDTable().elements().forEach(objectID -> {
            table.row(objectID.serializedFileIndex(), objectID.identifierInFile());
        });

        return new TableModel("Object IDs", table.get());
    }
}
