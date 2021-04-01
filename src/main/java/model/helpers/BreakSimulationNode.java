package model.helpers;

import model.FaceNode;
import model.GraphEdge;

public class BreakSimulationNode {

    private int stepNo;
    private FaceNode face;
    private GraphEdge edge;

    public BreakSimulationNode(int stepNo, FaceNode face, GraphEdge edge) {
        this.stepNo = stepNo;
        this.face = face;
        this.edge = edge;
    }

    public int getStepNo() {
        return stepNo;
    }

    public void setStepNo(int stepNo) {
        this.stepNo = stepNo;
    }

    public FaceNode getFace() {
        return face;
    }

    public void setFace(FaceNode face) {
        this.face = face;
    }

    public GraphEdge getEdge() {
        return edge;
    }

    public void setEdge(GraphEdge edge) {
        this.edge = edge;
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        BreakSimulationNode node = (BreakSimulationNode) o;
        return this.face.equals(node.getFace()) &&
               this.edge.getId().equals(node.getEdge().getId());
    }

    @Override
    public String toString(){
        return "FACE=" + face.getId() + ";EDGE=" + edge.getId() + ";STEP=" + stepNo;
    }
}
