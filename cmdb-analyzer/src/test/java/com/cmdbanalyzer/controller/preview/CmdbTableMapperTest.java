package com.cmdbanalyzer.controller.preview;

import com.cmdbanalyzer.model.CmdbBlock;
import com.cmdbanalyzer.model.CmdbSheet;
import com.cmdbanalyzer.model.CmdbWorkbook;
import com.cmdbanalyzer.model.ConfigurationItem;
import com.cmdbanalyzer.model.ParserWarning;
import com.cmdbanalyzer.model.Relationship;
import com.cmdbanalyzer.model.RelationshipStatus;
import com.cmdbanalyzer.model.SheetType;
import com.cmdbanalyzer.model.WarningSeverity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CmdbTableMapperTest {

    private final CmdbTableMapper mapper = new CmdbTableMapper();

    @Test
    void mapsWorkbookSummaryAndTableRows() {
        ConfigurationItem item = new ConfigurationItem(
                "ci-1",
                "Billing Service",
                "Application",
                "Revenue workflow",
                Map.of("owner", "Finance"),
                "sample.xlsx",
                "Applications",
                4,
                null
        );
        Relationship relationship = new Relationship(
                "rel-1",
                "ci-1",
                null,
                "Database Cluster",
                "depends on",
                "Depends On",
                "sample.xlsx",
                "Applications",
                5,
                RelationshipStatus.UNRESOLVED
        );
        ParserWarning warning = new ParserWarning(
                WarningSeverity.WARNING,
                "Target CI is unresolved",
                "sample.xlsx",
                "Applications",
                5,
                "Parent CI",
                "Database Cluster"
        );

        CmdbSheet sheet = new CmdbSheet();
        sheet.setName("Applications");
        sheet.setType(SheetType.CI_BLOCK);
        sheet.setHeaderRowIndex(2);
        sheet.addCiBlock(new CmdbBlock(4, List.of(5), 6, "Applications"));
        sheet.addConfigurationItem(item);
        sheet.addRelationship(relationship);
        sheet.addWarning(warning);

        CmdbWorkbook workbook = new CmdbWorkbook("sample.xlsx", Instant.now(), new ArrayList<>(List.of(sheet)), List.of(warning));

        ImportPreviewViewModel viewModel = mapper.toViewModel(workbook);

        assertEquals("sample.xlsx", viewModel.workbookName());
        assertEquals(1, viewModel.sheetCount());
        assertEquals(1, viewModel.ciCount());
        assertEquals(1, viewModel.relationshipCount());
        assertEquals(1, viewModel.warningCount());
        assertEquals("Applications", viewModel.sheets().get(0).name());
        assertEquals("Billing Service", viewModel.configurationItems().get(0).name());
        assertEquals("Billing Service", viewModel.relationships().get(0).sourceCiDisplay());
        assertEquals("WARNING", viewModel.warnings().get(0).severity());
    }

    @Test
    void fallsBackToSourceCiIdWhenSourceNameIsUnknown() {
        Relationship relationship = new Relationship(
                "rel-1",
                "missing-ci",
                null,
                "Unknown Target",
                "",
                "",
                "sample.xlsx",
                "Relationships",
                9,
                RelationshipStatus.MALFORMED
        );

        CmdbSheet sheet = new CmdbSheet();
        sheet.setName("Relationships");
        sheet.addRelationship(relationship);
        CmdbWorkbook workbook = new CmdbWorkbook("sample.xlsx", Instant.now(), List.of(sheet), List.of());

        ImportPreviewViewModel viewModel = mapper.toViewModel(workbook);

        assertEquals("missing-ci", viewModel.relationships().get(0).sourceCiDisplay());
        assertEquals("MALFORMED", viewModel.relationships().get(0).status());
    }
}
