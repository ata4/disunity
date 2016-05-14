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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TextTableFormat {

    private final Map<Integer, Integer> columnWidths = new HashMap<>();
    private final Map<Integer, TextTableAlignment> columnAlignments = new HashMap<>();
    private final Map<Integer, Function<Object, String>> columnFormatters = new HashMap<>();
    private int numColumns;

    public void columnWidth(int column, int width) {
        columnWidths.put(column, width);
    }

    public int columnWidth(int column) {
        return columnWidths.get(column);
    }

    public void columnAlignment(int column, TextTableAlignment align) {
        columnAlignments.put(column, align);
    }

    public TextTableAlignment columnAlignment(int column) {
        return columnAlignments.get(column);
    }

    public void columnFormatter(int column, Function<Object, String> formatter) {
        columnFormatters.put(column, formatter);
    }

    public Function<Object, String> columnFormatter(int column) {
        return columnFormatters.get(column);
    }

    void configure(TableModel model) {
        numColumns = 0;

        model.table().columnKeySet().stream().forEach(columnKey -> {
            if (!columnFormatters.containsKey(columnKey)) {
                columnFormatters.put(columnKey, String::valueOf);
            }

            // set minimum column width if not already defined
            if (!columnWidths.containsKey(columnKey)) {
                columnWidths.put(columnKey, model.table()
                    .column(columnKey)
                    .values()
                    .stream()
                    .map(columnFormatters.get(columnKey))
                    .mapToInt(String::length)
                    .max()
                    .getAsInt()
                );
            }

            if (!columnAlignments.containsKey(columnKey)
                    || columnAlignments.get(columnKey) == TextTableAlignment.AUTO) {
                // count class types
                Map<Class, Long> columnTypeMap = model.table()
                    .column(columnKey)
                    .values()
                    .stream()
                    .skip(model.columnHeader() ? 1 : 0) // don't include type of column header
                    .map(Object::getClass)
                    .collect(Collectors.groupingBy(o -> o, Collectors.counting()));

                // get most occurring class
                Optional<Map.Entry<Class, Long>> topClassEntry = columnTypeMap
                    .entrySet()
                    .stream()
                    .max((v1, v2) -> Long.compare(v1.getValue(), v2.getValue()));

                Class columnType = Object.class;

                if (topClassEntry.isPresent()) {
                    columnType = topClassEntry.get().getKey();
                }

                // align number columns to the right for better readability
                boolean isNumber = Number.class.isAssignableFrom(columnType);
                TextTableAlignment align;

                if (isNumber) {
                    align = TextTableAlignment.RIGHT;
                } else {
                    align = TextTableAlignment.LEFT;
                }

                columnAlignments.put(columnKey, align);
            }

            numColumns++;
        });
    }

    int tableWidth(String cellSeparator) {
        return columnWidths.values().stream()
                .reduce(0, (a, b) -> a + b)
                + cellSeparator.length() * (columnWidths.size() - 1);
    }

    String formatCell(Object value, int column) {
        int width = columnWidths.get(column);
        TextTableAlignment align = columnAlignments.get(column);
        Function<Object, String> formatter = columnFormatters.get(column);
        String content = formatter.apply(value);

        if (content.length() > width) {
            // truncate
            content = StringUtils.abbreviate(content, width);
        } else if (content.length() < width) {
            // add padding
            switch (align) {
                case LEFT:
                    content = StringUtils.rightPad(content, width);
                    break;
                case RIGHT:
                    content = StringUtils.leftPad(content, width);
                    break;
                case CENTER:
                    content = StringUtils.center(content, width);
                    break;
            }
        }

        return content;
    }

    int numColumns() {
        return numColumns;
    }

}
