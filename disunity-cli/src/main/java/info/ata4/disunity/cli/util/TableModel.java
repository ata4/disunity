/*
 ** 2015 November 27
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.util;

import com.google.common.collect.Table;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TableModel {

    private final Table<Integer, Integer, Object> table;
    private final String name;
    private boolean columnHeader = true;
    private TextTableFormat format = new TextTableFormat();

    public TableModel(String name, Table<Integer, Integer, Object> table) {
        this.name = name;
        this.table = table;
    }

    public Table<Integer, Integer, Object> table() {
        return table;
    }

    public TextTableFormat format() {
        return format;
    }

    public void format(TextTableFormat format) {
        this.format = format;
    }

    public boolean columnHeader() {
        return columnHeader;
    }

    public void columnHeader(boolean columnHeader) {
        this.columnHeader = columnHeader;
    }

    public String name() {
        return name;
    }
}
