package com.cmdb.analyzer.core;

import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic behavioral and thread-safety tests for CMDB core models.
 */
public class ModelTests {

    @Test
    void testCIEqualityAndToString() {
        CI ci1 = new CI("CI-001", "Server", "App01");
        CI ci2 = new CI("CI-001", "Server", "App01");
        assertEquals(ci1, ci2);
        assertTrue(ci1.toString().contains("App01"));
    }

    @Test
    void testCIMutabilityThreadSafety() throws InterruptedException {
        CI ci = new CI("CI-002", "Database", "DB01");
        ExecutorService exec = Executors.newFixedThreadPool(4);

        for (int i = 0; i < 100; i++) {
            exec.submit(() -> ci.putAttribute(Thread.currentThread().getName(), "ok"));
        }
        exec.shutdown();
        assertTrue(exec.awaitTermination(3, TimeUnit.SECONDS));
        assertTrue(ci.getAttributes().size() > 0);
    }

    @Test
    void testRelationshipLinking() {
        CI app = new CI("CI-1", "App", "DMS Frontend");
        CI db = new CI("CI-2", "DB", "DMS Database");
        Relationship r = new Relationship(app, db, "Depends on");

        assertEquals(app, r.getSource());
        assertEquals(db, r.getTarget());
        assertEquals("Depends on", r.getType());
    }

