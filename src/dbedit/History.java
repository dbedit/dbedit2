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
import java.util.List;

public class History {

    private int offset = -1;
    private List<String> history = new ArrayList<String>();

    public boolean hasPrevious() {
        return offset > 0;
    }

    public boolean hasNext() {
        return offset < history.size() - 1;
    }

    public String previous() {
        return history.get(--offset);
    }

    public String next() {
        return history.get(++offset);
    }

    public void add(String text) {
        offset = history.size() - 1;
        // don't add empty text or text that just has been added
        if (!text.trim().isEmpty() && (history.isEmpty() || !text.equals(history.get(offset)))) {
            history.add(text);
            offset++;
        }
    }
}
