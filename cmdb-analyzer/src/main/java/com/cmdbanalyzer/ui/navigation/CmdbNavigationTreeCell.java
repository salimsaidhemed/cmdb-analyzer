package com.cmdbanalyzer.ui.navigation;

import javafx.scene.control.TreeCell;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * JavaFX cell renderer for the CMDB navigation tree.
 */
public class CmdbNavigationTreeCell extends TreeCell<CmdbNavigationNode> {

    @Override
    protected void updateItem(CmdbNavigationNode item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        setText(item.displayLabel());
        FontIcon icon = new FontIcon(iconLiteral(item));
        icon.getStyleClass().add("navigation-tree-icon");
        setGraphic(icon);
    }

    private String iconLiteral(CmdbNavigationNode node) {
        return switch (node.type()) {
            case WORKBOOK -> "fas-book";
            case GROUP -> "fas-folder";
            case SHEET -> "fas-file-alt";
            case CI -> "fas-cube";
            case CI_CLASS -> "fas-tags";
            case RELATIONSHIP_STATUS -> "fas-link";
            case ISSUE_SEVERITY -> issueSeverityIcon(node);
        };
    }

    private String issueSeverityIcon(CmdbNavigationNode node) {
        if (node.issueSeverity() == null) {
            return "fas-info-circle";
        }
        return switch (node.issueSeverity()) {
            case ERROR -> "fas-exclamation-circle";
            case WARNING -> "fas-exclamation-triangle";
            case INFO -> "fas-info-circle";
        };
    }
}
