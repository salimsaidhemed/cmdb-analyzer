package com.cmdbanalyzer.ui.detail;

import com.cmdbanalyzer.ui.detail.DetailViewModel.DetailField;
import com.cmdbanalyzer.ui.detail.DetailViewModel.DetailNotice;
import com.cmdbanalyzer.ui.detail.DetailViewModel.DetailSection;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Renders detail panel view models into JavaFX nodes.
 */
public class DetailPanelView {

    public Node create(DetailViewModel viewModel) {
        DetailViewModel safeModel = viewModel == null ? DetailViewModel.empty() : viewModel;
        VBox root = new VBox(12);
        root.getStyleClass().add("detail-inspector");
        root.getChildren().add(header(safeModel));
        safeModel.notices().stream()
                .map(this::notice)
                .forEach(root.getChildren()::add);
        safeModel.sections().stream()
                .map(this::section)
                .forEach(root.getChildren()::add);
        return root;
    }

    private Node header(DetailViewModel viewModel) {
        VBox header = new VBox(6);
        header.getStyleClass().add("detail-inspector-header");

        HBox titleRow = new HBox(8);
        Label title = new Label(value(viewModel.title()));
        title.getStyleClass().add("detail-inspector-title");
        title.setWrapText(true);

        Label badge = new Label(value(viewModel.badge()));
        badge.getStyleClass().addAll("detail-badge", toneStyle(viewModel.tone()));

        titleRow.getChildren().addAll(title, badge);
        HBox.setHgrow(title, Priority.ALWAYS);

        Label subtitle = new Label(value(viewModel.subtitle()));
        subtitle.getStyleClass().add("detail-inspector-subtitle");
        subtitle.setWrapText(true);

        header.getChildren().addAll(titleRow, subtitle);
        return header;
    }

    private Node notice(DetailNotice notice) {
        Label label = new Label(value(notice.message()));
        label.setWrapText(true);
        label.getStyleClass().addAll("detail-notice", toneStyle(notice.tone()));
        return label;
    }

    private Node section(DetailSection section) {
        VBox card = new VBox(8);
        card.getStyleClass().add("detail-card");

        Label title = new Label(value(section.title()));
        title.getStyleClass().add("field-label");
        card.getChildren().add(title);

        section.fields().stream()
                .map(this::field)
                .forEach(card.getChildren()::add);
        return card;
    }

    private Node field(DetailField field) {
        VBox row = new VBox(2);
        row.getStyleClass().add("detail-field-row");

        Label label = new Label(value(field.label()));
        label.getStyleClass().add("detail-field-label");

        Label value = new Label(value(field.value()));
        value.setWrapText(true);
        value.getStyleClass().add("field-value");

        row.getChildren().addAll(label, value);
        return row;
    }

    private String toneStyle(DetailViewModel.DetailTone tone) {
        if (tone == null) {
            return "tone-neutral";
        }
        return switch (tone) {
            case SUCCESS -> "tone-success";
            case WARNING -> "tone-warning";
            case ERROR -> "tone-error";
            case NEUTRAL -> "tone-neutral";
        };
    }

    private String value(String text) {
        return text == null || text.isBlank() ? "-" : text;
    }
}
