package model;

import org.graphstream.graph.implementations.AbstractGraph;
import org.javatuples.Triplet;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InteriorNode extends GraphNode {

    private static final String INTERIOR_SYMBOL = "I";

    private  Triplet<Vertex, Vertex, Vertex> triangle;

    private boolean R;

    public InteriorNode(AbstractGraph graph, String id, Point3d coordinates) {
        super(graph, id, INTERIOR_SYMBOL, coordinates);
    }

    public InteriorNode(AbstractGraph graph, String id, Vertex v1, Vertex v2, Vertex v3) {
        super(graph, id, INTERIOR_SYMBOL, getInteriorPosition(v1, v2, v3));
        triangle = new Triplet<>(v1, v2, v3);
        R = false;
    }


    public Triplet<Vertex, Vertex, Vertex> getTriangle(){
        return triangle;
    }

    public void setR(boolean R) {
        this.R = R;
    }

    public boolean isR() {
        return R;
    }

    public Triplet<Vertex, Vertex, Vertex> getTriangleVertexes() {
        return triangle;
    }

    private static Point3d getInteriorPosition(Vertex v1, Vertex v2, Vertex v3) {
        return new Point3d(getInteriorXCoordinate(v1, v2, v3), getInteriorYCoordinate(v1, v2, v3), getInteriorZCoordinate(v1, v2, v3));
    }

    private static double getInteriorXCoordinate(Vertex v1, Vertex v2, Vertex v3) {
        return (v1.getXCoordinate() + v2.getXCoordinate() + v3.getXCoordinate()) / 3d;
    }

    private static double getInteriorYCoordinate(Vertex v1, Vertex v2, Vertex v3) {
        return (v1.getYCoordinate() + v2.getYCoordinate() + v3.getYCoordinate()) / 3d;
    }

    private static double getInteriorZCoordinate(Vertex v1, Vertex v2, Vertex v3) {
        return (v1.getZCoordinate() + v2.getZCoordinate() + v3.getZCoordinate()) / 3d;
    }
}
