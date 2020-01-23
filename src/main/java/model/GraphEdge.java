package model;

import org.graphstream.graph.implementations.AbstractEdge;
import org.javatuples.Pair;

public class GraphEdge extends AbstractEdge {

    private static final String EDGE_SYMBOL = "E";

    private final String symbol;

    private final Pair<GraphNode, GraphNode> edgeNodes;

    private boolean border;

    public GraphEdge(String id, String symbol, Pair<GraphNode, GraphNode> edgeNodes, boolean border) {
        super(id, edgeNodes.getValue0(), edgeNodes.getValue1(), false);
        this.edgeNodes = edgeNodes;
        this.symbol = symbol;
        this.border = border;
    }

    public GraphEdge(String id, Pair<GraphNode, GraphNode> edgeNodes, boolean border) {
        this(id, EDGE_SYMBOL, edgeNodes, border);
    }

    public String getSymbol() {
        return symbol;
    }

    public Pair<GraphNode, GraphNode> getEdgeNodes() {
        return edgeNodes;
    }

    public boolean getBorder() {
        return border;
    }

    public void setBorder(boolean border) {
        this.border = border;
    }

    public double getLength() {
        return Point3d.distance(edgeNodes.getValue0().getCoordinates(), edgeNodes.getValue1().getCoordinates());
    }

    public static class GraphEdgeBuilder {

        private final String symbol;

        private final Pair<GraphNode, GraphNode> edgeNodes;

        private final String id;

        private boolean border;

        public GraphEdgeBuilder(String id, GraphNode source, GraphNode target, boolean border) {
            this.symbol = EDGE_SYMBOL;
            this.id = id;
            this.edgeNodes = new Pair<>(source, target);
            this.border = border;
        }

        public GraphEdgeBuilder(String id, GraphNode source, GraphNode target) {
            this.symbol = EDGE_SYMBOL;
            this.id = id;
            this.edgeNodes = new Pair<>(source, target);
        }

        public GraphEdgeBuilder setBorder(boolean border) {
            this.border = border;
            return this;
        }

        public GraphEdge build() {
            return new GraphEdge(id, symbol, edgeNodes, border);
        }
    }
}
