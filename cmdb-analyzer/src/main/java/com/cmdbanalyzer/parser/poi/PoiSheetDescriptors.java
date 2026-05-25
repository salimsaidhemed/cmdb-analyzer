package com.cmdbanalyzer.parser.poi;

import com.cmdbanalyzer.parser.SheetDescriptor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class PoiSheetDescriptors {

    private PoiSheetDescriptors() {
    }

    static SheetDescriptor fromSheet(String workbookName, Sheet sheet) {
        DataFormatter formatter = new DataFormatter();
        int rowCount = sheet.getLastRowNum() + 1;
        int columnCount = 0;
        List<Map<Integer, String>> rows = new ArrayList<>();

        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            Map<Integer, String> values = new HashMap<>();

            if (row != null) {
                columnCount = Math.max(columnCount, row.getLastCellNum());
                for (int columnIndex = 0; columnIndex < row.getLastCellNum(); columnIndex++) {
                    Cell cell = row.getCell(columnIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    String value = cell == null ? "" : formatter.formatCellValue(cell).trim();
                    if (!value.isBlank()) {
                        values.put(columnIndex + 1, value);
                    }
                }
            }

            rows.add(values);
        }

        return new SheetDescriptor(workbookName, sheet.getSheetName(), rowCount, Math.max(columnCount, 0), rows);
    }
}
