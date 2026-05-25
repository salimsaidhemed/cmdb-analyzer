package com.cmdbanalyzer.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class DomainModelTest {

    @Test
    void configurationItemGeneratesUuidAndIdentityKey() {
        ConfigurationItem item = new ConfigurationItem(
                null,
                "  CD-DMS01  ",
                " cmdb_ci_appl ",
                "DMS application",
                Map.of("Project", "CD-DGDA"),
                " Sample Workbook.xlsx ",
                "Applications",
                12,
                null
        );

        assertNotNull(UUID.fromString(item.getId()));
        assertEquals("sample workbook.xlsx|cmdb_ci_appl|cd-dms01", item.getIdentityKey());
        assertEquals("CD-DGDA", item.getAttributes().get("Project"));
    }

    @Test
    void configurationItemRefreshesIdentityKeyWhenIdentityFieldsChange() {
        ConfigurationItem item = new ConfigurationItem();

        item.setSourceWorkbook("Workbook A.xlsx");
        item.setCiClass("cmdb_ci_vm");
        item.setName("VM-01");

        assertEquals("workbook a.xlsx|cmdb_ci_vm|vm-01", item.getIdentityKey());
    }

    @Test
    void relationshipAllowsNullableTargetAndDefaultsToUnresolved() {
        Relationship relationship = new Relationship();

        relationship.setSourceCiId("source-ci");
        relationship.setTargetName("Missing Target");
        relationship.setRelationshipType("Depends on");
        relationship.setRawRelationshipType("Depends on");

        assertNotNull(UUID.fromString(relationship.getId()));
        assertNull(relationship.getTargetCiId());
        assertEquals(RelationshipStatus.UNRESOLVED, relationship.getStatus());
    }

    @Test
    void workbookSheetAndRowsInitializeCollectionsSafely() {
        CmdbBlock block = new CmdbBlock(2, List.of(3, 4), 5, "Applications");
        FlatRow flatRow = new FlatRow("Business", 2, Map.of("Name", "Service"));
        ParserWarning warning = new ParserWarning(
                WarningSeverity.WARNING,
                "Duplicate CI name",
                "sample.xlsx",
                "Applications",
                12,
                "Name",
                "CD-DMS01"
        );
        CmdbSheet sheet = new CmdbSheet(
                "Applications",
                SheetType.CI_BLOCK,
                1,
                Map.of("name", 2),
                List.of(block),
                List.of(flatRow),
                List.of(warning)
        );
        CmdbWorkbook workbook = new CmdbWorkbook(
                "sample.xlsx",
                Instant.parse("2026-05-26T00:00:00Z"),
                List.of(sheet),
                List.of(warning)
        );

        assertEquals(1, workbook.getSheets().size());
        assertEquals(1, workbook.getParserWarnings().size());
        assertEquals(2, sheet.getHeaderMap().get("name"));
        assertEquals(List.of(3, 4), block.getRelationshipRowIndexes());
        assertEquals("Service", flatRow.getValues().get("Name"));
    }

    @Test
    void defaultConstructorsCreateMutableEmptyCollections() {
        CmdbWorkbook workbook = new CmdbWorkbook();
        CmdbSheet sheet = new CmdbSheet();
        CmdbBlock block = new CmdbBlock();
        FlatRow flatRow = new FlatRow();

        workbook.addSheet(sheet);
        sheet.addCiBlock(block);
        sheet.addFlatRow(flatRow);
        block.addRelationshipRowIndex(10);
        flatRow.getValues().put("Name", "Lookup row");

        assertFalse(workbook.getSheets().isEmpty());
        assertFalse(sheet.getCiBlocks().isEmpty());
        assertFalse(sheet.getFlatRows().isEmpty());
        assertEquals(List.of(10), block.getRelationshipRowIndexes());
        assertEquals("Lookup row", flatRow.getValues().get("Name"));
    }
}
