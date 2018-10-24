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

import com.itextpdf.text.pdf.codec.Base64;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.crypto.Cipher;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.PrivateKey;
import java.util.*;

public final class Config {

    public static final String HOME_PAGE = "http://dbedit2.sourceforge.net/";
    private static String version;
    public static final boolean IS_OS_WINDOWS = System.getProperty("os.name").startsWith("Windows");

    private Config() {
    }

    private static final Key KEY = new PrivateKey() {
        @Override
        public byte[] getEncoded() {
            return "$GeHeiM^".getBytes();
        }
        @Override
        public String getAlgorithm() {
            return "DES";
        }
        @Override
        public String getFormat() {
            return "RAW";
        }
    };

    public static Vector<ConnectionData> getDatabases() throws Exception {
        Element config = getConfig();
        return getDatabases(config);
    }

    public static Vector<ConnectionData> getDatabases(Element config) throws Exception {
        Vector<ConnectionData> connectionDatas = new Vector<ConnectionData>();
        NodeList nodeList = config.getElementsByTagName("database");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            connectionDatas.add(new ConnectionData(
                    element.getAttribute("name"),
                    element.getAttribute("connection"),
                    element.getAttribute("user"),
                    Config.decrypt(element.getAttribute("password")),
                    element.getAttribute("defaultOwner")
            ));
        }
        return connectionDatas;
    }

    @SuppressWarnings("unchecked")
    public static void saveDatabases(List<ConnectionData> connectionDatas) throws Exception {
        Collections.sort(connectionDatas);
        Element config = getConfig();
        NodeList nodeList = config.getElementsByTagName("database");
        for (int i = nodeList.getLength() - 1; i > -1; i--) {
            config.removeChild(nodeList.item(i));
        }
        for (ConnectionData connectionData : connectionDatas) {
            Element element = config.getOwnerDocument().createElement("database");
            element.setAttribute("name", connectionData.getName());
            element.setAttribute("user", connectionData.getUser());
            element.setAttribute("password", Config.encrypt(connectionData.getPassword()));
            element.setAttribute("connection", connectionData.getUrl());
            element.setAttribute("defaultOwner", connectionData.getDefaultOwner());
            config.appendChild(element);
        }
        Config.saveConfig(config);
    }

    public static Map<String, String> getFavorites() throws ParserConfigurationException, IOException, SAXException {
        Element config = getConfig();
        NodeList favorites = config.getElementsByTagName("favorite");
        Map<String, String> map = new TreeMap<String, String>();
        for (int i = 0; i < favorites.getLength(); i++) {
            Element favorite = (Element) favorites.item(i);
            map.put(favorite.getAttribute("name"), favorite.getAttribute("query"));
        }
        return map;
    }

    public static void saveFavorites(Map favorites) throws ParserConfigurationException, IOException,
                                                           TransformerException, SAXException {
        Element config = getConfig();
        NodeList nodeList = config.getElementsByTagName("favorite");
        for (int i = nodeList.getLength() - 1; i > -1; i--) {
            config.removeChild(nodeList.item(i));
        }
        for (Object o : favorites.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            Element favorite = config.getOwnerDocument().createElement("favorite");
            favorite.setAttribute("name", (String) entry.getKey());
            favorite.setAttribute("query", (String) entry.getValue());
            config.appendChild(favorite);
        }
        Config.saveConfig(config);
    }

    public static String getLastUsedDir() throws Exception {
        return getSetting("dir");
    }

    public static String getDrivers() throws Exception {
        return getSetting("drivers");
    }

    private static String getSetting(String name) throws Exception {
        Element config = getConfig();
        NodeList list = config.getElementsByTagName("settings");
        if (list.getLength() > 0) {
            Element settings = (Element) list.item(0);
            return settings.getAttribute(name);
        }
        return null;
    }

    public static void saveLastUsedDir(String dir) throws Exception {
        saveSetting("dir", dir);
    }

    public static void saveDrivers(String drivers) throws Exception {
        saveSetting("drivers", drivers);
    }

    private static void saveSetting(String name, String value) throws Exception {
        Element config = getConfig();
        NodeList list = config.getElementsByTagName("settings");
        if (list.getLength() > 0) {
            Element settings = (Element) list.item(0);
            if (value.equals(settings.getAttribute(name))) {
                return;
            }
            settings.setAttribute(name, value);
        } else {
            Element settings = config.getOwnerDocument().createElement("settings");
            settings.setAttribute(name, value);
            config.appendChild(settings);
        }
        Config.saveConfig(config);
    }

    protected static Element getConfig() throws ParserConfigurationException, IOException, SAXException {
        Element config;
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        try {
            InputStream inputStream = new FileInputStream(new File(System.getProperty("user.home"), "dbedit.xml"));
            config = documentBuilder.parse(inputStream).getDocumentElement();
            inputStream.close();
        } catch (Exception e) {
            InputStream inputStream = new ByteArrayInputStream("<config/>".getBytes());
            config = documentBuilder.parse(inputStream).getDocumentElement();
            inputStream.close();
        }
        return config;
    }

    protected static void saveConfig(Element config) throws TransformerException {
        // remove whitespace
        NodeList childNodes = config.getChildNodes();
        for (int i = childNodes.getLength() - 1; i > -1; i--) {
            if (childNodes.item(i).getNodeType() == Node.TEXT_NODE) {
                config.removeChild(childNodes.item(i));
            }
        }
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(new DOMSource(config),
                              new StreamResult(new ByteArrayOutputStream())); // test first
        transformer.transform(new DOMSource(config),
                              new StreamResult(new File(System.getProperty("user.home"), "dbedit.xml")));
    }

    protected static String decrypt(String encrypted) throws GeneralSecurityException {
        if (encrypted == null || "".equals(encrypted)) {
            return encrypted;
        }
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, KEY);
        return new String(cipher.doFinal(Base64.decode(encrypted)));

    }

    protected static String encrypt(String decrypted) throws GeneralSecurityException {
        if (decrypted == null || "".equals(decrypted)) {
            return decrypted;
        }
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, KEY);
        return Base64.encodeBytes(cipher.doFinal(decrypted.getBytes()));
    }

    public static String getVersion() throws IOException {
        if (version == null) {
            version = new BufferedReader(new InputStreamReader(Config.class.getResourceAsStream("/changes.txt")))
                    .readLine();
        }
        return version;
    }
}
