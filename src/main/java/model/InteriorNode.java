package model;

import org.javatuples.Quartet;

import static common.Utils.isVertexSameAs;

public class InteriorNode extends GraphNode{

    public static final String INTERIOR_SYMBOL = "I";
    private Quartet<Vertex, Vertex, Vertex, Vertex> quartet;
    private boolean isNewlyAdded = false;

    public InteriorNode(Graph graph, String id, Coordinates coordinates) {
        super(graph, id, INTERIOR_SYMBOL, coordinates);
    }

    public InteriorNode(Graph graph, String id, Vertex v1, Vertex v2, Vertex v3, Vertex v4) {
        super(graph, id, INTERIOR_SYMBOL, getInteriorNodePosition(v1, v2, v3, v4));
        quartet = new Quartet<>(v1, v2, v3, v4);
    }

    public Quartet<Vertex, Vertex, Vertex, Vertex> getQuartet() { return  quartet; }

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

    public void setIsNewlyAdded(boolean isNewlyAdded){
        this.isNewlyAdded = isNewlyAdded;
    }

    public boolean isNewlyAdded(){
        return isNewlyAdded;
    }

    public boolean checkSameVerticesInInteriorNode(Quartet<Vertex, Vertex, Vertex, Vertex> candSubGraph){
        Quartet<Vertex, Vertex, Vertex, Vertex> quartetNode = this.getQuartet();

        return (isVertexSameAs(candSubGraph.getValue0(), quartetNode.getValue0()) || isVertexSameAs(candSubGraph.getValue0(), quartetNode.getValue1()) ||
                isVertexSameAs(candSubGraph.getValue0(), quartetNode.getValue2()) || isVertexSameAs(candSubGraph.getValue0(), quartetNode.getValue3())) &&
                (isVertexSameAs(candSubGraph.getValue1(), quartetNode.getValue0()) || isVertexSameAs(candSubGraph.getValue1(), quartetNode.getValue1()) ||
                        isVertexSameAs(candSubGraph.getValue1(), quartetNode.getValue2()) || isVertexSameAs(candSubGraph.getValue1(), quartetNode.getValue3())) &&
                (isVertexSameAs(candSubGraph.getValue2(), quartetNode.getValue0()) || isVertexSameAs(candSubGraph.getValue2(), quartetNode.getValue1()) ||
                        isVertexSameAs(candSubGraph.getValue2(), quartetNode.getValue2()) || isVertexSameAs(candSubGraph.getValue2(), quartetNode.getValue3())) &&
                (isVertexSameAs(candSubGraph.getValue3(), quartetNode.getValue0()) || isVertexSameAs(candSubGraph.getValue3(), quartetNode.getValue1()) ||
                        isVertexSameAs(candSubGraph.getValue3(), quartetNode.getValue2()) || isVertexSameAs(candSubGraph.getValue3(), quartetNode.getValue3()));

    }

    public boolean containsFace(FaceNode face){
        Vertex v0 = face.getTriangle().getValue0(), v1 = face.getTriangle().getValue1(), v2 = face.getTriangle().getValue2();
        return ( v0.getId().equals(quartet.getValue0().getId()) || v0.getId().equals(quartet.getValue1().getId()) ||
                v0.getId().equals(quartet.getValue2().getId()) || v0.getId().equals(quartet.getValue3().getId()) ) &&
                ( v1.getId().equals(quartet.getValue0().getId()) || v1.getId().equals(quartet.getValue1().getId()) ||
                v1.getId().equals(quartet.getValue2().getId()) || v1.getId().equals(quartet.getValue3().getId()) ) &&
                ( v2.getId().equals(quartet.getValue0().getId()) || v2.getId().equals(quartet.getValue1().getId()) ||
                v2.getId().equals(quartet.getValue2().getId()) || v2.getId().equals(quartet.getValue3().getId()) );
    }
}
