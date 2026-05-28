package com.cmdbanalyzer.ui.graph;

import com.cmdbanalyzer.graph.CmdbGraph;
import com.cmdbanalyzer.ui.graph.GraphLayoutService.GraphLayout;
import com.cmdbanalyzer.ui.graph.GraphLayoutService.GraphNodeLayout;
import com.cmdbanalyzer.ui.graph.GraphNeighborhoodViewModel.GraphEdge;
import com.cmdbanalyzer.ui.graph.GraphNeighborhoodViewModel.GraphNode;
import com.cmdbanalyzer.ui.graph.GraphNeighborhoodViewModel.NodeRole;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * JavaFX graph canvas for CI-centered direct relationships.
 */
public class GraphTabView {

    private final CmdbGraph graph;
    private final GraphNeighborhoodService neighborhoodService;
    private final GraphLayoutService layoutService = new GraphLayoutService();
    private final Consumer<String> ciSelectionHandler;
    private final Consumer<String> relationshipSelectionHandler;
    private final BorderPane root = new BorderPane();
    private final ScrollPane scrollPane = new ScrollPane();
    private final Pane canvas = new Pane();
    private final Label selectedLabel = new Label("No CI selected");
    private final Label countLabel = new Label("0 nodes, 0 edges");
    private final Label noticeLabel = new Label("Select a CI to view its relationships.");
    private final CheckBox showLabelsCheckBox = new CheckBox("Show labels");
    private final Button fitButton = new Button("Fit to View");
    private final Button resetButton = new Button("Reset Selection");
    private final Map<String, VBox> nodeViews = new HashMap<>();
    private final Map<String, Group> edgeViews = new HashMap<>();
    private GraphNeighborhoodViewModel currentViewModel = GraphNeighborhoodViewModel.empty();
    private GraphLayout currentLayout = layoutService.layout(currentViewModel);
    private String selectedNodeId;
    private String selectedRelationshipId;

    public GraphTabView(
            CmdbGraph graph,
            GraphNeighborhoodService neighborhoodService,
            Consumer<String> ciSelectionHandler,
            Consumer<String> relationshipSelectionHandler
    ) {
        this.graph = graph;
        this.neighborhoodService = neighborhoodService;
        this.ciSelectionHandler = ciSelectionHandler;
        this.relationshipSelectionHandler = relationshipSelectionHandler;
        configure();
        showSelection(null);
    }

    public Node node() {
        return root;
    }

    public void showSelection(String ciId) {
        selectedNodeId = ciId;
        selectedRelationshipId = null;
        currentViewModel = neighborhoodService.build(graph, ciId);
        currentLayout = layoutService.layout(currentViewModel);
        render();
        fitToView();
    }

