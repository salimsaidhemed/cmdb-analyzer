package com.cmdbanalyzer.parser.poi;

import com.cmdbanalyzer.model.CmdbSheet;
import com.cmdbanalyzer.model.CmdbWorkbook;
import com.cmdbanalyzer.model.RelationshipStatus;
import com.cmdbanalyzer.model.SheetType;
import com.cmdbanalyzer.parser.ParseResult;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PoiWorkbookParserTest {

    @Test
    void parsesBasicCiBlockWorkbook() throws Exception {
        Path workbookPath = Files.createTempFile("cmdb-parser-test", ".xlsx");
        try (Workbook workbook = new XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Applications");
            Row header = sheet.createRow(0);
            header.createCell(1).setCellValue("Class");
            header.createCell(2).setCellValue("Name");
            header.createCell(3).setCellValue("Description");
            header.createCell(4).setCellValue("Parent CI");
            header.createCell(5).setCellValue("Relationship");

            Row ciRow = sheet.createRow(1);
            ciRow.createCell(1).setCellValue("cmdb_ci_appl");
            ciRow.createCell(2).setCellValue("App01");
            ciRow.createCell(3).setCellValue("Application");

            Row relationshipRow = sheet.createRow(2);
            relationshipRow.createCell(1).setCellValue("cmdb_ci_appl");
            relationshipRow.createCell(2).setCellValue("App01");
            relationshipRow.createCell(3).setCellValue("Application");
            relationshipRow.createCell(4).setCellValue("Service01");
            relationshipRow.createCell(5).setCellValue("Depends on");

            Row malformedRelationshipRow = sheet.createRow(3);
            malformedRelationshipRow.createCell(1).setCellValue("cmdb_ci_appl");
            malformedRelationshipRow.createCell(2).setCellValue("App01");
            malformedRelationshipRow.createCell(3).setCellValue("Application");
            malformedRelationshipRow.createCell(4).setCellValue("VM01");

            sheet.createRow(4);

            try (OutputStream outputStream = Files.newOutputStream(workbookPath)) {
                workbook.write(outputStream);
            }
        }

        ParseResult<CmdbWorkbook> result = new PoiWorkbookParser().parse(workbookPath);

        assertTrue(result.success());
        assertNotNull(result.result());
        assertEquals(1, result.result().getSheets().size());
        CmdbSheet sheet = result.result().getSheets().get(0);
        assertEquals(SheetType.CI_BLOCK, sheet.getType());
        assertEquals(1, sheet.getCiBlocks().size());
        assertEquals(1, sheet.getConfigurationItems().size());
        assertEquals("App01", sheet.getConfigurationItems().get(0).getName());
        assertEquals(2, sheet.getRelationships().size());
        assertEquals(RelationshipStatus.UNRESOLVED, sheet.getRelationships().get(0).getStatus());
        assertEquals(RelationshipStatus.MALFORMED, sheet.getRelationships().get(1).getStatus());
        assertFalse(result.warnings().isEmpty());
        assertEquals(result.warnings().size(), result.result().getParserWarnings().size());
    }

    @Test
    void returnsFailureWhenWorkbookCannotBeRead() {
        ParseResult<CmdbWorkbook> result = new PoiWorkbookParser().parse(Path.of("does-not-exist.xlsx"));

        assertFalse(result.success());
        assertTrue(result.errorMessage().contains("Could not open or read workbook"));
    }
}
