package dbedit;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

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
import java.security.Key;
import java.security.PrivateKey;
import java.text.ParseException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: ouwenlj
 * Date: 11-aug-2005
 * Time: 14:50:17
 */
public class Config {

    public static final String HOME_PAGE = "http://dbedit2.sourceforge.net/";
    private static String version;

    private static final Key KEY = new PrivateKey() {
        public byte[] getEncoded() {
            return "$GeHeiM^".getBytes();
        }
        public String getAlgorithm() {
            return "DES";
        }
        public String getFormat() {
            return "RAW";
        }
    };

    public static Vector getDatabases() throws Exception {
        Element config = getConfig();
        return getDatabases(config);
    }

    public static Vector getDatabases(Element config) throws Exception {
        Vector connectionDatas = new Vector();
        NodeList nodeList = config.getElementsByTagName("database");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            connectionDatas.add(new ConnectionData(
                    element.getAttribute("name"),
                    element.getAttribute("connection"),
                    element.getAttribute("user"),
                    Config.decrypt(element.getAttribute("password")),
                    element.getAttribute("driver"),
                    element.getAttribute("defaultOwner")
            ));
        }
        return connectionDatas;
    }

    public static void saveDatabases(List connectionDatas) throws Exception {
        Collections.sort(connectionDatas);
        Element config = getConfig();
        NodeList nodeList = config.getElementsByTagName("database");
        for (int i = nodeList.getLength() - 1; i > -1; i--) {
            config.removeChild(nodeList.item(i));
        }
        for (int i = 0; i < connectionDatas.size(); i++) {
            Element element = config.getOwnerDocument().createElement("database");
            ConnectionData connectionData = (ConnectionData) connectionDatas.get(i);
            element.setAttribute("name", connectionData.getName());
            element.setAttribute("user", connectionData.getUser());
            element.setAttribute("password", Config.encrypt(connectionData.getPassword()));
            element.setAttribute("connection", connectionData.getUrl());
            element.setAttribute("driver", connectionData.getDriver());
            element.setAttribute("defaultOwner", connectionData.getDefaultOwner());
            config.appendChild(element);
        }
        Config.saveConfig(config);
    }

    public static Map getFavorites() throws ParserConfigurationException, IOException, SAXException {
        Element config = getConfig();
        NodeList favorites = config.getElementsByTagName("favorite");
        Map map = new TreeMap();
        for (int i = 0; i < favorites.getLength(); i++) {
            Element favorite = (Element) favorites.item(i);
            map.put(favorite.getAttribute("name"), favorite.getAttribute("query"));
        }
        return map;
    }

    public static void saveFavorites(Map favorites) throws ParserConfigurationException, IOException, TransformerException, SAXException {
        Element config = getConfig();
        NodeList nodeList = config.getElementsByTagName("favorite");
        for (int i = nodeList.getLength() - 1; i > -1; i--) {
            config.removeChild(nodeList.item(i));
        }
        Iterator iterator = favorites.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Element favorite = config.getOwnerDocument().createElement("favorite");
            favorite.setAttribute("name", (String) entry.getKey());
            favorite.setAttribute("query", (String) entry.getValue());
            config.appendChild(favorite);
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
        for (int i = childNodes.getLength() - 1; i > -1; i--) if (childNodes.item(i).getNodeType() == Node.TEXT_NODE) config.removeChild(childNodes.item(i));
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(new DOMSource(config), new StreamResult(new ByteArrayOutputStream())); // test first
        transformer.transform(new DOMSource(config), new StreamResult(new File(System.getProperty("user.home"), "dbedit.xml")));

        // previous version
        new File(System.getProperty("user.home"), "query.xml").delete();
    }

    protected static String decrypt(String encrypted) throws Exception {
        if (encrypted == null || "".equals(encrypted)) return encrypted;
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, KEY);
        return new String(cipher.doFinal(new BASE64Decoder().decodeBuffer(encrypted)));

    }

    protected static String encrypt(String decrypted) throws Exception {
        if (decrypted == null || "".equals(decrypted)) return decrypted;
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, KEY);
        return new BASE64Encoder().encode(cipher.doFinal(decrypted.getBytes()));
    }

    public static String getVersion() throws IOException, ParseException {
        if (version == null) {
            version = new BufferedReader(new InputStreamReader(Config.class.getResourceAsStream("/changes"))).readLine();
        }
        return version;
    }
}
