package controller;

import common.LFunction;
import model.*;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Triplet;
import visualization.MatlabVisualizer;

import java.util.*;

public class TransformatorForLayers {
    public ModelGraph graph;
    public Stack<GraphEdge> hangingEdges;
    public GraphEdge currentEdge;
    //public boolean flag= false;
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
        while(face.isPresent() ){
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
            //graph = addMissingEdgesBetweenFaces(graph);
            graph = createNewInteriorNodes();
            graph = markFacesToBreak(graph);
            face = findFaceToBreak(graph); //face on different layers
            if(!checkAllFacesBelongToInteriorNode(graph)){
                System.err.println("Some FACES do not belong to any interiorNode " + counter);
            }else{
                System.out.println("Faces correctly matched with Interiors " + counter + " ......................................");
            }
            if(isEnoughBreakingAccuracy(graph)) {
                System.out.println("ENOUGH accuracy met");
                break;
            }
            checkFacesConnected(graph, counter);
            if(counter == 180) break;
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

    public void checkFacesConnected(ModelGraph graph, int counter){
        for(FaceNode face: graph.getFaces()){
            if(!graph.areVertexesLinked(face)){
                System.out.println("Face exists but its vertices are not linked !!!!!");
            }
        }
    }

    public boolean isEnoughBreakingAccuracy(ModelGraph graph){
        int numOfReqIntNodesBelowThresh = 50, numOfIntNodesBelowThreshIntermed = 0, numOfIntNodesBelowThreshLow = 0;
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
            //Quartet<Vertex, Vertex, Vertex, Vertex> vertices = node.getQuartet();
            if(node.containsFace(face)) count++;
        }
        return count;
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
            currentEdge = longestEdge;
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

        currentEdge = longestEdge;
        return graph;
    }

    public ModelGraph performBreaking(ModelGraph graph, Vertex opposite, GraphEdge edge){
        //try {
            Vertex newVertex = graph.insertVertexAutoNamedOrGet(edge.getMiddlePointCoordinates());
            graph.insertEdgeAutoNamedOrGet(opposite, newVertex, false);

            graph.deleteEdge(edge.getId());
            graph.insertEdgeAutoNamedOrGet(edge.getEdgeNodes().getValue0(), newVertex, true);
            graph.insertEdgeAutoNamedOrGet(edge.getEdgeNodes().getValue1(), newVertex, true);

            Triplet<Vertex, Vertex, Vertex> triangle = new Triplet<>(opposite, (Vertex) edge.getEdgeNodes().getValue0(), (Vertex) edge.getEdgeNodes().getValue1());
            graph = removeFace(graph, triangle);
            graph.insertFaceAutoNamed(opposite, newVertex, (Vertex) edge.getEdgeNodes().getValue0());
            graph.insertFaceAutoNamed(opposite, newVertex, (Vertex) edge.getEdgeNodes().getValue1());


        /*}catch(org.graphstream.graph.IdAlreadyInUseException e){
            System.out.println("RPY::ERROR occured");
            MatlabVisualizer matlabVisualizer2 = new MatlabVisualizer(graph, "visLay1");
            matlabVisualizer2.saveCode();
        }*/
        return graph;
    }

    public ModelGraph addMissingEdgesBetweenFaces(ModelGraph graph){
        Collection<FaceNode> faces = new LinkedList<>(graph.getFaces());
        for(FaceNode face: faces){
            Optional<FaceNode> nearestCongruentFace = graph.getNearestCongruentFace(face, faces);

            if(nearestCongruentFace.isPresent()){
                Optional<Pair<Vertex, Vertex>> uncommonVertices = face.getUncommonVerticesIfCongruent(nearestCongruentFace.get());
                Optional<Pair<Vertex, Vertex>> commonVertices = face.getVerticesFromCongruentEdge(nearestCongruentFace.get());
                if(!commonVertices.isPresent() || !uncommonVertices.isPresent()) continue;
                boolean newEdgeDoesNotSliceCommonEdge = edgeDoesNotSliceSecondEdge(uncommonVertices, commonVertices);
                if( newEdgeDoesNotSliceCommonEdge && !graph.getEdgeBetweenNodes(uncommonVertices.get().getValue0(), uncommonVertices.get().getValue1()).isPresent()
                    && (Math.abs(uncommonVertices.get().getValue0().getZCoordinate() - uncommonVertices.get().getValue1().getZCoordinate()) < EPS  )
                    && !graph.hasFaceNode(uncommonVertices.get().getValue0(), uncommonVertices.get().getValue1(), commonVertices.get().getValue0())
                    && !graph.hasFaceNode(uncommonVertices.get().getValue0(), uncommonVertices.get().getValue1(), commonVertices.get().getValue1()) )  {
                    System.out.println("Missing edge added = " + uncommonVertices.get().getValue0() + "-->" + uncommonVertices.get().getValue1() +
                            ", commonVert = "+ commonVertices.get().getValue0() + "-->>"+ commonVertices.get().getValue1());
                    graph.insertEdgeAutoNamedOrGet(uncommonVertices.get().getValue0(), uncommonVertices.get().getValue1(), false);
                    //flag = true;
                }
            }
            graph = addNewFaces(graph);
        }


        return graph;
    }

    public boolean edgeDoesNotSliceSecondEdge(Optional<Pair<Vertex, Vertex>> uncommonVertices,
                                              Optional<Pair<Vertex, Vertex>> commonVertices){
        return !((Math.abs(uncommonVertices.get().getValue0().getXCoordinate()-
                uncommonVertices.get().getValue1().getXCoordinate()) < EPS)
                && (Math.abs(Coordinates.middlePoint(commonVertices.get().getValue0().getCoordinates(), commonVertices.get().getValue1().getCoordinates())
                .getZ()-uncommonVertices.get().getValue0().getZCoordinate()) < EPS));
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
                    System.out.println("RPY FACE ADDED = " + edgeVertices.getValue0().getId() + ", "+ edgeVertices.getValue1().getId() + ", "+ v.getId());
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

}
