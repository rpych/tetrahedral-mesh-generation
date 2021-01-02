package controller;

import common.LFunction;
import model.*;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import visualization.MatlabVisualizer;

import java.util.*;

public class TransformatorForLayers {
    public ModelGraph graph;
    public Stack<GraphEdge> hangingEdges;
    static final double EPS = 0.00000001;

    public TransformatorForLayers(ModelGraph graph) {
        this.graph = graph;
        hangingEdges = new Stack<GraphEdge>();
    }

    public ModelGraph transform() {
        return transform(this.graph);
    }

    public ModelGraph transform(ModelGraph graph) {
        Optional<FaceNode> face = findFaceToBreak(graph);
        int counter  = 0;
        while(face.isPresent()){
            graph = breakFace(graph, face.get()); //here insert edge to break E on stack and E as currentEdgeVertices
            counter++;
            while( !hangingEdges.empty() ){
                System.out.println("STACK = "+ hangingEdges.peek().getId());
                Optional<FaceNode> faceHN = findFaceWithHangingNode(graph);
                if(!faceHN.isPresent()) {
                    hangingEdges.pop();
                    System.out.println("Stack not empty but hanging node not found");
                    continue;
                }
                processLastHangingNode(graph, faceHN.get());
                graph = addNewFaces(graph);
            }
            graph = createNewInteriorNodes();
            graph = markFacesToBreak(graph);
            face = findFaceToBreak(graph); //face on different layers
            if(!checkAllFacesBelongToInteriorNode(graph)){
                System.err.println("Some FACES do not belong to any interiorNode " + counter);
            }else System.out.println("Faces correctly matched with Interiors " + counter + " ......................................");

            if(isEnoughBreakingAccuracy(graph)) {
                System.out.println("ENOUGH accuracy met");
                break;
            }
            checkFacesConnected(graph);
            checkInteriorNodesMinMaxBreakingRatio(graph);
            //if(counter == 180) break;
        }
        return graph;
    }



