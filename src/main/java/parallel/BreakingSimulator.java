package parallel;

import controller.TransformatorForLayers;
import model.*;
import model.helpers.BreakConflictContainer;
import model.helpers.BreakSimulationNode;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CyclicBarrier;

import static common.Utils.isEdgeBetween;

public class BreakingSimulator implements Callable<Integer> {

    private String threadName;
    private TransformatorForLayers observer;
    private ModelGraph graph;
    private Stack<GraphEdge> hangingEdges;
    private Optional<FaceNode> startFace;
    private Deque<BreakSimulationNode> simPaths;
    private BreakConflictContainer conflictContainer;
    private int step = 0;

    private CyclicBarrier cyclicBarrier;

    public BreakingSimulator(TransformatorForLayers transformator, String startFaceId, CyclicBarrier cyclicBarrier){
        this.observer = transformator;
        this.graph = new ModelGraph(transformator.graph); //copy ctor
        this.hangingEdges = new Stack<GraphEdge>();
        this.startFace = this.graph.getFace(startFaceId);
        this.simPaths = new ConcurrentLinkedDeque<>();
        this.conflictContainer = new BreakConflictContainer();
        this.threadName = "";
        this.cyclicBarrier = cyclicBarrier;
    }

    @Override
    public Integer call(){
        threadName = Thread.currentThread().getName();
        conflictContainer.setThreadName(threadName);
        transform();
        updateObserversSimulationPaths();
        try {
            System.out.println("Waiting on await "+ TransformatorForLayers.counter + ", thread = " + threadName + " SIZE WAITING = "+ cyclicBarrier.getNumberWaiting()+ " BROKEN = "+ cyclicBarrier.isBroken());
            cyclicBarrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
        findConflictsInSimulationPath();
        updateObserversThreadsConflicts();
        return 0;
    }

    public ModelGraph transform() {
        return transform(this.graph);
    }

    public ModelGraph transform(ModelGraph graph) {
        Optional<FaceNode> face = this.startFace;
        if(face.isPresent()){
            step = 1;
            graph = breakFace(graph, face.get()); //here insert edge to break (E) on stack
            while( !hangingEdges.empty() ){
                step++;
                GraphEdge stackEdgeRef = hangingEdges.peek();
                System.out.println(threadName + "::STACK = "+ hangingEdges.peek().getId());
                //TransformatorForLayers.meshLogger.log("STACK = "+ hangingEdges.peek().getId());
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
            }
        }
        return graph;
    }

    public synchronized void updateObserversSimulationPaths(){
        observer.updateSimulationPathInfo(threadName, this.simPaths);
    }

    public synchronized void updateObserversThreadsConflicts(){
        observer.updateConflictInfo(conflictContainer);
    }

    public synchronized void findConflictsInSimulationPath(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Deque<BreakSimulationNode> simulationPath =  this.observer.breakSimulationPaths.get(this.threadName);
        for(Map.Entry<String, Deque<BreakSimulationNode>> breakInfo: this.observer.breakSimulationPaths.entrySet()){
            if(threadName.equals(breakInfo.getKey())) continue;

            findConflictsWithOtherThreads(breakInfo.getKey(), breakInfo.getValue(), simulationPath);
        }
    }

    public void findConflictsWithOtherThreads(String otherThreadName, Deque<BreakSimulationNode> breakPath, Deque<BreakSimulationNode> ownBreakPath){
        for(BreakSimulationNode ownNode: ownBreakPath){
            for(BreakSimulationNode otherNode: breakPath){
                if(ownNode.equals(otherNode)){
                    conflictContainer.addConflictWithThread(otherThreadName, ownNode);
                    return;
                }
            }
        }
    }

    public Optional<FaceNode> findFaceWithHangingNode(ModelGraph graph){
        Vertex v0 = (Vertex)hangingEdges.peek().getEdgeNodes().getValue0(), v1 = (Vertex)hangingEdges.peek().getEdgeNodes().getValue1();
        FaceNode faceWithLongestEdge = null;
        double longestEdgeLen = 0.0;
        for(FaceNode face: graph.getFaces()){
            if(face.containsVertices(v0, v1) && !graph.areVertexesLinked(face)){
                Pair<Vertex, Vertex> longEdgeVert = getLongestEdgeVerticesFromFace(face);
                double len = Coordinates.distance(longEdgeVert.getValue0().getCoordinates(), longEdgeVert.getValue1().getCoordinates());
                if(len > longestEdgeLen){
                    faceWithLongestEdge = face;
                    longestEdgeLen = len;
                }
            }
        }
        return Optional.ofNullable(faceWithLongestEdge);
    }

    public ModelGraph processLastHangingNode(ModelGraph graph, FaceNode face){
        Pair<Vertex, Vertex> longEdgeVert = getLongestEdgeVerticesFromFace(face);
        GraphEdge longestEdge = graph.insertEdgeAutoNamedOrGet(longEdgeVert.getValue0(), longEdgeVert.getValue1(), false);
        Vertex vOpposite = getVertexForNewEdge(face, longEdgeVert);
        graph = performBreaking(graph, vOpposite, longestEdge);
        System.out.println(threadName + "::HN::longestEdge = "+longestEdge.getId() + ", OPPOSITE = "+ vOpposite.getId());

        simPaths.add(new BreakSimulationNode(step, face, longestEdge));

        if(isLastHNRefined(longestEdge) && !existsFaceWithEdge(graph, longestEdge)){
            hangingEdges.pop();
        }
        else if(!isLastHNRefined(longestEdge)){
            hangingEdges.push(longestEdge);
        }
        return graph;
    }

    public boolean isLastHNRefined(GraphEdge longestEdge){
        return (hangingEdges.peek().getEdgeNodes().getValue0().getId().equals(longestEdge.getEdgeNodes().getValue0().getId()) ||
                hangingEdges.peek().getEdgeNodes().getValue0().getId().equals(longestEdge.getEdgeNodes().getValue1().getId())) &&
                (hangingEdges.peek().getEdgeNodes().getValue1().getId().equals(longestEdge.getEdgeNodes().getValue0().getId()) ||
                        hangingEdges.peek().getEdgeNodes().getValue1().getId().equals(longestEdge.getEdgeNodes().getValue1().getId()));
    }

    public boolean existsFaceWithEdge(ModelGraph graph, GraphEdge edge){
        Vertex v0 = (Vertex)edge.getEdgeNodes().getValue0(), v1 = (Vertex)edge.getEdgeNodes().getValue1();
        return getFacesWithEdge(graph, v0, v1).size() > 0;
    }

    public ModelGraph breakFace(ModelGraph graph, FaceNode face){
        Pair<Vertex, Vertex> longEdgeVert = getLongestEdgeVerticesFromFace(face);
        GraphEdge longestEdge = graph.getEdgeBetweenNodes(longEdgeVert.getValue0(), longEdgeVert.getValue1()).get();
        Vertex vOpposite = getVertexForNewEdge(face, longEdgeVert);
        graph = performBreaking(graph, vOpposite, longestEdge);

        simPaths.add(new BreakSimulationNode(step, face, longestEdge));

        System.out.println(threadName + "::longestEdge = "+longestEdge.getId() + ", OPPOSITE = "+ vOpposite.getId());
        hangingEdges.push(longestEdge);

        return graph;
    }

    public ModelGraph performBreaking(ModelGraph graph, Vertex opposite, GraphEdge edge){
        Vertex newVertex = graph.insertVertexAutoNamedOrGet(edge.getMiddlePointCoordinates());
        graph.insertEdgeAutoNamedOrGet(opposite, newVertex, false);

        graph.deleteEdge(edge.getId());
        graph.insertEdgeAutoNamedOrGet(edge.getEdgeNodes().getValue0(), newVertex, true);
        graph.insertEdgeAutoNamedOrGet(edge.getEdgeNodes().getValue1(), newVertex, true);

        Triplet<Vertex, Vertex, Vertex> triangle = new Triplet<>(opposite, (Vertex) edge.getEdgeNodes().getValue0(), (Vertex) edge.getEdgeNodes().getValue1());
        graph = removeFace(graph, triangle);
        graph.insertFaceAutoNamed(opposite, newVertex, (Vertex) edge.getEdgeNodes().getValue0());
        graph.insertFaceAutoNamed(opposite, newVertex, (Vertex) edge.getEdgeNodes().getValue1());

        return graph;
    }

    public Collection<FaceNode> getFacesWithEdge(ModelGraph graph, Vertex v0, Vertex v1){
        Collection<FaceNode> facesWithBrokenEdge = new LinkedList<>();
        for(FaceNode face: graph.getFaces()){
            if(face.containsVertices(v0, v1)){
                facesWithBrokenEdge.add(face);
            }
        }
        return facesWithBrokenEdge;
    }

    private ModelGraph addNewFaces(ModelGraph graph) {
        Collection<GraphEdge> ebv = graph.getEdgesBetweenVertices();
        for(GraphEdge edge : ebv) {
            Pair<Vertex, Vertex> edgeVertices = edge.getVertices();
            Collection<Vertex> cv = graph.getCommonVertices(edgeVertices.getValue0(), edgeVertices.getValue1());
            for(Vertex v : cv) {
                if(!graph.hasFaceNode(edgeVertices.getValue0(), edgeVertices.getValue1(), v)) {
                    FaceNode f = graph.insertFaceAutoNamed(edgeVertices.getValue0(), edgeVertices.getValue1(), v);
                    simPaths.getLast().addFaceToInsertInThisStep(f);
                    System.out.println(threadName + " SIMULATOR::ADD_NEW_FACE = "+ f.getId());
                }
            }
        }
        return graph;
    }

    private ModelGraph removeFace(ModelGraph modelGraph, Triplet<Vertex, Vertex, Vertex> triangle) {
        FaceNode face = modelGraph.getFace(triangle);
        modelGraph.removeFace(face.getId());
        return modelGraph;
    }

    private Vertex getVertexForNewEdge(FaceNode face, Pair<Vertex, Vertex> vertexes){
        Triplet<Vertex, Vertex, Vertex> triangle = face.getTriangle();
        if(!triangle.getValue0().getId().equals(vertexes.getValue0().getId()) &&
                !triangle.getValue0().getId().equals(vertexes.getValue1().getId())) {
            return triangle.getValue0();
        }else if(!triangle.getValue1().getId().equals(vertexes.getValue0().getId()) &&
                !triangle.getValue1().getId().equals(vertexes.getValue1().getId())) {
            return triangle.getValue1();
        }
        return triangle.getValue2();
    }

    public Pair<Vertex, Vertex> getLongestEdgeVerticesFromFace(FaceNode face){
        Vertex v0 = face.getTriangle().getValue0(), v1 = face.getTriangle().getValue1(), v2 = face.getTriangle().getValue2();
        double longestEdgeLen = Coordinates.distance(v0.getCoordinates(), v1.getCoordinates());
        Pair<Vertex, Vertex> longestEdge = new Pair<>(v0, v1);
        double v0v2Len = Coordinates.distance(v0.getCoordinates(), v2.getCoordinates());
        double v1v2Len = Coordinates.distance(v1.getCoordinates(), v2.getCoordinates());

        if(Double.compare(v0v2Len, longestEdgeLen) >= 0){ //0 equal, 1: first > sec, -1: first < sec
            if(Double.compare(v0v2Len, longestEdgeLen) == 1){
                longestEdgeLen = v0v2Len;
                longestEdge = new Pair<>(v0, v2);
            }
            else if((Double.compare(v0v2Len, longestEdgeLen) == 0) && isEdgeBetween(longestEdge.getValue0(), longestEdge.getValue1())
                    && !isEdgeBetween(v0, v2)){
                longestEdgeLen = v0v2Len;
                longestEdge = new Pair<>(v0, v2);
            }
        }
        if(Double.compare(v1v2Len, longestEdgeLen) >= 0){
            if(Double.compare(v1v2Len, longestEdgeLen) == 1){
                longestEdge = new Pair<>(v1, v2);
            }
            else if((Double.compare(v1v2Len, longestEdgeLen) == 0) && isEdgeBetween(longestEdge.getValue0(), longestEdge.getValue1())
                    && !isEdgeBetween(v1, v2)){
                longestEdge = new Pair<>(v1, v2);
            }
        }
        return longestEdge;
    }
}
