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
import info.ata4.disunity.cli.util.Formatters;
import info.ata4.disunity.cli.util.TableBuilder;
import info.ata4.disunity.cli.util.TableModel;
import info.ata4.disunity.cli.util.TextTableFormat;
import info.ata4.junity.serialize.SerializedFile;
import info.ata4.util.io.DataBlock;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandDescription = "List file data blocks."
)
public class AssetBlocks extends AssetTableCommand {

    @Override
    protected TableModel tableModel(SerializedFile serialized) {
        Map<String, DataBlock> blocks = new LinkedHashMap<>();

        blocks.put("Header", serialized.headerBlock());
        blocks.put("Type Tree", serialized.metadata().typeTreeBlock());
        blocks.put("Object Info", serialized.metadata().objectInfoBlock());
        blocks.put("External Refs", serialized.metadata().externalsBlock());
        blocks.put("Object Data", serialized.objectDataBlock());

        TableBuilder table = new TableBuilder();
        table.row("Name", "Offset", "Size");

        blocks.entrySet().stream()
            .sorted((e1, e2) ->
                Long.compare(
                    e1.getValue().offset(),
                    e2.getValue().offset()
                )
            )
            .forEach(e -> {
                DataBlock block = e.getValue();
                table.row(
                    e.getKey(),
                    block.offset(),
                    block.length()
                );
            });

        TableModel model = new TableModel("Blocks", table.get());
        TextTableFormat format = model.format();
        format.columnFormatter(1, Formatters::hex);
        return model;
    }
}
