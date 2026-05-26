package com.cmdbanalyzer.ui.navigation;

import com.cmdbanalyzer.analyzer.validation.ValidationSeverity;
import com.cmdbanalyzer.model.ConfigurationItem;
import com.cmdbanalyzer.model.RelationshipStatus;
import com.cmdbanalyzer.model.SheetType;

import java.util.List;

/**
 * UI-neutral navigation tree node for browsing imported CMDB workbook structure.
 */
public record CmdbNavigationNode(
        NavigationNodeType type,
        String label,
        int count,
        String sourceSheet,
        SheetType sheetType,
        ConfigurationItem configurationItem,
        String ciClass,
        RelationshipStatus relationshipStatus,
        ValidationSeverity issueSeverity,
        List<CmdbNavigationNode> children
) {

    public CmdbNavigationNode {
        children = List.copyOf(children == null ? List.of() : children);
    }

    public String displayLabel() {
        return count >= 0 ? label + " (" + count + ")" : label;
    }

    @Override
    public String toString() {
        return displayLabel();
    }

    public enum NavigationNodeType {
        WORKBOOK,
        GROUP,
        SHEET,
        CI,
        CI_CLASS,
        RELATIONSHIP_STATUS,
        ISSUE_SEVERITY
    }
}
