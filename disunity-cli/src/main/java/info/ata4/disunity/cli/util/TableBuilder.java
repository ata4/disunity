/*
 ** 2015 November 24
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.util;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import java.util.Map;
import java.util.OptionalInt;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TableBuilder<T> {

    private final Table<Integer, Integer, T> table = TreeBasedTable.create();

    public TableBuilder row(T... value) {
        int row = table.rowKeySet().size();
        for (int col = 0; col < value.length; col++) {
            table.put(row, col, value[col]);
        }
        return this;
    }

    public TableBuilder append(T... value) {
        int row = table.rowKeySet().size() - 1;

        // check for empty table
        if (row < 0) {
            return this;
        }

        Map<Integer, T> rowMap = table.row(row);
        OptionalInt colMax = rowMap.keySet().stream().mapToInt(Integer::valueOf).max();
        int colOffset = colMax.isPresent() ? colMax.getAsInt() + 1 : 0;

        for (int col = 0; col < value.length; col++) {
            table.put(row, colOffset + col, value[col]);
        }

        return this;
    }

    public Table<Integer, Integer, T> get() {
        return table;
    }
}
