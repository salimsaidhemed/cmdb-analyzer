package com.cmdbanalyzer.parser;

import com.cmdbanalyzer.model.ConfigurationItem;
import com.cmdbanalyzer.model.ParserWarning;
import com.cmdbanalyzer.model.Relationship;

import java.util.Collection;
import java.util.List;

/**
 * Resolves relationship target names to parsed Configuration Items.
 *
 * <p>Implementations should support unresolved targets, duplicate CI names, malformed
 * relationship rows, raw relationship-type preservation, and graph-friendly relationship
 * statuses.</p>
 */
public interface RelationshipResolver {

    /**
     * Resolves raw relationships against known configuration items.
     *
     * @param items parsed configuration items from the import scope
     * @param relationships relationships with raw target names
     * @return resolution result containing updated relationships and warnings
     */
    RelationshipResolutionResult resolve(
            Collection<ConfigurationItem> items,
            Collection<Relationship> relationships
    );

    /**
     * Result of relationship resolution.
     *
     * @param relationships resolved, unresolved, or malformed relationships
     * @param warnings diagnostics for unresolved and ambiguous relationships
     */
    record RelationshipResolutionResult(List<Relationship> relationships, List<ParserWarning> warnings) {
        public RelationshipResolutionResult {
            relationships = List.copyOf(relationships);
            warnings = List.copyOf(warnings);
        }
    }
}
