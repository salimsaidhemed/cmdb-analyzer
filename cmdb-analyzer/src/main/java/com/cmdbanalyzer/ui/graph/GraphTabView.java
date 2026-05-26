package com.cmdbanalyzer.ui.graph;

import com.cmdbanalyzer.graph.CmdbGraph;
import com.cmdbanalyzer.ui.graph.GraphNeighborhoodViewModel.GraphEdge;
import com.cmdbanalyzer.ui.graph.GraphNeighborhoodViewModel.GraphNode;
import com.cmdbanalyzer.ui.graph.GraphNeighborhoodViewModel.NodeRole;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
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
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Basic JavaFX graph canvas for CI-centered direct relationships.
 */
public class GraphTabView {

    private static final double CANVAS_WIDTH = 900;
    private static final double CANVAS_HEIGHT = 560;

    private final CmdbGraph graph;
    private final GraphNeighborhoodService neighborhoodService;
    private final Consumer<String> ciSelectionHandler;
    private final Consumer<String> relationshipSelectionHandler;
    private final BorderPane root = new BorderPane();
    private final Pane canvas = new Pane();
    private final Label selectedLabel = new Label("No CI selected");
    private final Label countLabel = new Label("0 nodes, 0 edges");
    private final Label noticeLabel = new Label("Select a CI to view its relationships.");

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
        GraphNeighborhoodViewModel viewModel = neighborhoodService.build(graph, ciId);
        render(viewModel);
    }

    private void configure() {
        root.getStyleClass().add("graph-view");
        root.setTop(header());
        root.setCenter(canvas);
        canvas.getStyleClass().add("graph-canvas");
        canvas.setMinSize(640, 420);
        canvas.setPrefSize(CANVAS_WIDTH, CANVAS_HEIGHT);
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
        titleRow.getChildren().addAll(icon, title, spacer, countLabel);

        HBox summaryRow = new HBox(12);
        summaryRow.setAlignment(Pos.CENTER_LEFT);
        selectedLabel.getStyleClass().add("graph-selected-label");
        noticeLabel.getStyleClass().add("graph-notice-label");
        summaryRow.getChildren().addAll(selectedLabel, noticeLabel);

        HBox legend = new HBox(12);
        legend.setAlignment(Pos.CENTER_LEFT);
        legend.getChildren().addAll(
                legendItem("Selected CI", "selected"),
                legendItem("Dependency", "dependency"),
                legendItem("Dependent", "dependent")
        );

        header.getChildren().addAll(titleRow, summaryRow, legend);
        return header;
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

    private void render(GraphNeighborhoodViewModel viewModel) {
        canvas.getChildren().clear();
        selectedLabel.setText(viewModel.selectedCiName() == null
                ? "No CI selected"
                : "Selected CI: " + viewModel.selectedCiName());
        countLabel.setText(viewModel.nodes().size() + " nodes, " + viewModel.edges().size() + " edges");
        noticeLabel.setText(viewModel.message() == null ? "" : viewModel.message());
        noticeLabel.setVisible(viewModel.message() != null && !viewModel.message().isBlank());
        noticeLabel.setManaged(noticeLabel.isVisible());

        if (viewModel.nodes().isEmpty()) {
            canvas.getChildren().add(emptyState());
            return;
        }

        Map<String, Point2D> positions = positions(viewModel.nodes());
        Group edgeLayer = new Group();
        Group nodeLayer = new Group();
        viewModel.edges().forEach(edge -> edgeLayer.getChildren().add(edgeNode(edge, positions)));
        viewModel.nodes().forEach(node -> nodeLayer.getChildren().add(graphNode(node, positions.get(node.ciId()))));
        canvas.getChildren().addAll(edgeLayer, nodeLayer);
    }

    private Node emptyState() {
        VBox empty = new VBox(10);
        empty.getStyleClass().add("graph-empty-state");
        empty.setAlignment(Pos.CENTER);
        empty.setPrefSize(CANVAS_WIDTH, CANVAS_HEIGHT);
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

    private Node graphNode(GraphNode node, Point2D point) {
        VBox labelBox = new VBox(2);
        labelBox.setAlignment(Pos.CENTER);
        labelBox.getStyleClass().addAll("graph-node", roleClass(node.role()));
        labelBox.setLayoutX(point.getX() - 58);
        labelBox.setLayoutY(point.getY() - 30);
        labelBox.setPrefSize(116, 60);
        Label name = new Label(safe(node.name()));
        name.getStyleClass().add("graph-node-title");
        name.setWrapText(true);
        Label ciClass = new Label(safe(node.ciClass()));
        ciClass.getStyleClass().add("graph-node-subtitle");
        labelBox.getChildren().addAll(name, ciClass);
        labelBox.setOnMouseClicked(event -> ciSelectionHandler.accept(node.ciId()));
        return labelBox;
    }

    private Node edgeNode(GraphEdge edge, Map<String, Point2D> positions) {
        Point2D source = positions.get(edge.sourceCiId());
        Point2D target = positions.get(edge.targetCiId());
        if (source == null || target == null) {
            return new Group();
        }
        Group group = new Group();
        Point2D start = trim(source, target, 68);
        Point2D end = trim(target, source, 68);
        Line line = new Line(start.getX(), start.getY(), end.getX(), end.getY());
        line.getStyleClass().add("graph-edge");
        Polygon arrow = arrowHead(start, end);
        arrow.getStyleClass().add("graph-edge-arrow");
        Label label = new Label(safe(edge.relationshipType()));
        label.getStyleClass().add("graph-edge-label");
        label.setLayoutX((start.getX() + end.getX()) / 2 - 34);
        label.setLayoutY((start.getY() + end.getY()) / 2 - 13);
        group.getChildren().addAll(line, arrow, label);
        group.setOnMouseClicked(event -> relationshipSelectionHandler.accept(edge.relationshipId()));
        return group;
    }

    private Map<String, Point2D> positions(List<GraphNode> nodes) {
        Map<String, Point2D> positions = new HashMap<>();
        GraphNode selected = nodes.stream()
                .filter(node -> node.role() == NodeRole.SELECTED)
                .findFirst()
                .orElse(nodes.get(0));
        Point2D center = new Point2D(CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2);
        positions.put(selected.ciId(), center);

        List<GraphNode> dependents = nodes.stream().filter(node -> node.role() == NodeRole.DEPENDENT).toList();
        List<GraphNode> dependencies = nodes.stream().filter(node -> node.role() == NodeRole.DEPENDENCY).toList();
        placeSide(positions, dependents, 170, 120, true);
        placeSide(positions, dependencies, CANVAS_WIDTH - 170, 120, false);
        return positions;
    }

    private void placeSide(Map<String, Point2D> positions, List<GraphNode> nodes, double x, double top, boolean left) {
        if (nodes.isEmpty()) {
            return;
        }
        double spacing = Math.min(84, (CANVAS_HEIGHT - 2 * top) / Math.max(1, nodes.size() - 1));
        double startY = nodes.size() == 1 ? CANVAS_HEIGHT / 2 : top;
        for (int index = 0; index < nodes.size(); index++) {
            double offset = nodes.size() == 1 ? 0 : index * spacing;
            double stagger = index % 2 == 0 ? 0 : (left ? -26 : 26);
            positions.put(nodes.get(index).ciId(), new Point2D(x + stagger, startY + offset));
        }
    }

    private Point2D trim(Point2D from, Point2D to, double distance) {
        Point2D vector = to.subtract(from);
        if (vector.magnitude() == 0) {
            return from;
        }
        return from.add(vector.normalize().multiply(distance));
    }

    private Polygon arrowHead(Point2D start, Point2D end) {
        Point2D direction = end.subtract(start).normalize();
        Point2D normal = new Point2D(-direction.getY(), direction.getX());
        Point2D base = end.subtract(direction.multiply(12));
        return new Polygon(
                end.getX(), end.getY(),
                base.add(normal.multiply(5)).getX(), base.add(normal.multiply(5)).getY(),
                base.subtract(normal.multiply(5)).getX(), base.subtract(normal.multiply(5)).getY()
        );
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
