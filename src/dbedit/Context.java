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

import java.io.File;
import java.sql.ResultSet;

public final class Context {

    private static final Context CONTEXT = new Context();

    private ConnectionData connectionData;
    private ResultSet resultSet;
    private int[] columnTypes;
    private String[] columnTypeNames;
    private String query;
    private File openedFile;
    private byte[][] savedLobs;
    private int fetchLimit = 0;

    private Context() {
    }

    public static Context getInstance() {
        return CONTEXT;
    }

    public ConnectionData getConnectionData() {
        return connectionData;
    }

    public void setConnectionData(ConnectionData newConnectionData) {
        connectionData = newConnectionData;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public void setResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public int[] getColumnTypes() {
        return columnTypes;
    }

    public void setColumnTypes(int[] columnTypes) {
        this.columnTypes = columnTypes;
    }

    public String[] getColumnTypeNames() {
        return columnTypeNames;
    }

    public void setColumnTypeNames(String[] columnTypeNames) {
        this.columnTypeNames = columnTypeNames;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public File getOpenedFile() {
        return openedFile;
    }

    public void setOpenedFile(File openedFile) {
        this.openedFile = openedFile;
    }

    public byte[][] getSavedLobs() {
        return savedLobs;
    }

    public void setSavedLobs(byte[][] savedLobs) {
        this.savedLobs = savedLobs;
    }

    public int getFetchLimit() {
        return fetchLimit;
    }

    public void setFetchLimit(int fetchLimit) {
        this.fetchLimit = fetchLimit;
    }
}
