package com.cmdbanalyzer.parser.poi;

import com.cmdbanalyzer.parser.HeaderDetectionResult;
import com.cmdbanalyzer.parser.SheetDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BasicHeaderDetectorTest {

    @Test
    void normalizesKnownHeaderAliases() {
        assertEquals("name", BasicHeaderDetector.normalizeHeaderName(" CI   Name "));
        assertEquals("class", BasicHeaderDetector.normalizeHeaderName("ci class"));
        assertEquals("parent ci", BasicHeaderDetector.normalizeHeaderName("Dependency"));
        assertEquals("relationship", BasicHeaderDetector.normalizeHeaderName("Relationship Type"));
    }

    @Test
    void detectsHeaderRowStartingAfterColumnA() {
        SheetDescriptor descriptor = new SheetDescriptor(
                "sample.xlsx",
                "Applications",
                4,
                6,
                List.of(
                        Map.of(),
                        Map.of(2, "Class", 3, "Name", 4, "Description", 5, "Parent CI", 6, "Relationship"),
                        Map.of(2, "cmdb_ci_appl", 3, "App01", 4, "App", 5, "Service01", 6, "Depends on")
                )
        );

        HeaderDetectionResult result = new BasicHeaderDetector().detectHeaders(descriptor);

        assertEquals(2, result.headerRowIndex());
        assertEquals(2, result.headerMap().get("class"));
        assertEquals(3, result.headerMap().get("name"));
        assertEquals(5, result.headerMap().get("parent ci"));
        assertEquals(6, result.headerMap().get("relationship"));
    }
}
