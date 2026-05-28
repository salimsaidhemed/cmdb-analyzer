package com.cmdbanalyzer.analyzer.impact;

import com.cmdbanalyzer.model.ConfigurationItem;
import com.cmdbanalyzer.model.Relationship;

import java.util.List;

/**
 * One traversed impact path from the selected CI to an affected endpoint.
 */
public record ImpactPath(
        ImpactDirection direction,
        int depth,
        List<ConfigurationItem> configurationItems,
        List<Relationship> relationships
) {

    public ImpactPath {
        configurationItems = List.copyOf(configurationItems == null ? List.of() : configurationItems);
        relationships = List.copyOf(relationships == null ? List.of() : relationships);
    }

    public ConfigurationItem endpoint() {
        return configurationItems.isEmpty() ? null : configurationItems.get(configurationItems.size() - 1);
    }
}
