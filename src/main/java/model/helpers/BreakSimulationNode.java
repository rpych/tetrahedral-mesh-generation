package model.helpers;

import model.FaceNode;
import model.GraphEdge;

import java.util.LinkedList;
import java.util.List;

public class BreakSimulationNode {

    private int stepNo;
    private FaceNode face;
    private GraphEdge edge;
    private List<FaceNode> facesToInsertInThisStep;

    public BreakSimulationNode(int stepNo, FaceNode face, GraphEdge edge) {
        this.stepNo = stepNo;
        this.face = face;
        this.edge = edge;
        this.facesToInsertInThisStep = new LinkedList<>();
    }

    public void addFaceToInsertInThisStep(FaceNode face){
        facesToInsertInThisStep.add(face);
    }

    public List<FaceNode> getFaceToInsertInThisStep() {
        return facesToInsertInThisStep;
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

    /*equals is a bit different than typical, because we need to detect conflict when faces OR edges are overlapping*/
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        BreakSimulationNode node = (BreakSimulationNode) o;
        return this.face.equals(node.getFace()) ||
               this.edge.getId().equals(node.getEdge().getId());
    }

    @Override
    public String toString(){
        return "FACE=" + face.getId() + ";EDGE=" + edge.getId() + ";STEP=" + stepNo + ";FACE_TO_INSERT="+facesToInsertInThisStep.toString();
    }
}
