package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.javatuples.Pair;

public abstract class GraphNode {
    private final String symbol;

    private Coordinates coordinates;

    protected String id;

    protected ConcurrentLinkedDeque<GraphEdge> neighborEdgeList;

    protected GraphNode(Graph graph, String id, String symbol, double xCoordinate, double yCoordinate, double zCoordinate) {
        this.id = id;
        this.symbol = symbol;
        this.coordinates = new Coordinates(xCoordinate, yCoordinate, zCoordinate);
        this.neighborEdgeList = new ConcurrentLinkedDeque<>();
    }

    protected GraphNode(Graph graph, String id, String symbol, Coordinates coordinates) {
        this.id = id;
        this.symbol = symbol;
        this.coordinates = coordinates;
        this.neighborEdgeList = new ConcurrentLinkedDeque<>();
    }

    protected GraphNode(String id, String symbol, Coordinates coordinates) {
        this.id = id;
        this.symbol = symbol;
        this.coordinates = coordinates;
        this.neighborEdgeList = new ConcurrentLinkedDeque<>();
    }

    public void rotate() {
        coordinates = coordinates.getRotation();
    }

    public double getXCoordinate() {
        return coordinates.getX();
    }

    public double getYCoordinate() {
        return coordinates.getY();
    }

    public double getZCoordinate() {
        return coordinates.getZ();
    }

    public Coordinates getCoordinates(){
        return  coordinates;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public synchronized void addNeighbourEdge(GraphEdge edge){
        synchronized(neighborEdgeList) {
            if (!neighborEdgeList.contains(edge)) {
                this.neighborEdgeList.add(edge);
            }
        }
    }

    public synchronized void removeNeighbourEdge(GraphEdge edge){
        synchronized(neighborEdgeList) {
            if (neighborEdgeList.contains(edge)) {
                this.neighborEdgeList.remove(edge);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphNode graphNode = (GraphNode) o;
        return symbol.equals(graphNode.symbol) &&
                id.equals(graphNode.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Collection<GraphNode> getAdjacentNodes(ModelGraph graph){
		Collection<GraphNode> nodes = new ArrayList<GraphNode>();
		Collection<GraphEdge> edges = graph.getEdges(this);
		for(GraphEdge edge : edges) {
			Pair<GraphNode, GraphNode> edgeNodes = edge.getEdgeNodes();
			if(this != edgeNodes.getValue0()) {
				nodes.add(edgeNodes.getValue0());
			} else {
				nodes.add(edgeNodes.getValue1());
			}
		}
		return nodes;
	}

    public GraphEdge getEdgeBetween(GraphNode node) {
        synchronized(neighborEdgeList) {
            for (GraphEdge e : this.neighborEdgeList) {
                if ((e.getEdgeNodes().getValue0().getId().equals(node.getId()) && this.id.equals(e.getEdgeNodes().getValue1().getId())) ||
                        (e.getEdgeNodes().getValue1().getId().equals(node.getId()) && this.id.equals(e.getEdgeNodes().getValue0().getId()))) {
                    return e;
                }
            }
        }
        return null;
    }

    public boolean hasEdgeBetween(GraphNode node){
        return getEdgeBetween(node) != null;
    }
}