    private void configure() {
        root.getStyleClass().add("graph-view");
        root.setTop(header());
        root.setCenter(scrollPane);
        scrollPane.getStyleClass().add("graph-scroll-pane");
        scrollPane.setContent(canvas);
        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);
        canvas.getStyleClass().add("graph-canvas");
        showLabelsCheckBox.setSelected(false);
        showLabelsCheckBox.getStyleClass().add("graph-control");
        showLabelsCheckBox.setOnAction(event -> updateEdgeLabelVisibility());
        fitButton.getStyleClass().add("filter-clear-button");
        fitButton.setGraphic(new FontIcon("fas-expand-arrows-alt"));
        fitButton.setOnAction(event -> fitToView());
        resetButton.getStyleClass().add("filter-clear-button");
        resetButton.setGraphic(new FontIcon("fas-undo"));
        resetButton.setOnAction(event -> resetSelection());
    }

    private Node header() {
        VBox header = new VBox(10);
        header.getStyleClass().add("graph-header");

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        FontIcon icon = new FontIcon("fas-project-diagram");
        icon.getStyleClass().add("graph-header-icon");
        Label title = new Label("Relationship Graph");
        title.getStyleClass().add("graph-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        countLabel.getStyleClass().add("graph-count-label");
        titleRow.getChildren().addAll(icon, title, spacer, fitButton, resetButton, showLabelsCheckBox, countLabel);

        HBox summaryRow = new HBox(12);
        summaryRow.setAlignment(Pos.CENTER_LEFT);
        selectedLabel.getStyleClass().add("graph-selected-label");
        noticeLabel.getStyleClass().add("graph-notice-label");
        summaryRow.getChildren().addAll(selectedLabel, noticeLabel);

        HBox legend = new HBox(14);
        legend.setAlignment(Pos.CENTER_LEFT);
        Label direction = new Label("A -> B means A depends on B");
        direction.getStyleClass().add("graph-direction-note");
        legend.getChildren().addAll(
                sideLabel("Dependencies", "fas-arrow-left"),
                legendItem("Selected CI", "selected"),
                legendItem("Dependency", "dependency"),
                legendItem("Dependent", "dependent"),
                sideLabel("Dependents", "fas-arrow-right"),
                direction
        );

        header.getChildren().addAll(titleRow, summaryRow, legend);
        return header;
    }

    private Node sideLabel(String text, String iconCode) {
        HBox item = new HBox(6);
        item.setAlignment(Pos.CENTER_LEFT);
        FontIcon icon = new FontIcon(iconCode);
        icon.getStyleClass().add("graph-side-icon");
        Label label = new Label(text);
        label.getStyleClass().add("graph-legend-label");
        item.getChildren().addAll(icon, label);
        return item;
    }

    private Node legendItem(String text, String style) {
        HBox item = new HBox(6);
        item.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(5);
        dot.getStyleClass().addAll("graph-legend-dot", "graph-node-" + style);
        Label label = new Label(text);
        label.getStyleClass().add("graph-legend-label");
        item.getChildren().addAll(dot, label);
        return item;
    }

    private void render() {
        canvas.getChildren().clear();
        nodeViews.clear();
        edgeViews.clear();
        selectedLabel.setText(currentViewModel.selectedCiName() == null
                ? "No CI selected"
                : "Selected CI: " + currentViewModel.selectedCiName());
        countLabel.setText(currentViewModel.nodes().size() + " nodes, " + currentViewModel.edges().size() + " edges");
        noticeLabel.setText(noticeText());
        noticeLabel.setVisible(!noticeLabel.getText().isBlank());
        noticeLabel.setManaged(noticeLabel.isVisible());
        canvas.setPrefSize(currentLayout.width(), currentLayout.height());
        canvas.setMinSize(currentLayout.width(), currentLayout.height());

        if (currentViewModel.nodes().isEmpty()) {
            canvas.getChildren().add(emptyState());
            return;
        }

        canvas.getChildren().add(sideTitle("Dependencies", 82, 36));
        canvas.getChildren().add(sideTitle("Selected CI", currentLayout.width() / 2 - 54, 36));
        canvas.getChildren().add(sideTitle("Dependents", currentLayout.width() - 190, 36));

        Group edgeLayer = new Group();
        Group nodeLayer = new Group();
        currentViewModel.edges().forEach(edge -> edgeLayer.getChildren().add(edgeNode(edge)));
        currentViewModel.nodes().forEach(node -> nodeLayer.getChildren().add(graphNode(node)));
        canvas.getChildren().addAll(edgeLayer, nodeLayer);
        updateSelectionStyles();
        updateEdgeLabelVisibility();
    }

    private String noticeText() {
        if (currentViewModel.nodes().isEmpty()) {
            return currentViewModel.message() == null ? "Select a CI to view its relationships." : currentViewModel.message();
        }
        if (currentViewModel.edges().isEmpty()) {
            return "Selected CI has no direct relationships.";
        }
        return currentViewModel.message() == null ? "" : currentViewModel.message();
    }

    private Node sideTitle(String text, double x, double y) {
        Label label = new Label(text);
        label.getStyleClass().add("graph-side-title");
        label.setLayoutX(x);
        label.setLayoutY(y);
        return label;
    }

    private Node emptyState() {
        VBox empty = new VBox(10);
        empty.getStyleClass().add("graph-empty-state");
        empty.setAlignment(Pos.CENTER);
        empty.setPrefSize(currentLayout.width(), currentLayout.height());
        FontIcon icon = new FontIcon("fas-mouse-pointer");
        icon.getStyleClass().add("graph-empty-icon");
        Label title = new Label("Select a CI to view its relationships.");
        title.getStyleClass().add("empty-title");
        Label description = new Label("Use the Configuration Items table or the navigation tree to choose a CI.");
        description.getStyleClass().add("empty-description");
        description.setWrapText(true);
        empty.getChildren().addAll(icon, title, description);
        return empty;
    }

    private Node graphNode(GraphNode node) {
        GraphNodeLayout layout = currentLayout.nodes().get(node.ciId());
        VBox labelBox = new VBox(3);
        labelBox.setAlignment(Pos.CENTER);
        labelBox.getStyleClass().addAll("graph-node", roleClass(node.role()));
        labelBox.setLayoutX(layout.x());
        labelBox.setLayoutY(layout.y());
        labelBox.setPrefSize(layout.width(), layout.height());
        labelBox.setMinSize(layout.width(), layout.height());
        labelBox.setMaxSize(layout.width(), layout.height());

        Label name = new Label(displayName(node.name()));
        name.getStyleClass().add("graph-node-title");
        name.setWrapText(true);
        name.setMaxWidth(layout.width() - 22);
        Label ciClass = new Label(safe(node.ciClass()));
        ciClass.getStyleClass().add("graph-node-subtitle");
        ciClass.setMaxWidth(layout.width() - 22);
        labelBox.getChildren().addAll(name, ciClass);
        labelBox.setOnMouseClicked(event -> {
            selectedNodeId = node.ciId();
            selectedRelationshipId = null;
            updateSelectionStyles();
            ciSelectionHandler.accept(node.ciId());
        });
        labelBox.setOnMouseEntered(event -> highlightConnectedEdges(node.ciId(), true));
        labelBox.setOnMouseExited(event -> highlightConnectedEdges(node.ciId(), false));
        Tooltip.install(labelBox, new Tooltip(nodeTooltip(node)));
        nodeViews.put(node.ciId(), labelBox);
        return labelBox;
    }

    private Node edgeNode(GraphEdge edge) {
        GraphNodeLayout sourceLayout = currentLayout.nodes().get(edge.sourceCiId());
        GraphNodeLayout targetLayout = currentLayout.nodes().get(edge.targetCiId());
        if (sourceLayout == null || targetLayout == null) {
            return new Group();
        }
        Point2D source = new Point2D(sourceLayout.centerX(), sourceLayout.centerY());
        Point2D target = new Point2D(targetLayout.centerX(), targetLayout.centerY());
        Point2D start = trim(source, target);
        Point2D end = trim(target, source);
        Group group = new Group();
        group.getStyleClass().add("graph-edge-group");

        Line line = new Line(start.getX(), start.getY(), end.getX(), end.getY());
        line.getStyleClass().add("graph-edge");
        Line hitLine = new Line(start.getX(), start.getY(), end.getX(), end.getY());
        hitLine.getStyleClass().add("graph-edge-hit-area");
        Polygon arrow = arrowHead(start, end);
        arrow.getStyleClass().add("graph-edge-arrow");
        Label label = new Label(safe(edge.relationshipType()));
        label.getStyleClass().add("graph-edge-label");
        label.setLayoutX((start.getX() + end.getX()) / 2 - 52);
        label.setLayoutY((start.getY() + end.getY()) / 2 - 15);
        group.getChildren().addAll(hitLine, line, arrow, label);
        group.setOnMouseClicked(event -> {
            selectedRelationshipId = edge.relationshipId();
            selectedNodeId = null;
            updateSelectionStyles();
            relationshipSelectionHandler.accept(edge.relationshipId());
            event.consume();
        });
        group.setOnMouseEntered(event -> setEdgeState(edge.relationshipId(), "graph-edge-hover", true));
        group.setOnMouseExited(event -> setEdgeState(edge.relationshipId(), "graph-edge-hover", false));
        Tooltip.install(group, new Tooltip(edgeTooltip(edge)));
        edgeViews.put(edge.relationshipId(), group);
        return group;
    }

    private void fitToView() {
        if (currentLayout.width() <= 0 || currentLayout.height() <= 0) {
            return;
        }
        scrollPane.setHvalue(0.5);
        scrollPane.setVvalue(0.5);
    }

    private void resetSelection() {
        selectedNodeId = currentViewModel.selectedCiId();
        selectedRelationshipId = null;
        updateSelectionStyles();
    }

    private void updateSelectionStyles() {
        nodeViews.forEach((ciId, node) -> setStyle(node, "graph-node-active", ciId.equals(selectedNodeId)));
        edgeViews.forEach((relationshipId, edge) -> setEdgeState(
                relationshipId,
                "graph-edge-selected",
                relationshipId.equals(selectedRelationshipId)
        ));
    }

    private void updateEdgeLabelVisibility() {
        boolean show = showLabelsCheckBox.isSelected();
        edgeViews.values().forEach(edge -> edge.getChildren().stream()
                .filter(Label.class::isInstance)
                .forEach(label -> {
                    label.setVisible(show);
                    label.setManaged(show);
                }));
    }

    private void highlightConnectedEdges(String ciId, boolean highlight) {
        currentViewModel.edges().stream()
                .filter(edge -> ciId.equals(edge.sourceCiId()) || ciId.equals(edge.targetCiId()))
                .forEach(edge -> setEdgeState(edge.relationshipId(), "graph-edge-connected", highlight));
    }

    private void setEdgeState(String relationshipId, String styleClass, boolean enabled) {
        Group edge = edgeViews.get(relationshipId);
        if (edge == null) {
            return;
        }
        setStyle(edge, styleClass, enabled);
    }

    private void setStyle(Node node, String styleClass, boolean enabled) {
        if (enabled && !node.getStyleClass().contains(styleClass)) {
            node.getStyleClass().add(styleClass);
        } else if (!enabled) {
            node.getStyleClass().remove(styleClass);
        }
    }

    private Point2D trim(Point2D from, Point2D to) {
        Point2D vector = to.subtract(from);
        if (vector.magnitude() == 0) {
            return from;
        }
        double distance = GraphLayoutService.NODE_WIDTH / 2 + 8;
        return from.add(vector.normalize().multiply(distance));
    }

    private Polygon arrowHead(Point2D start, Point2D end) {
        Point2D direction = end.subtract(start).normalize();
        Point2D normal = new Point2D(-direction.getY(), direction.getX());
        Point2D base = end.subtract(direction.multiply(14));
        return new Polygon(
                end.getX(), end.getY(),
                base.add(normal.multiply(6)).getX(), base.add(normal.multiply(6)).getY(),
                base.subtract(normal.multiply(6)).getX(), base.subtract(normal.multiply(6)).getY()
        );
    }

    private String nodeTooltip(GraphNode node) {
        return "Name: " + safe(node.name())
                + "\nClass: " + safe(node.ciClass())
                + "\nSheet: " + safe(node.sourceSheet())
                + "\nRow: " + (node.sourceRow() <= 0 ? "-" : node.sourceRow());
    }

    private String edgeTooltip(GraphEdge edge) {
        return "Relationship: " + safe(edge.relationshipType())
                + "\nDirection: " + safe(edge.sourceCiId()) + " -> " + safe(edge.targetCiId());
    }

    private String displayName(String value) {
        String safeValue = safe(value);
        return safeValue.length() > 34 ? safeValue.substring(0, 31) + "..." : safeValue;
    }

    private String roleClass(NodeRole role) {
        return switch (role) {
            case SELECTED -> "graph-node-selected";
            case DEPENDENCY -> "graph-node-dependency";
            case DEPENDENT -> "graph-node-dependent";
        };
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
