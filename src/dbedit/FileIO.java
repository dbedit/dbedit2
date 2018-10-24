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

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public final class FileIO {

    private static JFileChooser fileChooser;

    private FileIO() {
    }

    public static void saveAndOpenFile(String fileName, byte[] bytes) throws Exception {
        File file = saveFile(fileName, bytes);
        if (file != null && Dialog.YES_OPTION == Dialog.show(
                "Open file", "Open the file with the associated application?",
                Dialog.QUESTION_MESSAGE, Dialog.YES_NO_OPTION)) {
            openFile(file);
        }
    }

    public static File saveFile(String fileName, byte[] bytes) throws Exception {
        JFileChooser fileChooser = getFileChooser();
        fileChooser.setSelectedFile(new File(fileName));
        if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(ApplicationPanel.getInstance())) {
            Config.saveLastUsedDir(fileChooser.getCurrentDirectory().getCanonicalPath());
            File selectedFile = fileChooser.getSelectedFile();
            if (!selectedFile.exists() || Dialog.YES_OPTION == Dialog.show("File exists",
                    "Overwrite existing file?", Dialog.QUESTION_MESSAGE, Dialog.YES_NO_OPTION)) {
                writeFile(selectedFile, bytes);
                return selectedFile;
            }
        }
        return null;
    }

    public static void writeFile(File file, byte[] bytes) throws Exception {
        FileOutputStream out = new FileOutputStream(file);
        out.write(bytes);
        out.close();
    }

    public static void openFile(File file) throws IOException {
        Desktop.getDesktop().open(file);
    }

    public static File openFile() throws Exception {
        JFileChooser fileChooser = getFileChooser();
        if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(ApplicationPanel.getInstance())) {
            Config.saveLastUsedDir(fileChooser.getCurrentDirectory().getCanonicalPath());
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    public static byte[] readFile(File file) throws Exception {
        FileInputStream in = new FileInputStream(file);
        byte[] b = new byte[in.available()];
        in.read(b);
        in.close();
        return b;
    }

    protected static JFileChooser getFileChooser() throws Exception {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
        }
        String dir = Config.getLastUsedDir();
        if (dir != null) {
            fileChooser.setCurrentDirectory(new File(dir));
        }
        return fileChooser;
    }
}
