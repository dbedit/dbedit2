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

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import dbedit.ApplicationPanel;
import dbedit.Dialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.util.List;

public class ExportPdfAction extends CustomAction implements PdfPageEvent {

    private PdfTemplate pdfTemplate;
    private static final Font ROW_HEADER_FONT = new Font(Font.UNDEFINED, 12, Font.BOLD);
    private static final Font FONT = new Font();
    private static final BaseFont ROW_HEADER_BASE_FONT = ROW_HEADER_FONT.getCalculatedBaseFont(false);
    private static final BaseFont BASE_FONT = FONT.getCalculatedBaseFont(false);

    protected ExportPdfAction() {
        super("PDF", "pdf.png", null);
    }

    @Override
    protected void performThreaded(ActionEvent e) throws Exception {
        boolean selection = false;
        JTable table = ApplicationPanel.getInstance().getTable();
        if (table.getSelectedRowCount() > 0 && table.getSelectedRowCount() != table.getRowCount()) {
            Object option = Dialog.show("PDF", "Export", Dialog.QUESTION_MESSAGE,
                    new Object[] {"Everything", "Selection"}, "Everything");
            if (option == null) {
                return;
            }
            selection = "Selection".equals(option);
        }
        List list = ((DefaultTableModel) table.getModel()).getDataVector();
        int columnCount = table.getColumnCount();
        PdfPTable pdfPTable = new PdfPTable(columnCount);
        pdfPTable.setWidthPercentage(100);
        pdfPTable.getDefaultCell().setPaddingBottom(4);
        int[] widths = new int[columnCount];

        // Row Header
        pdfPTable.getDefaultCell().setBorderWidth(2);
        for (int i = 0; i < columnCount; i++) {
            String columnName = table.getColumnName(i);
            pdfPTable.addCell(new Phrase(columnName, ROW_HEADER_FONT));
            widths[i] = Math.min(50000, Math.max(widths[i], ROW_HEADER_BASE_FONT.getWidth(columnName + " ")));
        }
        pdfPTable.getDefaultCell().setBorderWidth(1);
        if (!list.isEmpty()) {
            pdfPTable.setHeaderRows(1);
        }

        // Body
        for (int i = 0; i < list.size(); i++) {
            if (!selection || table.isRowSelected(i)) {
                List record = (List) list.get(i);
                for (int j = 0; j < record.size(); j++) {
                    Object o = record.get(j);
                    if (o != null) {
                        if (CustomAction.isLob(j)) {
                            o = getColumnTypeNames()[j];
                        }
                    } else {
                        o = "";
                    }
                    PdfPCell cell = new PdfPCell(new Phrase(o.toString()));
                    cell.setPaddingBottom(4);
                    if (o instanceof Number) {
                        cell.setHorizontalAlignment(Cell.ALIGN_RIGHT);
                    }
                    pdfPTable.addCell(cell);
                    widths[j] = Math.min(50000, Math.max(widths[j], BASE_FONT.getWidth(o.toString())));
                }
            }
        }

        // Size
        pdfPTable.setWidths(widths);
        int totalWidth = 0;
        for (int width : widths) {
            totalWidth += width;
        }
        com.lowagie.text.Rectangle pageSize = PageSize.A4.rotate();
        pageSize.setRight(pageSize.getRight() * Math.max(1f, totalWidth / 53000f));
        pageSize.setTop(pageSize.getTop() * Math.max(1f, totalWidth / 53000f));

        // Document
        Document document = new Document(pageSize);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);
        document.open();
        pdfTemplate = writer.getDirectContent().createTemplate(100, 100);
        pdfTemplate.setBoundingBox(new com.lowagie.text.Rectangle(-20, -20, 100, 100));
        writer.setPageEvent(this);
        document.add(pdfPTable);
        document.close();
        openFile("export", ".pdf", byteArrayOutputStream.toByteArray());
    }

    /**
     * Print page numbers on right bottom corner
     * @param writer
     * @param document
     */
    public void onEndPage(PdfWriter writer, Document document) {
        PdfContentByte cb = writer.getDirectContent();
        String text = "Page " + writer.getPageNumber() + " of ";
        float textSize = BASE_FONT.getWidthPoint(text, 12);
        float textBase = document.bottom() - 20;
        cb.beginText();
        cb.setFontAndSize(BASE_FONT, 12);
        float adjust = BASE_FONT.getWidthPoint("000", 12);
        cb.setTextMatrix(document.right() - textSize - adjust, textBase);
        cb.showText(text);
        cb.endText();
        cb.addTemplate(pdfTemplate, document.right() - adjust, textBase);
    }

    /**
     * Append total number of pages on each page after the page number
     * @param writer
     * @param document
     */
    public void onCloseDocument(PdfWriter writer, Document document) {
       pdfTemplate.beginText();
       pdfTemplate.setFontAndSize(BASE_FONT, 12);
       pdfTemplate.showText("" + (writer.getPageNumber() - 1));
       pdfTemplate.endText();
    }


    public void onOpenDocument(PdfWriter pdfWriter, Document document) {
    }

    public void onStartPage(PdfWriter pdfWriter, Document document) {
    }

    public void onParagraph(PdfWriter pdfWriter, Document document, float v) {
    }

    public void onParagraphEnd(PdfWriter pdfWriter, Document document, float v) {
    }

    public void onChapter(PdfWriter pdfWriter, Document document, float v, Paragraph paragraph) {
    }

    public void onChapterEnd(PdfWriter pdfWriter, Document document, float v) {
    }

    public void onSection(PdfWriter pdfWriter, Document document, float v, int i, Paragraph paragraph) {
    }

    public void onSectionEnd(PdfWriter pdfWriter, Document document, float v) {
    }

    public void onGenericTag(PdfWriter pdfWriter, Document document, com.lowagie.text.Rectangle rectangle,
                             String string) {
    }
}
