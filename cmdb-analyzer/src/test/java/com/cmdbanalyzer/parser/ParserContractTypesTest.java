package com.cmdbanalyzer.parser;

import com.cmdbanalyzer.model.ParserWarning;
import com.cmdbanalyzer.model.WarningSeverity;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParserContractTypesTest {

    @Test
    void parseResultCapturesSuccessWithWarnings() {
        ParserWarning warning = new ParserWarning(
                WarningSeverity.WARNING,
                "Unknown sheet type",
                "sample.xlsx",
                "Sheet1",
                1,
                "A",
                "value"
        );

        ParseResult<String> result = ParseResult.success("parsed", List.of(warning));

        assertTrue(result.success());
        assertEquals("parsed", result.result());
        assertNull(result.errorMessage());
        assertEquals(1, result.warnings().size());
    }

    @Test
    void parseResultCapturesFailureWithoutMutableWarningLeak() {
        List<ParserWarning> warnings = new ArrayList<>();
        ParseResult<String> result = ParseResult.failure("Could not read workbook", warnings);

        warnings.add(new ParserWarning());

        assertFalse(result.success());
        assertNull(result.result());
        assertEquals("Could not read workbook", result.errorMessage());
        assertTrue(result.warnings().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> result.warnings().add(new ParserWarning()));
    }

    @Test
    void headerDetectionResultCopiesHeaderMapAndWarnings() {
        ParserWarning warning = new ParserWarning(WarningSeverity.INFO, "Header found", null, null, null, null, null);
        HeaderDetectionResult result = new HeaderDetectionResult(
                2,
                Map.of("name", 3, "class", 2),
                List.of(warning)
        );

        assertEquals(2, result.headerRowIndex());
        assertEquals(3, result.headerMap().get("name"));
        assertEquals(1, result.warnings().size());
        assertThrows(UnsupportedOperationException.class, () -> result.headerMap().put("description", 4));
    }

    @Test
    void sheetDescriptorCopiesSampleRows() {
        SheetDescriptor descriptor = new SheetDescriptor(
                "sample.xlsx",
                "Applications",
                10,
                5,
                List.of(Map.of(2, "Class", 3, "Name"))
        );

        assertEquals("sample.xlsx", descriptor.workbookName());
        assertEquals("Applications", descriptor.sheetName());
        assertEquals(10, descriptor.rowCount());
        assertEquals("Name", descriptor.sampleRows().get(0).get(3));
        assertThrows(UnsupportedOperationException.class, () -> descriptor.sampleRows().get(0).put(4, "Description"));
    }

    @Test
    void relationshipResolutionResultCapturesCounts() {
        RelationshipResolutionResult result = new RelationshipResolutionResult(
                8,
                2,
                1,
                List.of(new ParserWarning())
        );

        assertEquals(8, result.resolvedCount());
        assertEquals(2, result.unresolvedCount());
        assertEquals(1, result.malformedCount());
        assertEquals(1, result.warnings().size());
    }
}
