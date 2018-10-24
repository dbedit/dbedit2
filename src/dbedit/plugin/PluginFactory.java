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
package dbedit.plugin;

import java.util.Properties;

public final class PluginFactory {

    private static Plugin plugin;


    private PluginFactory() {
    }

    public static Plugin getPlugin() {
        if (plugin == null) {
            try {
                Properties plugins = new Properties();
                plugins.load(PluginFactory.class.getResourceAsStream("plugins.properties"));
                plugin = (Plugin) Class.forName(plugins.getProperty("plugin")).newInstance();
            } catch (Exception e) {
                plugin = new DefaultPlugin();
            }
        }
        return plugin;
    }
}
