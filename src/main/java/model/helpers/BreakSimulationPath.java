package model.helpers;

import model.FaceNode;
import model.GraphEdge;

public class BreakSimulationPath {

    private int stepNo;
    private FaceNode face;
    private GraphEdge edge;

    public BreakSimulationPath(int stepNo, FaceNode face, GraphEdge edge) {
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
}
