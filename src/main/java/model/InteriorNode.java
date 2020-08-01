package model;

import org.graphstream.graph.implementations.AbstractGraph;
import org.javatuples.Quartet;

public class InteriorNode extends GraphNode{

    public static final String INTERIOR_SYMBOL = "I";
    private Quartet<Vertex, Vertex, Vertex, Vertex> quartet;

    public InteriorNode(AbstractGraph graph, String id, Coordinates coordinates) {
        super(graph, id, INTERIOR_SYMBOL, coordinates);
    }

    public InteriorNode(AbstractGraph graph, String id, Vertex v1, Vertex v2, Vertex v3, Vertex v4) {
        super(graph, id, INTERIOR_SYMBOL, getInteriorNodePosition(v1, v2, v3, v4));
        quartet = new Quartet<>(v1, v2, v3, v4);
    }

    private static Coordinates getInteriorNodePosition(Vertex v1, Vertex v2, Vertex v3, Vertex v4) {
        return new Coordinates(getInteriorNodeXCoordinate(v1, v2, v3, v4), getInteriorNodeYCoordinate(v1, v2, v3, v4), getInteriorNodeZCoordinate(v1, v2, v3, v4));
    }

    private static double getInteriorNodeXCoordinate(Vertex v1, Vertex v2, Vertex v3, Vertex v4) {
        return (v1.getXCoordinate() + v2.getXCoordinate() + v3.getXCoordinate() + v4.getXCoordinate()) / 4d;
    }

    private static double getInteriorNodeYCoordinate(Vertex v1, Vertex v2, Vertex v3, Vertex v4) {
        return (v1.getYCoordinate() + v2.getYCoordinate() + v3.getYCoordinate() + v4.getYCoordinate()) / 4d;
    }

    private static double getInteriorNodeZCoordinate(Vertex v1, Vertex v2, Vertex v3, Vertex v4) {
        return (v1.getZCoordinate() + v2.getZCoordinate() + v3.getZCoordinate() + v4.getZCoordinate()) / 4d;
    }
}
