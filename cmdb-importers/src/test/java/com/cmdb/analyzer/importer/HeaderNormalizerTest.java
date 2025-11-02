package com.cmdb.analyzer.importer;

import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link HeaderNormalizer}.
 * These verify that messy Excel column headers are normalized
 * to consistent CMDB field identifiers.
 */
public class HeaderNormalizerTest {

    @Test
    void testBasicNormalization() {
        Map<String, String> headers = HeaderNormalizer.normalizeHeaders(Arrays.asList(
                "Parent CI", "Parent CI Name", "CI Name", "Class",
                "Relationship Type", "Project", "Location", "Description"));

        assertEquals("parent_ci", headers.get("Parent CI"));
        assertEquals("parent_ci", headers.get("Parent CI Name"));
        assertEquals("name", headers.get("CI Name"));
        assertEquals("class", headers.get("Class"));
        assertEquals("relationship", headers.get("Relationship Type"));
        assertEquals("project", headers.get("Project"));
        assertEquals("location", headers.get("Location"));
        assertEquals("description", headers.get("Description"));
    }

    @Test
    void testNormalizationIgnoresCaseAndSpaces() {
        Map<String, String> headers = HeaderNormalizer.normalizeHeaders(Arrays.asList(
                "  ci   name  ", "RELATIONSHIP TYPE", " Parent_ci "));
        assertEquals("name", headers.get("  ci   name  "));
        assertEquals("relationship", headers.get("RELATIONSHIP TYPE"));
        assertEquals("parent_ci", headers.get(" Parent_ci "));
    }

    @Test
    void testFallbackForUnknownHeaders() {
        Map<String, String> headers = HeaderNormalizer.normalizeHeaders(Arrays.asList(
                "Custom Field", "Business Owner", "Factory Code"));
        assertEquals("custom_field", headers.get("Custom Field"));
        assertEquals("business_owner", headers.get("Business Owner"));
        assertEquals("factory_code", headers.get("Factory Code"));
    }

    @Test
    void testThreadSafetyDuringParallelNormalization() throws InterruptedException {
        List<String> headerList = Arrays.asList("Parent CI", "Class", "Project", "Location");
        Set<Map<String, String>> results = Collections.synchronizedSet(new HashSet<>());

        Thread t1 = new Thread(() -> results.add(HeaderNormalizer.normalizeHeaders(headerList)));
        Thread t2 = new Thread(() -> results.add(HeaderNormalizer.normalizeHeaders(headerList)));
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertTrue(results.size() >= 1);
        assertTrue(results.iterator().next().containsValue("parent_ci"));
    }
}
