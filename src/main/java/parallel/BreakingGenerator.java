package parallel;

import model.*;
import model.helpers.BreakGenerationProvider;
import model.helpers.BreakSimulationNode;
import org.javatuples.Triplet;
import java.util.*;
import java.util.concurrent.Callable;

public class BreakingGenerator extends BreakGenerationProvider implements Callable<Integer> { //implements Callable<Integer>

    private String threadName;
    private ModelGraph graph;
    Deque<BreakSimulationNode> simulatedPath;
    List<FaceNode> insertedImplicitFaces;

    public BreakingGenerator(ModelGraph graph, FaceNode startFace, Deque<BreakSimulationNode> simulatedPath){
        super(graph, startFace);
        this.graph = graph;
        this.simulatedPath = simulatedPath;
        this.insertedImplicitFaces = new LinkedList<>();
    }

    @Override
    public Integer call(){
        threadName = Thread.currentThread().getName();

        transform(graph);

        System.out.println("THREAD = " + threadName + " FINISHED");

        return 0;
    }

    @Override
    public ModelGraph transform() {
        return transform(graph);
    }

    @Override
    public ModelGraph transform(ModelGraph graph) {
        step = 1;

        System.out.println("GENERATOR:: SIMULATED_PATH EXECUTE = "+ simulatedPath.toString());

        for(BreakSimulationNode simNode: simulatedPath){
            String faceId = simNode.getFace().getId();
            FaceNode face = graph.getFaceNonOptional(faceId);
            System.out.println("GENERATOR_FACE::"+ face.getId()+ ", EDGE = "+simNode.getEdge().getId() + ", STEP = "+step);
            if(step == 1){
                graph = breakFace(graph, face);
            }
            else{
                graph = processLastHangingNode(graph, face);
                addNewImplicitFaces(simNode);
            }
            step++;
        }
        removeFakeFacesWithinImplicitFaces();

        return graph;
    }

    private void addNewImplicitFaces(BreakSimulationNode simNode){
        List<FaceNode> facesToInsert = simNode.getFaceToInsertInThisStep();
        if(facesToInsert.size() == 0) return;

        for(FaceNode fc: facesToInsert) {
            Triplet<Vertex, Vertex, Vertex> triangle = fc.getTriangle();
            Vertex v0 = graph.getVertexNonOptional(triangle.getValue0().getId());
            Vertex v1 = graph.getVertexNonOptional(triangle.getValue1().getId());
            Vertex v2 = graph.getVertexNonOptional(triangle.getValue2().getId());
            FaceNode f = graph.insertFaceAutoNamed(v0, v1, v2);
            insertedImplicitFaces.add(f);
            System.out.println(threadName + " GENERATOR::ADD_NEW_FACE = " + f.getId());
        }
    }

    private void removeFakeFacesWithinImplicitFaces(){
        for(FaceNode face: insertedImplicitFaces){
            if(!graph.areVertexesLinked(face)){
                System.out.println(threadName + " GENERATOR::REMOVE_NEW_FACE "+ face.getId());
                graph.removeFace(face.getId());
            }
        }
    }
}
