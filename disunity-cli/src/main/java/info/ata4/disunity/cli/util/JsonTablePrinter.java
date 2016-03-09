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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class JsonTablePrinter extends TablePrinter {

    public JsonTablePrinter(PrintWriter out) {
        super(out);
    }

    public JsonTablePrinter withFile(Path file) {
        this.file = file;
        return this;
    }

    @Override
    public void print(TableModel model) {
        print(Arrays.asList(model));
    }

    @Override
    public void print(Collection<TableModel> models) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject jsonRoot = new JsonObject();

        if (file != null) {
            jsonRoot.add("file", new JsonPrimitive(file.toString()));
        }

        models.forEach(model -> {
            jsonRoot.add(model.name().toLowerCase(), tableToJson(model.table(), gson));
        });

        gson.toJson(jsonRoot, out);
    }

    private JsonArray tableToJson(Table<Integer, Integer, Object> table, Gson gson) {
        JsonArray jsonTable = new JsonArray();

        table.rowMap().forEach((rk, r) -> {
            if (rk == 0) {
                return;
            }

            JsonObject jsonRow = new JsonObject();

            table.columnMap().forEach((ck, c) -> {
                String key = String.valueOf(table.get(0, ck)).toLowerCase();
                Object value = table.get(rk, ck);
                jsonRow.add(key, gson.toJsonTree(value));
            });

            jsonTable.add(jsonRow);
        });

        JsonObject jsonRoot = new JsonObject();

        if (file != null) {
            jsonRoot.add("file", new JsonPrimitive(file.toString()));
        }

        return jsonTable;
    }
}
