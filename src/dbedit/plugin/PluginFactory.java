/*
 * DBEdit 2
 * Copyright (C) 2006-2009 Jef Van Den Ouweland
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
package dbedit.plugin;

import dbedit.ExceptionDialog;

public final class PluginFactory {

    private static Plugin plugin;


    private PluginFactory() {
    }

    public static Plugin getPlugin() {
        if (plugin == null) {
            try {
                plugin = (Plugin) Class.forName("dbedit.plugin.DuPontPlugin").newInstance();
            } catch (ClassNotFoundException e) {
                plugin = new DefaultPlugin();
            } catch (IllegalAccessException e) {
                ExceptionDialog.hideException(e);
            } catch (InstantiationException e) {
                ExceptionDialog.hideException(e);
            }
        }
        return plugin;
    }
}
