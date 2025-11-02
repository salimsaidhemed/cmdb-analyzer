package com.cmdb.analyzer.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CITest {
    @Test
    void testCreateCIAndAttributes() {
        CI ci = new CI("CI-001", "Server", "AppServer01");
        ci.setLocation("EU");
        ci.setProject("FactoryNet");

        assertEquals("CI-001", ci.getId());
        assertEquals("Server", ci.getCiClass());
        assertEquals("AppServer01", ci.getName());
        assertEquals("EU", ci.getLocation());
        assertEquals("FactoryNet", ci.getProject());
    }

    @Test
    void testEqualsAndHashcode() {
        CI ci1 = new CI("CI-002", "Switch", "SW01");
        CI ci2 = new CI("CI-002", "Switch", "SW01");
        assertEquals(ci1, ci2);
        assertEquals(ci1.hashCode(), ci2.hashCode());
    }

    @Test
    void testToString() {
        CI ci = new CI("CI-003", "Database", "DB01");
        assertTrue(ci.toString().contains("DB01"));
        assertTrue(ci.toString().contains("Database"));
    }

}
