/*
 ** 2015 December 01
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
import info.ata4.disunity.cli.util.TableBuilder;
import info.ata4.disunity.cli.util.TableModel;
import info.ata4.disunity.cli.util.TablePrinter;
import info.ata4.junity.bundle.Bundle;
import info.ata4.junity.bundle.BundleHeader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandDescription = "Show bundle information."
)
public class BundleInfo extends BundleCommand {

    @ParametersDelegate
    private final OutputFormatDelegate outputFormat = new OutputFormatDelegate();

    @Override
    protected void runBundle(Path file, Bundle bundle) {
        if (outputFormat.get() == OutputFormat.TEXT) {
            output().println(file);
        }

        List<TableModel> tables = new ArrayList<>();
        tables.add(new TableModel("Header", buildHeaderTable(bundle.header())));

        TablePrinter tablePrinter = TablePrinter.fromOutputFormat(outputFormat.get(), output());
        tablePrinter.file(file);
        tablePrinter.print(tables);
    }

    private Table<Integer, Integer, Object> buildHeaderTable(BundleHeader header) {
        TableBuilder table = new TableBuilder();
        table.row("Field", "Value");

        table.row("signature", header.signature());
        table.row("streamVersion", header.streamVersion());
        table.row("unityVersion", header.unityVersion());
        table.row("unityRevision", header.unityRevision());
        table.row("minimumStreamedBytes", header.minimumStreamedBytes());
        table.row("headerSize", header.headerSize());
        table.row("numberOfLevelsToDownload", header.numberOfLevelsToDownload());
        table.row("numberOfLevels", header.numberOfLevels());

        List<Pair<Long, Long>> levelByteEnds = header.levelByteEnd();
        for (int i = 0; i < levelByteEnds.size(); i++) {
            Pair<Long, Long> levelByteEnd = levelByteEnds.get(i);
            table.row("levelByteEnd[" + i + "][0]", levelByteEnd.getLeft());
            table.row("levelByteEnd[" + i + "][1]", levelByteEnd.getRight());
        }

        if (header.streamVersion() >= 2) {
            table.row("completeFileSize", header.completeFileSize());
        }

        if (header.streamVersion() >= 3) {
            table.row("dataHeaderSize", header.dataHeaderSize());
        }

        return table.get();
    }

}
