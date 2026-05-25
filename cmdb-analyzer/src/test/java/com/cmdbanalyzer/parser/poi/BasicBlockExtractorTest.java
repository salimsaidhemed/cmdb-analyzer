package com.cmdbanalyzer.parser.poi;

import com.cmdbanalyzer.model.CmdbBlock;
import com.cmdbanalyzer.parser.HeaderDetectionResult;
import com.cmdbanalyzer.parser.SheetDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BasicBlockExtractorTest {

    @Test
    void detectsBlankRows() {
        BasicBlockExtractor extractor = new BasicBlockExtractor();

        assertTrue(extractor.isBlankRow(Map.of()));
        assertTrue(extractor.isBlankRow(Map.of(2, "  ")));
    }

    @Test
    void extractsVariableSizedCiBlockWithBlankSeparator() {
        SheetDescriptor descriptor = new SheetDescriptor(
                "sample.xlsx",
                "Applications",
                5,
                6,
                List.of(
                        Map.of(2, "Class", 3, "Name", 4, "Description", 5, "Parent CI", 6, "Relationship"),
                        Map.of(2, "cmdb_ci_appl", 3, "App01", 4, "App"),
                        Map.of(2, "cmdb_ci_appl", 3, "App01", 4, "App", 5, "Service01", 6, "Depends on"),
                        Map.of(2, "cmdb_ci_appl", 3, "App01", 4, "App", 5, "VM01", 6, "Runs on"),
                        Map.of()
                )
        );
        HeaderDetectionResult headers = new BasicHeaderDetector().detectHeaders(descriptor);

        List<CmdbBlock> blocks = new BasicBlockExtractor().extractBlocks(descriptor, headers);

        assertEquals(1, blocks.size());
        assertEquals(2, blocks.get(0).getCiRowIndex());
        assertEquals(List.of(3, 4), blocks.get(0).getRelationshipRowIndexes());
        assertEquals(5, blocks.get(0).getSeparatorRowIndex());
    }
}
