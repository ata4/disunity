/*
 ** 2014 December 17
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.util;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TablePrinter {
    
    private static final char CHR_HORIZ = '-';
    private static final char CHR_VERT = '|';
    private static final char CHR_CONNECT = '+';
    private static final char CHR_SPACE = ' ';
    
    private final List<List<String>> data = new ArrayList<>();
    private final List<MutableInt> columnWidths;
    private final List<MutableInt> columnAligns;
    
    public TablePrinter(int columns) {
        columnWidths = new ArrayList<>(columns);
        columnAligns = new ArrayList<>(columns);
        for (int i = 0; i < columns; i++) {
            columnWidths.add(new MutableInt());
            columnAligns.add(new MutableInt());
        }
    }
    
    public void setColumnAlignment(int icol, int align) {
        columnAligns.get(icol).setValue(align);
    }
    
    public void addRow(Object... v) {
        List cells = Arrays.asList(v);
        
        if (cells.size() != columnWidths.size()) {
            throw new IllegalArgumentException();
        }
        
        List<String> row = new ArrayList<>(cells.size());
        for (int i = 0; i < cells.size(); i++) {
            String col = String.valueOf(cells.get(i));
            MutableInt columnWidth = columnWidths.get(i);
            columnWidth.setValue(Math.max(columnWidth.intValue(), col.length()));
            row.add(col);
        }
        
        data.add(row);
    }
    
    public void print(PrintWriter out) {
        final int rows = data.size();
        final int columns = columnWidths.size();
        
        for (int irow = 0; irow < rows; irow++) {
            // print separator for header
            if (irow <= 1) {
                printSeparator(out);
            }
            
            // print rows
            List<String> row = data.get(irow);
            for (int icol = 0; icol < columns; icol++) {
                String col = row.get(icol);
                out.print(CHR_VERT);
                out.print(CHR_SPACE);
                
                // print cell content
                int columnWidth = columnWidths.get(icol).intValue();
                int columnAlign = columnAligns.get(icol).intValue();
                
                switch (columnAlign) {
                    case 1:
                        out.print(StringUtils.leftPad(col, columnWidth, CHR_SPACE));
                        break;
                        
                    case 2:
                        out.print(StringUtils.center(col, columnWidth, CHR_SPACE));
                        break;
                        
                    default:
                        out.print(StringUtils.rightPad(col, columnWidth, CHR_SPACE));
                }
                
                out.print(CHR_SPACE);
            }
            out.print(CHR_VERT);
            out.println();
            
            // print separator for footer
            if (irow == rows - 1) {
                printSeparator(out);
            }
        }
    }
    
    private void printSeparator(PrintWriter out) {
        out.print(CHR_VERT);
        for (int icol = 0; icol < columnWidths.size(); icol++) {
            if (icol > 0) {
                out.print(CHR_CONNECT);
            }
            int columnWidth = columnWidths.get(icol).intValue();
            out.print(StringUtils.repeat(CHR_HORIZ, columnWidth + 2));
        }
        out.print(CHR_VERT);
        out.println();
    }
}
