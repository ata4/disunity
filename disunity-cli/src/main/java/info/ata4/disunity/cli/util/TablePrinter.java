/*
 ** 2015 November 22
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.util;

import info.ata4.disunity.cli.OutputFormat;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Collection;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class TablePrinter {

    public static TablePrinter fromOutputFormat(OutputFormat format, PrintWriter out) {
        switch (format) {
            case JSON:
                return new JsonTablePrinter(out);

            default:
                return new TextTablePrinter(out);
        }
    }

    protected final PrintWriter out;
    protected Path file;

    public TablePrinter(PrintWriter out) {
        this.out = out;
    }

    public void file(Path file) {
        this.file = file;
    }

    public abstract void print(TableModel model);

    public void print(Collection<TableModel> models) {
        models.forEach(this::print);
    }
}

