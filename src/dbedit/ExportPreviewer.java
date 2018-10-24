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

import javax.swing.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;

public class ExportPreviewer {

    private ExportPreviewer() {
    }

    public static void preview(String text, byte[] bytes) throws Exception {
        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        boolean isXml = text.startsWith("<?xml");
        Object[] options = isXml
            ? new Object[] {"Save to file", "Save to file and open", "Copy to clipboard", "Pretty print XML", "Cancel"}
            : new Object[] {"Save to file", "Save to file and open", "Copy to clipboard", "Cancel"};
        Object value = Dialog.show("Preview", scrollPane, Dialog.PLAIN_MESSAGE, options, "Save to file");
        if ("Save to file".equals(value)) {
            String fileName = isXml ? "export.xml" : "export.txt";
            FileIO.saveFile(fileName, bytes != null ? bytes : text.getBytes());
        } else if ("Save to file and open".equals(value)) {
            String fileName = isXml ? "export.xml" : "export.txt";
            File file = FileIO.saveFile(fileName, bytes != null ? bytes : text.getBytes());
            if (file != null) {
                FileIO.openFile(file);
            }
        } else if ("Copy to clipboard".equals(value)) {
            try {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
            } catch (Throwable t2) {
                ExceptionDialog.hideException(t2);
            }
        } else if ("Pretty print XML".equals(value)) {
            try {
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                transformer.transform(new StreamSource(new StringReader(text)), new StreamResult(outputStream));
                text = outputStream.toString();
            } catch (Throwable t) {
                ExceptionDialog.showException(t);
            }
            preview(text, bytes);
        }
    }
}
