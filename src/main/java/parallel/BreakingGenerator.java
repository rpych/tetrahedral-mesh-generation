package parallel;

import controller.TransformatorForLayers;
import model.*;
import model.helpers.BreakGenerationProvider;
import model.helpers.BreakSimulationNode;
import model.helpers.ThreadLockContainer;
import model.helpers.ThreadsDependencyGraph;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static common.Utils.isEdgeBetween;

public class BreakingGenerator extends BreakGenerationProvider implements Callable<Integer> { //implements Callable<Integer>

    private String threadName;
    private ModelGraph graph;
    //private Map<String, List<BreakSimulationNode>> breakPaths;
    private List<String> dependencyList; //list of superior threads to current one
    //private ThreadsDependencyGraph dependencyGraph;
    //private Map<String, Boolean> threadsExecutionFinished;
    //private Stack<GraphEdge> hangingEdges;
    //private Map<String, String> directSuperiorThreadNames;
    //private ThreadLockContainer lockContainer;
    //private volatile TransformatorForLayers observer;
    //private boolean visited = false;
    //private int step;
    Deque<BreakSimulationNode> simulatedPath;
    List<FaceNode> insertedImplicitFaces;

    ///*ThreadsDependencyGraph dependencyGraph, Map<String, String> directSuperiorThreadNames,*/
    public BreakingGenerator(ModelGraph graph, Map<String, Deque<BreakSimulationNode>> breakPaths,
                             FaceNode startFace, Deque<BreakSimulationNode> simulatedPath,
                             ThreadLockContainer container){
        super(graph, startFace);
        this.graph = graph;
        //this.breakPaths = breakPaths;
        this.dependencyList = new LinkedList<>();
        this.simulatedPath = simulatedPath;
        this.insertedImplicitFaces = new LinkedList<>();
        //this.dependencyGraph = dependencyGraph;
        //this.threadsExecutionFinished = threadsExecutionFinished;
        //this.observer = observer;
        //this.hangingEdges = new Stack<GraphEdge>();
        //this.directSuperiorThreadNames = directSuperiorThreadNames;
        //this.lockContainer = container;
    }

     /*private void prepareDependencyList(){
        //System.out.println("TOPOLOGICAL = "+ dependencyGraph.getTopologicalOrder().keySet());
        for(Map.Entry<String, List<String>> threadInfo: dependencyGraph.getAdjacencyMap().entrySet()){
            if(!threadInfo.getKey().equals(threadName) && threadInfo.getValue().contains(threadName)){
                dependencyList.add(threadInfo.getKey());
            }
        }
        System.out.println("THREAD_NAME = " + threadName + ", DEPENDENCY_LIST = "+  dependencyList+ ", SIZE = "+dependencyGraph.getAdjacencyMap().size());
    }*/



    @Override
    public Integer call(){
        threadName = Thread.currentThread().getName();

        //System.out.println("THREAD = " + threadName + ", SUPERIOR_THREAD = " + directSuperiorThreadNames.get(threadName));
        //if(shouldPrepareDependencyList()) prepareDependencyList();
        //prepareDependencyList();

        //String superiorThreadName = directSuperiorThreadNames.get(threadName);

        transform(graph);

        System.out.println("TRANSFORMED THREAD = " + threadName);

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
        /*Optional<FaceNode> faceOpt = graph.getFace(startFace.getId());
        FaceNode face = null;
        if(faceOpt.isPresent())
            face = faceOpt.get();*/

        System.out.println("GENERATOR:: SIMULATED_PATH EXECUTE = "+ simulatedPath.toString());

        for(BreakSimulationNode simNode: simulatedPath){
            String faceId = simNode.getFace().getId();
            FaceNode face = graph.getFaceNonOptional(faceId);
            //GraphEdge stackEdgeRef = simNode.getEdge();
            System.out.println("GENERATOR_FACE::"+ face.getId()+ ", EDGE = "+simNode.getEdge().getId() + ", STEP = "+step);
            if(step == 1){
                graph = breakFace(graph, face);
            }
            else{
                graph = processLastHangingNode(graph, face);
                //graph = addNewFaces(graph);
                List<FaceNode> facesToInsert = simNode.getFaceToInsertInThisStep();
                if(facesToInsert.size() == 0) continue;
                for(FaceNode fc: facesToInsert) {
                    Triplet<Vertex, Vertex, Vertex> triangle = fc.getTriangle();
                    Vertex v0 = graph.getVertexNonOptional(triangle.getValue0().getId());
                    Vertex v1 = graph.getVertexNonOptional(triangle.getValue1().getId());
                    Vertex v2 = graph.getVertexNonOptional(triangle.getValue2().getId());
                    FaceNode f = graph.insertFaceAutoNamed(v0, v1, v2);
                    insertedImplicitFaces.add(f);
                    System.out.println(threadName + " GENERATOR::ADD_NEW_FACE = " + f.getId());
                }
                /*if(existsFaceWithEdge(graph, stackEdgeRef) && hangingEdges.isEmpty()){
                    hangingEdges.push(stackEdgeRef);
                }*/
            }
            step++;
        }
        for(FaceNode face: insertedImplicitFaces){
            if(!graph.areVertexesLinked(face)){
                System.out.println(threadName + " REFJU "+ face.getId());
                graph.removeFace(face.getId());
            }
        }

        return graph;

        /*System.out.println("GENERATOR_START_FACE::"+ face.getId()+ " ,STACK_SIZE = "+hangingEdges.size());
        graph = breakFace(graph, face);
        //TransformatorForLayers.meshLogger.log("GENERATOR_START_FACE::"+ face.getId()+ " ,STACK_SIZE = "+hangingEdges.size());
        while( !hangingEdges.empty() ){
            step++;
            GraphEdge stackEdgeRef = hangingEdges.peek();
            System.out.println("GENERATOR::STACK = "+ hangingEdges.peek().getId());
            //TransformatorForLayers.meshLogger.log("GENERATOR::STACK = "+ hangingEdges.peek().getId());
            Optional<FaceNode> faceHN = findFaceWithHangingNode(graph);
            if(!faceHN.isPresent()) {
                System.err.println("Stack not empty but hanging node not found - top = "+hangingEdges.size());
                //TransformatorForLayers.meshLogger.log("Stack not empty but hanging node not found - top = "+hangingEdges.size());
                hangingEdges.pop();
                continue;
            }
            graph = processLastHangingNode(graph, faceHN.get());
            graph = addNewFaces(graph);
            if(existsFaceWithEdge(graph, stackEdgeRef) && hangingEdges.isEmpty()){
                hangingEdges.push(stackEdgeRef);
            }
            step++;
        }
        return graph;*/
    }

    /*private boolean shouldPrepareDependencyList(){
        return dependencyGraph.getTopologicalOrder().get(threadName).size() != 0;
    }*/

    private boolean isEligibleToRun(Map<String, Boolean> finishedMap){
        /*boolean res = true;
        for(Map.Entry<String, Boolean> info: threadsExecutionFinished.entrySet()){
            if(dependencyList.contains(info.getKey()))
                res = res && info.getValue();
        }
        //System.out.println("RES = "+ res);
        return res;*/
       //if(directSuperiorThreadNames.get(threadName).equals(threadName)) return true;
       //String superiorThreadName = directSuperiorThreadNames.get(threadName);
       //return observer.getIsThreadExecutionFinished(superiorThreadName);
        //return threadsExecutionFinished.get(superiorThreadName);
        return true;
    }




}
