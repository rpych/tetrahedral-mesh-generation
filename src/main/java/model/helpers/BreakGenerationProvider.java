package model.helpers;

import model.*;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.*;

import static common.Utils.isEdgeBetween;

public class BreakGenerationProvider {

    protected ModelGraph graph;
    //private Map<String, List<BreakSimulationNode>> breakPaths;
    //private List<String> dependencyList; //list of superior threads to current one
    //private ThreadsDependencyGraph dependencyGraph;
    //private Map<String, Boolean> threadsExecutionFinished;
    protected Stack<GraphEdge> hangingEdges;
    //private Map<String, String> directSuperiorThreadNames;
    //private ThreadLockContainer lockContainer;
    private FaceNode startFace;
    //private volatile TransformatorForLayers observer;
    private boolean visited = false;
    protected int step;


    public BreakGenerationProvider(ModelGraph graph, FaceNode startFace){
        this.graph = graph;
        this.startFace = startFace;
        this.hangingEdges = new Stack<GraphEdge>();
    }

    public ModelGraph transform() {
        return transform(graph);
    }

    public ModelGraph transform(ModelGraph graph) {
        step = 1;
        Optional<FaceNode> faceOpt = graph.getFace(startFace.getId());
        FaceNode face = null;
        if(faceOpt.isPresent())
            face = faceOpt.get();
        System.out.println("GENERATOR_START_FACE::"+ face.getId()+ " ,STACK_SIZE = "+hangingEdges.size());
        graph = breakFace(graph, face);
        //TransformatorForLayers.meshLogger.log("GENERATOR_START_FACE::"+ face.getId()+ " ,STACK_SIZE = "+hangingEdges.size());
        while( !hangingEdges.empty() ){

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
        return graph;
    }


    public ModelGraph processLastHangingNode(ModelGraph graph, FaceNode face){
        Pair<Vertex, Vertex> longEdgeVert = getLongestEdgeVerticesFromFace(face);
        GraphEdge longestEdge = graph.insertEdgeAutoNamedOrGet(longEdgeVert.getValue0(), longEdgeVert.getValue1(), false);
        Vertex vOpposite = getVertexForNewEdge(face, longEdgeVert);
        graph = performBreaking(graph, vOpposite, longestEdge);
        System.out.println("GENERATOR::HN::longestEdge = "+longestEdge.getId() + ", OPPOSITE = "+ vOpposite.getId() + ", STEP = "+step);

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

        System.out.println("GENERATOR::longestEdge = "+longestEdge.getId() + ", OPPOSITE = "+ vOpposite.getId());
        //TransformatorForLayers.meshLogger.log("GENERATOR::longestEdge = "+longestEdge.getId() + ", OPPOSITE = "+ vOpposite.getId());
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

    public Collection<FaceNode> getFacesWithEdge(ModelGraph graph, Vertex v0, Vertex v1){
        //Vertex v0 = (Vertex)edge.getEdgeNodes().getValue0(), v1 = (Vertex)edge.getEdgeNodes().getValue1();
        Collection<FaceNode> facesWithBrokenEdge = new LinkedList<>();
        for(FaceNode face: graph.getFaces()){
            if(face.containsVertices(v0, v1)){
                facesWithBrokenEdge.add(face);
            }
        }
        return facesWithBrokenEdge;
    }

    protected ModelGraph addNewFaces(ModelGraph graph) {
        Collection<GraphEdge> ebv = graph.getEdgesBetweenVertices();
        for(GraphEdge edge : ebv) {
            Pair<Vertex, Vertex> edgeVertices = edge.getVertices();
            Collection<Vertex> cv = graph.getCommonVertices(edgeVertices.getValue0(), edgeVertices.getValue1());
            for(Vertex v : cv) {
                if(!graph.hasFaceNode(edgeVertices.getValue0(), edgeVertices.getValue1(), v)) {
                    FaceNode f = graph.insertFaceAutoNamed(edgeVertices.getValue0(), edgeVertices.getValue1(), v);
                    System.out.println(step + " GENERATOR::ADD_NEW_FACE = "+ f.getId());
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