    @Test
    void testRelationshipThreadSafety() throws InterruptedException {
        CI src = new CI("CI-100", "Server", "App");
        CI tgt = new CI("CI-200", "DB", "DB01");
        Relationship r = new Relationship(src, tgt, "Uses");

        ExecutorService exec = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 100; i++) {
            exec.submit(() -> {
                r.setType("Depends on");
                r.setType("Uses");
                assertNotNull(r.toString());
            });
        }
        exec.shutdown();
        assertTrue(exec.awaitTermination(3, TimeUnit.SECONDS));
    }

    @Test
    void testBusinessServiceAndOfferings() {
        BusinessService bs = new BusinessService("BS-1", "Document Mgmt");
        ServiceOffering so = new ServiceOffering("SO-1", "DMS Portal");
        bs.addServiceOffering(so);

        assertEquals("Document Mgmt", bs.getName());
        assertEquals(1, bs.getServiceOfferings().size());
        assertEquals(so, bs.getServiceOfferings().get(0));
    }

    @Test
    void testServiceOfferingLinkage() {
        BusinessService bs = new BusinessService("BS-2", "Factory API");
        ServiceOffering so = new ServiceOffering("SO-2", "API Gateway");
        so.setBusinessService(bs);
        bs.addServiceOffering(so);
        assertEquals(bs, so.getBusinessService());
    }

    @Test
    void testProjectGrouping() {
        Project project = new Project("FactoryNet");
        CI ci = new CI("CI-10", "PLC", "PLC-001");
        project.addCI(ci);

        assertEquals("FactoryNet", project.getName());
        assertEquals(1, project.getCis().size());
        assertEquals(ci, project.getCis().get(0));
    }

    @Test
    void testProjectThreadSafety() throws InterruptedException {
        Project project = new Project("FactoryNet");
        ExecutorService exec = Executors.newFixedThreadPool(6);

        for (int i = 0; i < 100; i++) {
            int idx = i;
            exec.submit(() -> project.addCI(new CI("CI-" + idx, "Server", "Srv" + idx)));
            exec.submit(() -> project.setLocation("Site-" + idx));
        }

        exec.shutdown();
        exec.awaitTermination(3, TimeUnit.SECONDS);

        assertTrue(project.getCis().size() > 0);
        assertNotNull(project.getLocation());
        assertTrue(project.toString().contains("ciCount"));
    }

    @Test
    void testValidationFinding() {
        ValidationFinding f = new ValidationFinding();
        f.setSeverity(ValidationFinding.Severity.WARNING);
        f.setCode(ValidationFinding.Code.MISSING_PARENT);
        f.setMessage("Missing parent for CI-001");
        assertTrue(f.toString().contains("MISSING_PARENT"));
    }

    @Test
    void testCMDBDatasetThreadSafety() throws InterruptedException {
        CMDBDataset ds = new CMDBDataset();
        ExecutorService exec = Executors.newFixedThreadPool(8);

        for (int i = 0; i < 100; i++) {
            int idx = i;
            exec.submit(() -> ds.addCI(new CI("CI-" + idx, "Server", "Srv" + idx)));
        }
        exec.shutdown();
        exec.awaitTermination(3, TimeUnit.SECONDS);
        assertEquals(100, ds.getConfigurationItems().size());
    }

    @Test
    void testConcurrentAddsToDataset() throws InterruptedException {
        CMDBDataset dataset = new CMDBDataset();
        ExecutorService pool = Executors.newFixedThreadPool(8);

        for (int i = 0; i < 200; i++) {
            int idx = i;
            pool.submit(() -> dataset.addCI(new CI("CI-" + idx, "Server", "Srv" + idx)));
        }

        pool.shutdown();
        pool.awaitTermination(3, TimeUnit.SECONDS);

        assertEquals(200, dataset.getConfigurationItems().size());
        assertTrue(dataset.toString().contains("CIs=200"));
    }

    @Test
    void testImportMetadataThreadSafety() throws InterruptedException {
        ImportMetadata meta = new ImportMetadata();
        ExecutorService pool = Executors.newFixedThreadPool(4);

        for (int i = 0; i < 100; i++) {
            int idx = i;
            pool.submit(() -> {
                meta.setRowIndexEntity(idx);
                meta.setRowIndexRelation(idx + 1);
                assertNotNull(meta.toString());
            });
        }

        pool.shutdown();
        assertTrue(pool.awaitTermination(3, TimeUnit.SECONDS));
        assertTrue(meta.getRowIndexRelation() >= 0);
    }

    @Test
    void testBusinessServiceConcurrency() throws InterruptedException {
        BusinessService bs = new BusinessService("BS-1", "Factory Systems");
        ExecutorService exec = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 50; i++) {
            int idx = i;
            exec.submit(() -> bs.addServiceOffering(new ServiceOffering("SO-" + idx, "Offer" + idx)));
            exec.submit(() -> bs.addDependency(new CI("CI-" + idx, "Server", "Srv" + idx)));
        }
        exec.shutdown();
        exec.awaitTermination(3, TimeUnit.SECONDS);
        assertTrue(bs.getServiceOfferings().size() > 0);
        assertTrue(bs.getDependsOn().size() > 0);
    }

    @Test
    void testValidationFindingThreadSafety() throws InterruptedException {
        ValidationFinding vf = new ValidationFinding();
        ExecutorService pool = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 100; i++) {
            int idx = i;
            pool.submit(() -> vf.putContext("k" + idx, "v" + idx));
        }
        pool.shutdown();
        pool.awaitTermination(3, TimeUnit.SECONDS);
        assertEquals(100, vf.getContext().size());
    }

    @Test
    void testReferenceCatalogConcurrency() throws InterruptedException {
        ReferenceCatalog ref = new ReferenceCatalog();
        ExecutorService pool = Executors.newFixedThreadPool(4);

        for (int i = 0; i < 200; i++) {
            int idx = i;
            pool.submit(() -> {
                ref.addValidClass("Class" + idx);
                ref.addValidRelationshipType("Type" + idx);
                ref.addValidLocation("Loc" + idx);
                ref.addValidEnvironment("Env" + idx);
            });
        }

        pool.shutdown();
        pool.awaitTermination(3, TimeUnit.SECONDS);

        assertTrue(ref.getValidClasses().size() > 100);
        assertTrue(ref.isValidClass("Class50"));
        assertTrue(ref.toString().contains("classes"));
    }

}
