package com.cmdbanalyzer.parser;

import java.util.List;
import java.util.Map;

/**
 * Parser-neutral description of a worksheet.
 *
 * <p>This type intentionally avoids Apache POI and other Excel-library types. Sample rows are
 * represented as one-based column indexes mapped to display values so classifiers and header
 * detectors can inspect worksheet shape without depending on workbook internals.</p>
 *
 * @param workbookName source workbook name
 * @param sheetName worksheet name
 * @param rowCount worksheet row count reported by the adapter
 * @param columnCount worksheet column count reported by the adapter
 * @param sampleRows representative rows keyed by one-based column index
 */
public record SheetDescriptor(
        String workbookName,
        String sheetName,
        int rowCount,
        int columnCount,
        List<Map<Integer, String>> sampleRows
) {
    public SheetDescriptor {
        sampleRows = (sampleRows == null ? List.<Map<Integer, String>>of() : sampleRows)
                .stream()
                .map(Map::copyOf)
                .toList();
    }
}
