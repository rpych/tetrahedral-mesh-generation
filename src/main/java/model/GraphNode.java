package model;

import common.ElementAttributes;

import java.util.ArrayList;
import java.util.Collection;

import org.graphstream.graph.implementations.AbstractGraph;
import org.graphstream.graph.implementations.SingleNode;
import org.javatuples.Pair;

public abstract class GraphNode extends SingleNode {

    private final String symbol;

    private Coordinates coordinates;

    protected GraphNode(AbstractGraph graph, String id, String symbol, double xCoordinate, double yCoordinate, double zCoordinate) {
        this(graph, id, symbol, new Coordinates(xCoordinate, yCoordinate, zCoordinate));
    }

    protected GraphNode(AbstractGraph graph, String id, String symbol, Coordinates coordinates) {
        super(graph, id);
        super.setAttribute(ElementAttributes.FROZEN_LAYOUT);
        this.symbol = symbol;
        this.coordinates = coordinates;
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
}
