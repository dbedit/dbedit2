/*
 * DBEdit 2
 * Copyright (C) 2006-2011 Jef Van Den Ouweland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dbedit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Grid {

    private List<List<Object>> rows;
    private String columnSeparator = "  ";

    public Grid() {
        this(new ArrayList<List<Object>>());
    }

    public Grid(List<List<Object>> newRows) {
        this.rows = newRows;
    }

    public Grid set(int x, int y, String text) {
        while (rows.size() < y + 1) {
            rows.add(new ArrayList<Object>());
        }
        List<Object> row = rows.get(y);
        while (row.size() < x + 1) {
            row.add(null);
        }
        row.set(x, text);
        return this;
    }

    public void add(Object[] row) {
        rows.add(Arrays.asList(row));
    }

    public void addSeparator() {
        add(new Object[] {getClass()});
    }

    public Object get(int x, int y) {
        try {
            List<Object> row = rows.get(y);
            return row.get(x);
        } catch (Exception e) {
            return null;
        }
    }

    public int getHeight() {
        return rows.size();
    }

    public void setColumnSeparator(String newColumnSeparator) {
        this.columnSeparator = newColumnSeparator;
    }

    public int[] calculateWidths() {
        int cols = 0;
        int[] widths = new int[1024];
        for (List<Object> row : rows) {
            for (int j = 0; j < row.size(); j++) {
                widths[j] = Math.max(widths[j],
                        row.get(j) == null || row.get(j) == getClass() ? 0 : row.get(j).toString().length());
            }
            cols = Math.max(cols, row.size());
        }
        // put total width in last index
        for (int i = 0; i < widths.length - 1; i++) {
            widths[widths.length - 1] += widths[i];
        }
        widths[widths.length - 1] += cols * columnSeparator.length() - columnSeparator.length();
        return widths;
    }

    @Override
    public String toString() {
        return toString(new int[0]);
    }

    public String toString(int[] rightAlignedColumns) {
        int[] widths = calculateWidths();
        Arrays.sort(rightAlignedColumns);
        StringBuilder buffer = new StringBuilder();
        for (List<Object> row : rows) {
            for (int j = 0; j < row.size(); j++) {
                Object text = row.get(j);
                if (text == getClass()) {
                    buffer.append(new String(new char[widths[widths.length - 1]]).replace((char) 0, '-'));
                    break;
                }
                buffer.append(toString(text, widths[j], Arrays.binarySearch(rightAlignedColumns, j) >= 0));
                if (j + 1 < row.size()) {
                    buffer.append(columnSeparator);
                }
            }
            buffer.append('\n');
        }
        return buffer.toString();
    }

    protected String toString(Object text, int length, boolean rightAligned) {
        text = text == null ? "" : text.toString();
        String fill = new String(new char[length]).replace((char) 0, ' ');
        if (rightAligned) {
            return (fill + text).substring(text.toString().length());
        } else {
            return (text + fill).substring(0, length);
        }
    }
}
