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

import com.beust.jcommander.ParametersDelegate;
import info.ata4.disunity.cli.OutputFormatDelegate;
import info.ata4.disunity.cli.util.TableModel;
import info.ata4.disunity.cli.util.TablePrinter;
import info.ata4.junity.serialize.SerializedFile;
import java.nio.file.Path;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class AssetTableCommand extends AssetCommand {

    @ParametersDelegate
    private final OutputFormatDelegate outputFormat = new OutputFormatDelegate();

    @Override
    protected void runSerializedFile(Path file, SerializedFile serialized) {
        TablePrinter tablePrinter = TablePrinter.fromOutputFormat(
                outputFormat.get(), output());
        tablePrinter.file(file);
        tablePrinter.print(tableModel(serialized));
    }

    protected abstract TableModel tableModel(SerializedFile serialized);
}
