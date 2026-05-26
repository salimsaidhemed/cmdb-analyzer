package com.cmdbanalyzer.analyzer.validation;

import com.cmdbanalyzer.graph.CmdbGraph;
import com.cmdbanalyzer.graph.GraphBuildResult;
import com.cmdbanalyzer.model.CmdbWorkbook;

import java.util.Objects;
import java.util.Optional;

/**
 * Validation input context containing the workbook and optional graph analysis artifacts.
 */
public record ValidationContext(
        CmdbWorkbook workbook,
        CmdbGraph graph,
        GraphBuildResult graphBuildResult
) {

    public ValidationContext {
        Objects.requireNonNull(workbook, "workbook must not be null");
    }

    public static ValidationContext forWorkbook(CmdbWorkbook workbook) {
        return new ValidationContext(workbook, null, null);
    }

    public Optional<CmdbGraph> graphOptional() {
        return Optional.ofNullable(graph);
    }

    public Optional<GraphBuildResult> graphBuildResultOptional() {
        return Optional.ofNullable(graphBuildResult);
    }
}
