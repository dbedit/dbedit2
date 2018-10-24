package dbedit.actions;

import dbedit.ApplicationPanel;
import dbedit.Dialog;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.util.List;

public class ExportExcelAction extends CustomAction {

    protected ExportExcelAction() {
        super("Excel", "spreadsheet.png", null);
    }

    protected void performThreaded(ActionEvent e) throws Exception {
        boolean selection = false;
        JTable table = ApplicationPanel.getInstance().getTable();
        if (table.getSelectedRowCount() > 0 && table.getSelectedRowCount() != table.getRowCount()) {
            Object option = Dialog.show("Excel", "Export", Dialog.QUESTION_MESSAGE,
                    new Object[] {"Everything", "Selection"}, "Everything");
            if (option == null) {
                return;
            }
            selection = "Selection".equals(option);
        }
        List list = ((DefaultTableModel) table.getModel()).getDataVector();
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        HSSFRow row = sheet.createRow(0);
        HSSFCellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(HSSFColor.GREY_40_PERCENT.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        HSSFFont font = workbook.createFont();
        font.setColor(HSSFColor.WHITE.index);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        style.setFont(font);
        for (int i = 0; i < table.getColumnCount(); i++) {
            HSSFCell cell = row.createCell((short) i);
            cell.setCellValue(new HSSFRichTextString(table.getColumnName(i)));
            cell.setCellStyle(style);
            sheet.setColumnWidth((short) i, (short) (table.getColumnModel().getColumn(i).getPreferredWidth() * 45));
        }
        int count = 1;
        for (int i = 0; i < list.size(); i++) {
            if (!selection || table.isRowSelected(i)) {
                List data = (List) list.get(i);
                row = sheet.createRow(count++);
                for (int j = 0; j < data.size(); j++) {
                    Object o = data.get(j);
                    HSSFCell cell = row.createCell((short) j);
                    if (o instanceof Number) {
                        cell.setCellValue(((Number) o).doubleValue());
                    } else if (o != null) {
                        if (CustomAction.isLob(j)) {
                            cell.setCellValue(new HSSFRichTextString(getColumnTypeNames()[j]));
                        } else {
                            cell.setCellValue(new HSSFRichTextString(o.toString()));
                        }
                    }
                }
            }
        }
        sheet.createFreezePane(0, 1);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);
        openFile("export", ".xls", byteArrayOutputStream.toByteArray());
    }
}
