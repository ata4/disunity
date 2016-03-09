/*
 ** 2015 November 30
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.command.bundle;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.collect.Table;
import info.ata4.disunity.cli.OutputFormat;
import info.ata4.disunity.cli.OutputFormatDelegate;
import info.ata4.disunity.cli.util.Formatters;
import info.ata4.disunity.cli.util.TableBuilder;
import info.ata4.disunity.cli.util.TableModel;
import info.ata4.disunity.cli.util.TablePrinter;
import info.ata4.disunity.cli.util.TextTableFormat;
import info.ata4.junity.bundle.Bundle;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandDescription = "List all files in bundles."
)
public class BundleList extends BundleCommand {

    @ParametersDelegate
    private final OutputFormatDelegate outputFormat = new OutputFormatDelegate();

    @Override
    protected void runBundle(Path file, Bundle bundle) {
        if (outputFormat.get() == OutputFormat.TEXT) {
            output().println(file);
        }

        TableModel tableModel = new TableModel("Files", buildEntryTable(bundle));
        TextTableFormat format = tableModel.format();
        format.columnFormatter(2, Formatters::hex);

        List<TableModel> tables = new ArrayList<>();
        tables.add(tableModel);

        TablePrinter tablePrinter = TablePrinter.fromOutputFormat(outputFormat.get(), output());
        tablePrinter.file(file);
        tablePrinter.print(tables);
    }

    private Table<Integer, Integer, Object> buildEntryTable(Bundle bundle) {
        TableBuilder table = new TableBuilder();
        table.row("Name", "Size", "Offset");

        bundle.entryInfos().forEach(entry -> {
            table.row(entry.name(), entry.size(), entry.offset());
        });

        return table.get();
    }
}
