package controller;

import app.Config;
import common.BreakingStats;
import common.LFunction;
import logger.MeshLogger;
import model.*;
import model.helpers.BreakConflictContainer;
import model.helpers.BreakSimulationNode;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import parallel.BreakingSimulator;
import parallel.TetrahedraGenerator;
import visualization.MatlabVisualizer;

import java.util.*;
import java.util.concurrent.*;

import static common.Utils.isEdgeBetween;

public class TransformatorForLayers implements ITransformator{
    public ModelGraph graph;
    public Stack<GraphEdge> hangingEdges;
    public static Integer counter = 0;
    private TetThreadPoolManager tetGenManager;
    public Map<String, List<BreakSimulationNode>> breakSimulationPaths;
    public Deque<BreakConflictContainer> threadsConflicts;

    private BreakingStats stats;
    MeshLogger meshLogger = new MeshLogger(TransformatorForLayers.class.getName(), MeshLogger.LogHandler.FILE_HANDLER);


    public TransformatorForLayers(ModelGraph graph, BreakingStats stats) {
        this.graph = graph;
        this.hangingEdges = new Stack<GraphEdge>();
        this.tetGenManager = new TetThreadPoolManager();
        this.stats = stats;
        this.breakSimulationPaths = new ConcurrentSkipListMap<>();
        this.threadsConflicts = new ConcurrentLinkedDeque<>();
    }

    public ModelGraph transform() {
        return transform(this.graph);
    }

    public ModelGraph transform(ModelGraph graph) {
        Set<FaceNode> faces = findFacesToBreak(graph);
        counter  = 0;
        while(!faces.isEmpty()){
            runBreakingSimulations(faces);
            graph = createNewInteriorNodes();
            faces = findFacesToBreak(graph);
            counter++;
            if(counter == 20 ) {
                this.tetGenManager.shutdownThreadPool();
                break;
            }

            //breaking ratio checking
            stats.checkAdaptationProperties(graph);

            if(counter % 20 == 0 || counter <= 300) {
                if (!stats.checkAllFacesBelongToInteriorNode(graph)) {
                    System.err.println("Some FACES do not belong to any interiorNode " + counter);
                } else
                    System.out.println("Faces correctly matched with Interiors " + counter + " ......................................");
                //stats.checkInteriorNodesMinMaxBreakingRatio(graph);
                System.out.println("FACES = "+ graph.getFaces().size() + ", INTERIORS = "+graph.getInteriorNodes().size() +
                        ", VERTICES = "+ graph.getVertices().size() + ", EDGES = "+(graph.getEdges().size()) );
            }
            if (isEnoughBreakingAccuracy(graph)) {
                tetGenManager.shutdownThreadPool();
                System.out.println("ENOUGH accuracy met");
                if (!stats.checkAllFacesBelongToInteriorNode(graph)) {
                    System.err.println("Some FACES do not belong to any interiorNode " + counter);
                } else
                    System.out.println("Faces correctly matched with Interiors " + counter + " ......................................");
                System.out.println("FACES = "+ graph.getFaces().size() + ", INTERIORS = "+graph.getInteriorNodes().size() +
                        ", VERTICES = "+ graph.getVertices().size() + ", EDGES = "+ (graph.getEdges().size()));
                MatlabVisualizer matlabVisualizer = new MatlabVisualizer(graph, "visLayCuboid10_03_20_Par_" + counter);
                matlabVisualizer.saveCode();
                break;
            }
            //stats.checkFacesConnected(graph);

        }
        return graph;
    }

    public void runBreakingSimulations(Set<FaceNode> faces){
        this.tetGenManager.createBreakingSimulationTasks(this, faces);
    }

    public Optional<FaceNode> findFaceToBreak(ModelGraph graph){
        for(FaceNode face: graph.getFaces()){
            if(face.isR()) return Optional.of(face);
        }
        return Optional.empty();
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
        System.out.println("HN::longestEdge = "+longestEdge.getId() + ", OPPOSITE = "+ vOpposite.getId());

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
        return getFacesWithEdge(graph, edge).size() > 0;
    }

