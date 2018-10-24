/*
 * DBEdit 2
 * Copyright (C) 2006-2012 Jef Van Den Ouweland
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

import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TableSorter implements MouseListener, Comparator<List> {

    private int col = -1;
    private int mouseButton = -1;

    @Override
    public int compare(List l1, List l2) {
        Object o1 = (l1).get(col);
        Object o2 = (l2).get(col);
        int i = o1 == null && o2 == null ? 0
                : o1 == null ? -1
                : o2 == null ? 1
                : o1 instanceof Number
                ? Double.compare(((Number) o1).doubleValue(), ((Number) o2).doubleValue())
                : o1.toString().compareTo(o2.toString());
        return i * (mouseButton == MouseEvent.BUTTON1 ? 1 : -1);
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        JTableHeader tableHeader = (JTableHeader) e.getSource();
        col = tableHeader.columnAtPoint(e.getPoint());
        mouseButton = e.getButton();
        @SuppressWarnings("unchecked")
        List<List> list = ((DefaultTableModel) tableHeader.getTable().getModel()).getDataVector();
        Collections.sort(list, this);
        ApplicationPanel.getInstance().updateUI();
    }

    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }
}
