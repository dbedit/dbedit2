/*
 * DBEdit 2
 * Copyright (C) 2006-2010 Jef Van Den Ouweland
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
package dbedit.actions;

import dbedit.Dialog;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class FetchLimitAction extends CustomAction {

    protected FetchLimitAction() {
        super("Fetch limit = unlimited", "empty.png", null);
        setEnabled(true);
    }

    @Override
    protected void performThreaded(ActionEvent e) throws Exception {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(getFetchLimit(), 0, 999999, 1));
        if (Dialog.OK_OPTION == Dialog.show("Fetch limit", spinner, Dialog.QUESTION_MESSAGE, Dialog.OK_CANCEL_OPTION)) {
            setFetchLimit(((Number) spinner.getValue()).intValue());
        }
        putValue(NAME, String.format("Fetch limit = %s",
                getFetchLimit() == 0 ? "unlimited" : String.valueOf(getFetchLimit())));
    }
}
