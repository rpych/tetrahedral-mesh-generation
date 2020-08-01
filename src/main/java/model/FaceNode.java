package model;

import org.graphstream.graph.implementations.AbstractGraph;
import org.javatuples.Triplet;

import java.util.HashSet;
import java.util.Set;

public class FaceNode extends GraphNode {

    private static final String FACE_SYMBOL = "F";

    private  Triplet<Vertex, Vertex, Vertex> triangle;

    private boolean R;

    public FaceNode(AbstractGraph graph, String id, Coordinates coordinates) {
        super(graph, id, FACE_SYMBOL, coordinates);
    }

    public FaceNode(AbstractGraph graph, String id, Vertex v1, Vertex v2, Vertex v3) {
        super(graph, id, FACE_SYMBOL, getFacePosition(v1, v2, v3));
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

    public Triplet<Vertex, Vertex, Vertex> getTriangleVertices() {
        return triangle;
    }
    
    private static Coordinates getFacePosition(Vertex v1, Vertex v2, Vertex v3) {
        return new Coordinates(getFaceXCoordinate(v1, v2, v3), getFaceYCoordinate(v1, v2, v3), getFaceZCoordinate(v1, v2, v3));
    }

    private static double getFaceXCoordinate(Vertex v1, Vertex v2, Vertex v3) {
        return (v1.getXCoordinate() + v2.getXCoordinate() + v3.getXCoordinate()) / 3d;
    }

    private static double getFaceYCoordinate(Vertex v1, Vertex v2, Vertex v3) {
        return (v1.getYCoordinate() + v2.getYCoordinate() + v3.getYCoordinate()) / 3d;
    }

    private static double getFaceZCoordinate(Vertex v1, Vertex v2, Vertex v3) {
        return (v1.getZCoordinate() + v2.getZCoordinate() + v3.getZCoordinate()) / 3d;
    }

    /*if faces are different but have common edge then set of their vertices has size of 4*/
    public boolean isFaceCongruent(FaceNode face){
        Set<Vertex> commonVertices = new HashSet<>();
        Triplet<Vertex, Vertex, Vertex> faceTriangle = face.getTriangleVertices();
        commonVertices.add(faceTriangle.getValue0());
        commonVertices.add(faceTriangle.getValue1());
        commonVertices.add(faceTriangle.getValue2());
        commonVertices.add(triangle.getValue0());
        commonVertices.add(triangle.getValue1());
        commonVertices.add(triangle.getValue2());

        return commonVertices.size() == 4;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FaceNode f = (FaceNode) o;
        Vertex fv0 = f.getTriangle().getValue0(), fv1 = f.getTriangle().getValue1(), fv2 = f.getTriangle().getValue2();
        Vertex thisv0 = triangle.getValue0(), thisv1 = triangle.getValue1(), thisv2 = triangle.getValue2();
        return (fv0.getCoordinates().equals(thisv0.getCoordinates()) ||  fv0.getCoordinates().equals(thisv1.getCoordinates()) ||
                fv0.getCoordinates().equals(thisv2.getCoordinates())) && (fv1.getCoordinates().equals(thisv0.getCoordinates()) ||
                fv1.getCoordinates().equals(thisv1.getCoordinates()) || fv1.getCoordinates().equals(thisv2.getCoordinates())) &&
                (fv2.getCoordinates().equals(thisv0.getCoordinates()) ||  fv2.getCoordinates().equals(thisv1.getCoordinates()) ||
                fv2.getCoordinates().equals(thisv2.getCoordinates()));
    }


}