    public boolean checkAllFacesBelongToInteriorNode(ModelGraph graph){
        int counter = 0;
        Map<String, Integer> facesInInteriorCount = new HashMap<>();
        Collection<FaceNode> faces = graph.getFaces();
        for(FaceNode face: faces){
            int out = isFaceInAnyInteriorNode(face);
            counter += out;
            if(out == 0) counter += 1;
            facesInInteriorCount.put(face.getId(), out);
            if(out == 0 ){ //|| out > 2
                graph.debugFaces.add(face);
            }
        }
        boolean f = true;
        for(Map.Entry<String, Integer> entry: facesInInteriorCount.entrySet()){
            if(entry.getValue().equals(0) || entry.getValue() > 2){
                f = false;
                System.out.println("APPEARANCE IN INTERIORS = "+entry.getKey() + " :"+ entry.getValue());
            }
        }
        System.out.println("Faces counter = "+counter + ", whereas InteriorNodes count = "+graph.getInteriorNodes().size() + " and " +
                " InteriorNodes*4 = "+ graph.getInteriorNodes().size()*4);
        return f;
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
            else if((Double.compare(v0v2Len, longestEdgeLen) == 0) && graph.isEdgeBetween(longestEdge.getValue0(), longestEdge.getValue1())
                    && !graph.isEdgeBetween(v0, v2)){
                longestEdgeLen = v0v2Len;
                longestEdge = new Pair<>(v0, v2);
            }
        }
        if(Double.compare(v1v2Len, longestEdgeLen) >= 0){
            if(Double.compare(v1v2Len, longestEdgeLen) == 1){
                //longestEdgeLen = v1v2Len;
                longestEdge = new Pair<>(v1, v2);
            }
            else if((Double.compare(v1v2Len, longestEdgeLen) == 0) && graph.isEdgeBetween(longestEdge.getValue0(), longestEdge.getValue1())
                    && !graph.isEdgeBetween(v1, v2)){
                //longestEdgeLen = v1v2Len;
                longestEdge = new Pair<>(v1, v2);
            }
        }
        return longestEdge;
    }

    //InteriorNode part

    public ModelGraph createNewInteriorNodes(){
        graph.clearInteriorNodes();
        return graph.createInteriorNodesForNewlyFoundSubGraphs();
    }

    //checks
    public void checkFacesConnected(ModelGraph graph){
        for(FaceNode face: graph.getFaces()){
            if(!graph.areVertexesLinked(face)){
                System.out.println("Face exists but its vertices are not linked !!!!!");
            }
        }
    }

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
        return false;
    }

    public int isFaceInAnyInteriorNode(FaceNode face){
        int count = 0;
        Collection<InteriorNode> interiorNodes = graph.getInteriorNodes();
        for(InteriorNode node: interiorNodes){
            if(node.containsFace(face)) count++;
        }
        return count;
    }

    public void checkInteriorNodesMinMaxBreakingRatio(ModelGraph graph){
        double min_dist = 1000.0, max_dist = 0.0;
        for(InteriorNode node: graph.getInteriorNodes()){
            Vertex hZVertex = findVertexWithHighestZCoord(node);
            Triplet<Vertex, Vertex, Vertex> basisTriangle = getBasisTriangleFromInteriorNode(node, hZVertex);
            FaceNode basisFace = graph.getFace(basisTriangle);
            if(basisFace != null){
                double height = Math.abs(hZVertex.getZCoordinate() - basisFace.getZCoordinate());
                if(height > max_dist) max_dist = height;
                if(height < min_dist) min_dist = height;
            }
        }
        double ratio = max_dist/min_dist;
        System.out.println("Current breaking ratio = "+ratio);
    }

    public Vertex findVertexWithHighestZCoord(InteriorNode node){
        if(node.getQuartet().getValue0().getZCoordinate() >= node.getQuartet().getValue1().getZCoordinate() &&
                node.getQuartet().getValue0().getZCoordinate() >= node.getQuartet().getValue2().getZCoordinate() &&
                node.getQuartet().getValue0().getZCoordinate() >= node.getQuartet().getValue3().getZCoordinate()){
            return node.getQuartet().getValue0();
        }
        else if(node.getQuartet().getValue1().getZCoordinate() >= node.getQuartet().getValue0().getZCoordinate() &&
                node.getQuartet().getValue1().getZCoordinate() >= node.getQuartet().getValue2().getZCoordinate() &&
                node.getQuartet().getValue1().getZCoordinate() >= node.getQuartet().getValue3().getZCoordinate()){
            return node.getQuartet().getValue1();
        }
        else if(node.getQuartet().getValue2().getZCoordinate() >= node.getQuartet().getValue0().getZCoordinate() &&
                node.getQuartet().getValue2().getZCoordinate() >= node.getQuartet().getValue1().getZCoordinate() &&
                node.getQuartet().getValue2().getZCoordinate() >= node.getQuartet().getValue3().getZCoordinate()){
            return node.getQuartet().getValue2();
        }
        return node.getQuartet().getValue3();
    }

    public Triplet<Vertex, Vertex, Vertex> getBasisTriangleFromInteriorNode(InteriorNode node, Vertex topVertex){
        if(topVertex.getId().equals(node.getQuartet().getValue0().getId())){
            return new Triplet<>(node.getQuartet().getValue1(), node.getQuartet().getValue2(), node.getQuartet().getValue3());
        }
        else if(topVertex.getId().equals(node.getQuartet().getValue1().getId())){
            return new Triplet<>(node.getQuartet().getValue0(), node.getQuartet().getValue2(), node.getQuartet().getValue3());
        }
        else if(topVertex.getId().equals(node.getQuartet().getValue2().getId())){
            return new Triplet<>(node.getQuartet().getValue0(), node.getQuartet().getValue1(), node.getQuartet().getValue3());
        }
        return new Triplet<>(node.getQuartet().getValue0(), node.getQuartet().getValue1(), node.getQuartet().getValue2());
    }

}
