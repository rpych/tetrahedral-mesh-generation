package model;

//import org.graphstream.graph.implementations.AbstractGraph;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.*;

public class FaceNode extends GraphNode {

    private static final String FACE_SYMBOL = "F";

    private  Triplet<Vertex, Vertex, Vertex> triangle;

    private boolean R;

    public FaceNode(ModelGraph graph, String id, Coordinates coordinates) {
        super(graph, id, FACE_SYMBOL, coordinates);
    }

    public FaceNode(ModelGraph graph, String id, Vertex v1, Vertex v2, Vertex v3) {
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

    public boolean containsVertices(Vertex v1, Vertex v2){
        return ( v1.getId().equals(triangle.getValue0().getId()) || v1.getId().equals(triangle.getValue1().getId()) ||
                v1.getId().equals(triangle.getValue2().getId()) ) && ( v2.getId().equals(triangle.getValue0().getId()) ||
                v2.getId().equals(triangle.getValue1().getId()) || v2.getId().equals(triangle.getValue2().getId()) );
    }

    public Vertex[] getVerticesFromMap(FaceNode face){
        Vertex v0 = face.getTriangle().getValue0(), v1 = face.getTriangle().getValue1(), v2 = face.getTriangle().getValue2();
        Map<String, Vertex> commonVertices = new HashMap<>();
        commonVertices.put(v0.getId(), v0);
        commonVertices.put(v1.getId(), v1);
        commonVertices.put(v2.getId(), v2);
        commonVertices.put(triangle.getValue0().getId(), triangle.getValue0());
        commonVertices.put(triangle.getValue1().getId(), triangle.getValue1());
        commonVertices.put(triangle.getValue2().getId(), triangle.getValue2());
        Vertex[] vertices = new Vertex[4];
        int i = 0;
        for(Map.Entry<String, Vertex> vertex: commonVertices.entrySet()){
            vertices[i++] = vertex.getValue();
        }
        return vertices;
    }

    //method unused for now
    public Optional<Pair<Vertex, Vertex>> getVerticesFromCongruentEdge(FaceNode face){
        Vertex v0 = face.getTriangle().getValue0(), v1 = face.getTriangle().getValue1(), v2 = face.getTriangle().getValue2();
        if(this.isFaceCongruent(face)){
            if(containsVertices(v0, v1)){
                return Optional.of(new Pair<>(v0, v1));
            }
            else if(containsVertices(v0, v2)){
                return Optional.of(new Pair<>(v0, v2));
            }
            else if(containsVertices(v1, v2)){
                return Optional.of(new Pair<>(v1, v2));
            }
        }
        return Optional.empty();
    }

    public Optional<Pair<Vertex, Vertex>> getUncommonVerticesIfCongruent(FaceNode face){
        if(this.isFaceCongruent(face)){
            Vertex[] vertices = getVerticesFromMap(face);
            if(containsVertices(vertices[0], vertices[1])){
                return Optional.of(new Pair<>(vertices[2], vertices[3]));
            }
            else if(containsVertices(vertices[0], vertices[2])){
                return Optional.of(new Pair<>(vertices[1], vertices[3]));
            }
            else if(containsVertices(vertices[0], vertices[3])){
                return Optional.of(new Pair<>(vertices[1], vertices[2]));
            }
            else if(containsVertices(vertices[1], vertices[2])){
                return Optional.of(new Pair<>(vertices[0], vertices[3]));
            }
            else if(containsVertices(vertices[1], vertices[3])){
                return Optional.of(new Pair<>(vertices[0], vertices[2]));
            }
            else if(containsVertices(vertices[2], vertices[3])){
                return Optional.of(new Pair<>(vertices[0], vertices[1]));
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FaceNode f = (FaceNode) o;
        Vertex fv0 = f.getTriangle().getValue0(), fv1 = f.getTriangle().getValue1(), fv2 = f.getTriangle().getValue2();
        Vertex thisv0 = triangle.getValue0(), thisv1 = triangle.getValue1(), thisv2 = triangle.getValue2();
        return (fv0.getId().equals(thisv0.getId()) || fv0.getId().equals(thisv1.getId()) || fv0.getId().equals(thisv2.getId())) &&
                (fv1.getId().equals(thisv0.getId()) || fv1.getId().equals(thisv1.getId()) || fv1.getId().equals(thisv2.getId())) &&
                (fv2.getId().equals(thisv0.getId()) || fv2.getId().equals(thisv1.getId()) || fv2.getId().equals(thisv2.getId()));
    }


}
