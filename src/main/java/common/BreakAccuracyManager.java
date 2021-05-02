package common;

import app.Config;
import model.InteriorNode;
import model.ModelGraph;

public class BreakAccuracyManager {

    private int numOfIntNodesBelowThreshIntermed;
    private int numOfIntNodesBelowThreshLow;

    public BreakAccuracyManager(){
        numOfIntNodesBelowThreshIntermed = 0;
        numOfIntNodesBelowThreshLow = 0;
    }


    public boolean isEnoughBreakingAccuracy(ModelGraph graph){
        int numOfReqIntNodesBelowThresh = Config.REQ_INTERIORS_NUM;
        numOfIntNodesBelowThreshIntermed = 0;
        numOfIntNodesBelowThreshLow = 0;
        for(InteriorNode interiorNode: graph.getInteriorNodes()){
            boolean resIntermed = LFunction.isDistanceToLayerBelowThreshold(LFunction.LAYER.INTERMEDIATE, interiorNode.getCoordinates());
            boolean resLowest = LFunction.isDistanceToLayerBelowThreshold(LFunction.LAYER.LOWEST, interiorNode.getCoordinates());
            if(resIntermed) numOfIntNodesBelowThreshIntermed++;
            if(resLowest) numOfIntNodesBelowThreshLow++;
            if(numOfIntNodesBelowThreshIntermed >= numOfReqIntNodesBelowThresh &&
                    numOfIntNodesBelowThreshLow >= numOfReqIntNodesBelowThresh){
                return true;
            }
        }
        System.out.println("Intermediate threshold = "+ numOfIntNodesBelowThreshIntermed + ", Low threshold = "+ numOfIntNodesBelowThreshLow);
        return false;
    }

    public boolean isEnoughNumOfIntNodesBelowThreshIntermed(){
        return numOfIntNodesBelowThreshIntermed > (1.5* Config.REQ_INTERIORS_NUM);
    }

    public boolean isEnoughNumOfIntNodesBelowThreshLow(){
        return numOfIntNodesBelowThreshLow > (1.5 * Config.REQ_INTERIORS_NUM);
    }
}
