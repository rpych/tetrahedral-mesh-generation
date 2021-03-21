package model;

import org.javatuples.Pair;

public class GraphEdge { //extends AbstractEdge

    private final Pair<GraphNode, GraphNode> edgeNodes;

    private boolean border;

    private String id;

    public GraphEdge(String id, Pair<GraphNode, GraphNode> edgeNodes, boolean border) {
        this.id = id;
        this.edgeNodes = edgeNodes;
        this.border = border;
    }

    public Pair<GraphNode, GraphNode> getEdgeNodes() {
        return edgeNodes;
    }

    public boolean getBorder() {
        return border;
    }

    public double getLength() {
        return Coordinates.distance(edgeNodes.getValue0().getCoordinates(), edgeNodes.getValue1().getCoordinates());
    }
    
    public Coordinates getMiddlePointCoordinates() {
		GraphNode n0 = edgeNodes.getValue0();
		GraphNode n1 = edgeNodes.getValue1();
		return Coordinates.middlePoint(n0.getCoordinates(), n1.getCoordinates());
    }

    public boolean isBetweenVertices(){
    	return (edgeNodes.getValue0() instanceof Vertex) &&
    			(edgeNodes.getValue1() instanceof Vertex);
    }
    
    public Pair<Vertex, Vertex> getVertices(){
    	if(this.isBetweenVertices()) {
    		return new Pair<Vertex, Vertex>((Vertex)edgeNodes.getValue0(), (Vertex)edgeNodes.getValue1());
    	}
    	throw new IllegalArgumentException("Some GraphNode of edge is not a Vertex!");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GraphNode getNode0(){
        return edgeNodes.getValue0();
    }

    public GraphNode getNode1(){
        return edgeNodes.getValue1();
    }
    
    public static class GraphEdgeBuilder {

        private final Pair<GraphNode, GraphNode> edgeNodes;

        private final String id;

        private boolean border;

        public GraphEdgeBuilder(String id, GraphNode source, GraphNode target, boolean border) {
            this.id = id;
            this.edgeNodes = new Pair<>(source, target);
            this.border = border;
        }

        public GraphEdgeBuilder(String id, GraphNode source, GraphNode target) {
            this.id = id;
            this.edgeNodes = new Pair<>(source, target);
        }

        public GraphEdgeBuilder setBorder(boolean border) {
            this.border = border;
            return this;
        }

        public GraphEdgeBuilder withBorder(boolean border) {
            this.border = border;
            return this;
        }

        public GraphEdge build() {
            return new GraphEdge(id, edgeNodes, border);
        }
    }
}