    public ModelGraph breakFace(ModelGraph graph, FaceNode face){
        Pair<Vertex, Vertex> longEdgeVert = getLongestEdgeVerticesFromFace(face);
        GraphEdge longestEdge = graph.getEdgeBetweenNodes(longEdgeVert.getValue0(), longEdgeVert.getValue1()).get();
        Vertex vOpposite = getVertexForNewEdge(face, longEdgeVert);
        graph = performBreaking(graph, vOpposite, longestEdge);

        System.out.println("longestEdge = "+longestEdge.getId() + ", OPPOSITE = "+ vOpposite.getId());
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



    public Collection<FaceNode> getFacesWithEdge(ModelGraph graph, GraphEdge edge){
        Vertex v0 = (Vertex)edge.getEdgeNodes().getValue0(), v1 = (Vertex)edge.getEdgeNodes().getValue1();
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
                    graph.insertFaceAutoNamed(edgeVertices.getValue0(), edgeVertices.getValue1(), v);
                }
            }
        }
        return graph;
    }

    private ModelGraph markFacesToBreak(ModelGraph graph) {
        FaceNode faceWithLongestEdge = null;
        double longestEdgeLen = 0.0;
        for(FaceNode faceNode : graph.getFaces()) {
            if(checkEdgesOnLayersBorder(graph, faceNode)) {
                Pair<Vertex, Vertex> longEdgeVert = getLongestEdgeVerticesFromFace(faceNode);
                double len = Coordinates.distance(longEdgeVert.getValue0().getCoordinates(), longEdgeVert.getValue1().getCoordinates());
                if(len > longestEdgeLen){
                    faceWithLongestEdge = faceNode;
                    longestEdgeLen = len;
                }
            }
        }
        if(faceWithLongestEdge != null) faceWithLongestEdge.setR(true);

        return graph;
    }

    private Set<FaceNode> findFacesToBreak(ModelGraph graph) {
        FaceNode faceWithShortLongestEdge = null;
        double shortLongestEdgeLen = 0.0;
        Map<FaceNode, Double> facesToBreak = new HashMap<>();
        for(FaceNode faceNode : graph.getFaces()) {
            if(checkEdgesOnLayersBorder(graph, faceNode)) {
                Pair<Vertex, Vertex> longEdgeVert = getLongestEdgeVerticesFromFace(faceNode);
                double len = Coordinates.distance(longEdgeVert.getValue0().getCoordinates(), longEdgeVert.getValue1().getCoordinates());
                if(facesToBreak.size() < Config.THREADS_IN_POOL){
                    facesToBreak.put(faceNode, len);
                }
                else if(facesToBreak.size() == Config.THREADS_IN_POOL && len > shortLongestEdgeLen){
                    if(facesToBreak.containsKey(faceWithShortLongestEdge)){
                        facesToBreak.remove(faceWithShortLongestEdge);
                        facesToBreak.put(faceNode, len);
                    }
                }
                Pair<FaceNode, Double> res = getMinsFromMap(facesToBreak);
                faceWithShortLongestEdge = res.getValue0();
                shortLongestEdgeLen = res.getValue1();
            }
        }
        facesToBreak.keySet().forEach(face -> face.setR(true));

        return facesToBreak.keySet();
    }

    public Pair<FaceNode, Double> getMinsFromMap(Map<FaceNode, Double> facesMinEdges){
        FaceNode minFace = null;
        Double minEdgeLen = 1000.0;
        for(Map.Entry<FaceNode, Double> el: facesMinEdges.entrySet()){
            if(el.getValue() < minEdgeLen) {
                minEdgeLen = el.getValue();
                minFace = el.getKey();
            }
        }
        return new Pair<>(minFace, minEdgeLen);
    }

    public boolean checkEdgesOnLayersBorder(ModelGraph graph, FaceNode face){
        Triplet<Vertex, Vertex, Vertex> triangle = face.getTriangle();

        if(LFunction.areDifferentLayers( triangle.getValue0().getCoordinates(),
                triangle.getValue1().getCoordinates() ))
        {
            return true;
        }
        if(LFunction.areDifferentLayers( triangle.getValue0().getCoordinates(),
                triangle.getValue2().getCoordinates()) )
        {
            return true;
        }
        if(LFunction.areDifferentLayers( triangle.getValue1().getCoordinates(),
                triangle.getValue2().getCoordinates()) )
        {
            return true;
        }
        return false;
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

    //InteriorNode part

    public ModelGraph createNewInteriorNodes(){
        graph.setOldInteriorNodes(graph);
        graph.clearInteriorNodes();
        //System.out.println("OLD size = "+ graph.interiorNodesOld.size() + ", interiorNodes size = "+graph.getInteriorNodes().size());
        //return graph.createInteriorNodesForNewlyFoundSubGraphs();
        tetGenManager.createGenerationTasks(graph.getFacesNum());
        return graph;
    }

    //checks


    public boolean isEnoughBreakingAccuracy(ModelGraph graph){
        int numOfReqIntNodesBelowThresh = 20, numOfIntNodesBelowThreshIntermed = 0, numOfIntNodesBelowThreshLow = 0;
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

    //inner class
    private class TetThreadPoolManager {
        final Integer POOL_SIZE = Config.THREADS_IN_POOL;
        ExecutorService service = Executors.newFixedThreadPool(POOL_SIZE);

        private void createGenerationTasks(Integer facesCollectionSize){
            int facesPerThread = facesCollectionSize / POOL_SIZE;
            Collection< Callable<Integer> > taskRes = new LinkedList<>();

            if(facesPerThread < 20){
                taskRes.add(new TetrahedraGenerator(graph, 1, facesCollectionSize));
            }
            else {
                for (int i = 0; i < POOL_SIZE; ++i) {
                    int facesPerThreadUpdated = ((i + 1) < POOL_SIZE) ? facesPerThread : (facesPerThread + (facesCollectionSize % POOL_SIZE));
                    taskRes.add(new TetrahedraGenerator(graph, (facesPerThread * i + 1), facesPerThreadUpdated));
                }
            }

            try {
                List<Future<Integer>> futures = service.invokeAll(taskRes);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        private void createBreakingSimulationTasks(TransformatorForLayers transformator, Set<FaceNode> faces){
            Collection< Callable<Integer> > taskRes = new LinkedList<>();
            for(FaceNode face: faces){
                //System.out.println("ELEMENT = "+ face.getId());
                taskRes.add(new BreakingSimulator(transformator, face.getId()));
            }
            try {
                List<Future<Integer>> futures = service.invokeAll(taskRes);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void shutdownThreadPool(){
            service.shutdown();
            try {
                if (!service.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                    service.shutdownNow();
                }
            } catch (InterruptedException e) {
                service.shutdownNow();
            }
        }
    }

    public void updateSimulationPathInfo(String threadId, List<BreakSimulationNode> path){
        this.breakSimulationPaths.put(threadId, path);
        System.out.println("ThreadName = "+ threadId + ", PATH = "+ path.toString());
    }

    public void updateConflictInfo(BreakConflictContainer container){
        this.threadsConflicts.add(container);
        System.out.println("CONFLICT::ThreadName = "+ container.threadName + ", PATH = "+ container.toString());
    }

}
