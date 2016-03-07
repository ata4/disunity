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

import com.google.common.collect.Table;
import java.io.PrintWriter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TextTablePrinter extends TablePrinter {

    private final char rowSeparator = '-';
    private final char nameSeparator = '=';
    private final String cellSeparator = "  ";

    public TextTablePrinter(PrintWriter out) {
        super(out);
    }

    @Override
    public void print(TableModel model) {
        TextTableFormat format = model.format();
        format.configure(model);

        out.println(file);

        // print table name
        String name = model.name();
        name = " " + name + " ";
        int size = Math.max(name.length() + 2, format.tableWidth(cellSeparator));
        name = StringUtils.center(name, size, nameSeparator);
        out.println(name);

        // print cells
        model.table().cellSet().forEach(cell -> printCell(model, format, cell));
        out.println();
        out.println();
    }

    private void printCell(TableModel model, TextTableFormat format, Table.Cell<Integer, Integer, Object> cell) {
        int numColumns = format.numColumns();
        int colKey = cell.getColumnKey();
        int rowKey = cell.getRowKey();

        // print new line after the last cell of a row
        if (colKey == 0) {
            if (rowKey != 0) {
                out.println();
            }

            // print column header separator for first and second row
            if (model.columnHeader() && colKey == 0 && rowKey == 1) {
                for (int i = 0; i < numColumns; i++) {
                    out.print(StringUtils.repeat(rowSeparator, format.columnWidth(i)));
                    if (i != numColumns - 1) {
                        out.print(cellSeparator);
                    }
                }
                out.println();
            }
        }

        out.print(format.formatCell(cell.getValue(), colKey));

        // print cell separator unless it's the last cell
        if (colKey != numColumns - 1) {
            out.print(cellSeparator);
        }
    }
}
