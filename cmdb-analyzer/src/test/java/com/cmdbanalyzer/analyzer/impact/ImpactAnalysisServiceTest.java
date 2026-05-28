package com.cmdbanalyzer.analyzer.impact;

import com.cmdbanalyzer.graph.CmdbGraphBuilder;
import com.cmdbanalyzer.graph.GraphBuildResult;
import com.cmdbanalyzer.model.CmdbSheet;
import com.cmdbanalyzer.model.CmdbWorkbook;
import com.cmdbanalyzer.model.ConfigurationItem;
import com.cmdbanalyzer.model.Relationship;
import com.cmdbanalyzer.model.RelationshipStatus;
import com.cmdbanalyzer.model.SheetType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.cmdbanalyzer.analyzer.impact.ImpactDirection.BOTH;
import static com.cmdbanalyzer.analyzer.impact.ImpactDirection.DEPENDENCIES;
import static com.cmdbanalyzer.analyzer.impact.ImpactDirection.DEPENDENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImpactAnalysisServiceTest {

    private final CmdbGraphBuilder graphBuilder = new CmdbGraphBuilder();

    @Test
    void dependencyTraversalFindsOutgoingTargets() {
        ImpactAnalysisService service = service(
                List.of(item("app", "APP"), item("db", "DB"), item("storage", "Storage")),
                List.of(
                        relationship("r1", "app", "db"),
                        relationship("r2", "db", "storage")
                )
        );

        ImpactAnalysisResult result = service.analyzeImpact("app", DEPENDENCIES, 3);

        assertEquals(List.of("db", "storage"), result.affectedCis().stream().map(ConfigurationItem::getId).toList());
        assertEquals(2, result.paths().size());
    }

    @Test
    void dependentTraversalFindsIncomingSources() {
        ImpactAnalysisService service = service(
                List.of(item("app", "APP"), item("db", "DB"), item("report", "Report")),
                List.of(
                        relationship("r1", "app", "db"),
                        relationship("r2", "report", "db")
                )
        );

        ImpactAnalysisResult result = service.analyzeImpact("db", DEPENDENTS, 2);

        assertEquals(List.of("app", "report"), result.affectedCis().stream().map(ConfigurationItem::getId).toList());
    }

    @Test
    void bothDirectionTraversalCombinesDependenciesAndDependents() {
        ImpactAnalysisService service = service(
                List.of(item("app", "APP"), item("db", "DB"), item("service", "Service")),
                List.of(
                        relationship("r1", "app", "db"),
                        relationship("r2", "service", "app")
                )
        );

        ImpactAnalysisResult result = service.analyzeImpact("app", BOTH, 1);

        assertEquals(2, result.affectedCis().size());
        assertTrue(result.affectedCis().stream().map(ConfigurationItem::getId).toList().containsAll(List.of("db", "service")));
    }

    @Test
    void maxDepthIsRespected() {
        ImpactAnalysisService service = service(
                List.of(item("app", "APP"), item("db", "DB"), item("storage", "Storage")),
                List.of(
                        relationship("r1", "app", "db"),
                        relationship("r2", "db", "storage")
                )
        );

        ImpactAnalysisResult result = service.analyzeImpact("app", DEPENDENCIES, 1);

        assertEquals(List.of("db"), result.affectedCis().stream().map(ConfigurationItem::getId).toList());
        assertEquals(1, result.paths().size());
    }

    @Test
    void traversalIsCycleSafe() {
        ImpactAnalysisService service = service(
                List.of(item("app", "APP"), item("db", "DB")),
                List.of(
                        relationship("r1", "app", "db"),
                        relationship("r2", "db", "app")
                )
        );

        ImpactAnalysisResult result = service.analyzeImpact("app", DEPENDENCIES, 5);

        assertEquals(List.of("db"), result.affectedCis().stream().map(ConfigurationItem::getId).toList());
        assertEquals(1, result.paths().size());
    }

    @Test
    void truncationLimitIsRespected() {
        List<ConfigurationItem> items = new ArrayList<>();
        List<Relationship> relationships = new ArrayList<>();
        items.add(item("app", "APP"));
        for (int index = 0; index < 10; index++) {
            String id = "ci-" + index;
            items.add(item(id, "CI " + index));
            relationships.add(relationship("r" + index, "app", id));
        }
        ImpactAnalysisService service = service(items, relationships, 3, 500);

        ImpactAnalysisResult result = service.analyzeImpact("app", DEPENDENCIES, 1);

        assertEquals(3, result.affectedCis().size());
        assertTrue(result.truncated());
    }

    @Test
    void missingCiHandledSafely() {
        ImpactAnalysisService service = service(List.of(item("app", "APP")), List.of());

        ImpactAnalysisResult result = service.analyzeImpact("missing", DEPENDENCIES, 3);

        assertTrue(result.affectedCis().isEmpty());
        assertTrue(result.message().contains("not available"));
    }

    private ImpactAnalysisService service(List<ConfigurationItem> items, List<Relationship> relationships) {
        return service(items, relationships, ImpactAnalysisService.DEFAULT_MAX_NODES, ImpactAnalysisService.DEFAULT_MAX_PATHS);
    }

    private ImpactAnalysisService service(
            List<ConfigurationItem> items,
            List<Relationship> relationships,
            int maxNodes,
            int maxPaths
    ) {
        GraphBuildResult result = graphBuilder.build(workbook(items, relationships));
        return new ImpactAnalysisService(result.graph(), maxNodes, maxPaths);
    }

    private CmdbWorkbook workbook(List<ConfigurationItem> items, List<Relationship> relationships) {
        CmdbSheet sheet = new CmdbSheet();
        sheet.setName("Applications");
        sheet.setType(SheetType.CI_BLOCK);
        items.forEach(sheet::addConfigurationItem);
        relationships.forEach(sheet::addRelationship);
        return new CmdbWorkbook("sample.xlsx", Instant.now(), List.of(sheet), List.of());
    }

    private ConfigurationItem item(String id, String name) {
        return new ConfigurationItem(
                id,
                name,
                "Application",
                null,
                Map.of(),
                "sample.xlsx",
                "Applications",
                2,
                null
        );
    }

    private Relationship relationship(String id, String sourceCiId, String targetCiId) {
        return new Relationship(
                id,
                sourceCiId,
                targetCiId,
                targetCiId,
                "depends on",
                "Depends On",
                "sample.xlsx",
                "Applications",
                3,
                RelationshipStatus.RESOLVED
        );
    }
}
