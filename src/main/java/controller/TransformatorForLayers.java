package controller;

import common.LFunction;
import model.*;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Stack;

public class TransformatorForLayers {
    public ModelGraph graph;
    public Stack<GraphEdge> hangingEdges;
    public GraphEdge currentEdge;
    static final double EPS = 0.00001;

    public TransformatorForLayers(ModelGraph graph) {
        this.graph = graph;
        hangingEdges = new Stack<GraphEdge>();
    }

    public ModelGraph transform() {
        return transform(this.graph);
    }

    /*public ModelGraph transform(ModelGraph graph) {
        FaceNode face = findFaceToBreak(graph);
        while(face != null) {
            graph = breakFace(graph, face);
            graph = addNewFaces(graph);
            graph = markFacesToBreak(graph);
            face = findFaceToBreak(graph);
        }

        return graph;
    }*/

    public ModelGraph transform(ModelGraph graph) {
        Optional<FaceNode> face = findFaceToBreak(graph);
        int counter  = 0;
        while(face.isPresent() ){
            graph = breakFace(graph, face.get()); //here insert edge to break E on stack and E as currentEdgeVertices
            counter++;

            do {
                System.out.println("STACK = "+ hangingEdges.peek().getId().toString());
                Optional<FaceNode> faceHN = findFaceWithHangingNode(graph);
                if(!faceHN.isPresent()) break;
                processLastHangingNode(graph, faceHN.get());
                graph = addNewFaces(graph);


            }while( !hangingEdges.empty() );
            /*graph = createNewInteriorNodes();*/
            graph = markFacesToBreak(graph);
            face = findFaceToBreak(graph); //face on different layers
            if(counter == 10) break;
        }
        return graph;
    }

    public Optional<FaceNode> findFaceToBreak(ModelGraph graph){
        for(FaceNode face: graph.getFaces()){
            if(face.isR()) return Optional.of(face);
        }
        return Optional.empty();
    }

    public Optional<FaceNode> findFaceWithHangingNode(ModelGraph graph){
        Vertex v0 = (Vertex)hangingEdges.peek().getEdgeNodes().getValue0(), v1 = (Vertex)hangingEdges.peek().getEdgeNodes().getValue1();
        for(FaceNode face: graph.getFaces()){
            if(face.containsVertices(v0, v1) && !graph.areVertexesLinked(face)) return Optional.of(face);
        }
        return Optional.empty();
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
        else{
            hangingEdges.push(longestEdge);
            currentEdge = longestEdge;
        }
        return graph;
    }

    public boolean isLastHNRefined(GraphEdge longestEdge){
        return (hangingEdges.peek().getEdgeNodes().getValue0().equals(longestEdge.getEdgeNodes().getValue0()) ||
                hangingEdges.peek().getEdgeNodes().getValue0().equals(longestEdge.getEdgeNodes().getValue1())) &&
                (hangingEdges.peek().getEdgeNodes().getValue1().equals(longestEdge.getEdgeNodes().getValue0()) ||
                hangingEdges.peek().getEdgeNodes().getValue1().equals(longestEdge.getEdgeNodes().getValue1()));
    }

    public boolean existsFaceWithEdge(ModelGraph graph, GraphEdge edge){
        return getFacesWithEdge(graph, edge).size() > 0;
    }

    public ModelGraph breakFace(ModelGraph graph, FaceNode face){
        //Vertex v0 = face.getTriangle().getValue0(), v1 = face.getTriangle().getValue1(), v2 = face.getTriangle().getValue2();
        Pair<Vertex, Vertex> longEdgeVert = getLongestEdgeVerticesFromFace(face);
        GraphEdge longestEdge = graph.getEdgeNotOptional(longEdgeVert.getValue0(), longEdgeVert.getValue1());
        Vertex vOpposite = getVertexForNewEdge(face, longEdgeVert);
        graph = performBreaking(graph, vOpposite, longestEdge);

        //Collection<FaceNode> facesWithLongestEdge = getFacesWithEdge(graph, longestEdge);
       // for(int i=0;i<facesWithLongestEdge.size();++i)
        System.out.println("longestEdge = "+longestEdge.getId() + ", OPPOSITE = "+ vOpposite.getId());
        hangingEdges.push(longestEdge);

        currentEdge = longestEdge;
        return graph;
    }

    public ModelGraph performBreaking(ModelGraph graph, Vertex opposite, GraphEdge edge){
        Vertex newVertex = graph.insertVertexAutoNamedOrGet(edge.getMiddlePointCoordinates());
        graph.insertEdgeAutoNamedOrGet(opposite, newVertex, false);

        graph.deleteEdge(edge.getId());
        graph.insertEdgeAutoNamedOrGet(edge.getEdgeNodes().getValue0(), newVertex, true);
        graph.insertEdgeAutoNamedOrGet(edge.getEdgeNodes().getValue1(), newVertex, true);

        Triplet<Vertex, Vertex, Vertex> triangle = new Triplet<>(opposite, (Vertex)edge.getEdgeNodes().getValue0(), (Vertex)edge.getEdgeNodes().getValue1());
        graph = removeFace(graph, triangle);
        graph.insertFaceAutoNamed(opposite, newVertex, (Vertex)edge.getEdgeNodes().getValue0());
        graph.insertFaceAutoNamed(opposite, newVertex, (Vertex)edge.getEdgeNodes().getValue1());

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
        for(FaceNode faceNode : graph.getFaces()) {
            if(checkEdgesOnLayersBorder(graph, faceNode)) {
                faceNode.setR(true);
                break;
            }
        }
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
        //System.out.println("CANDIDATES = " + edges);
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

        if(v0v2Len >= longestEdgeLen){
            if(v0v2Len > longestEdgeLen){
                longestEdgeLen = v0v2Len;
                longestEdge = new Pair<>(v0, v2);
            }
            else if((Math.abs(v0v2Len-longestEdgeLen) < EPS) && graph.isEdgeBetween(longestEdge.getValue0(), longestEdge.getValue1())
                    && !graph.isEdgeBetween(v0, v2)){
                longestEdgeLen = v0v2Len;
                longestEdge = new Pair<>(v0, v2);
            }
        }
        if(v1v2Len >= longestEdgeLen){
            if(v1v2Len > longestEdgeLen){
                longestEdgeLen = v1v2Len;
                longestEdge = new Pair<>(v1, v2);
            }
            else if((Math.abs(v1v2Len-longestEdgeLen) < EPS) && graph.isEdgeBetween(longestEdge.getValue0(), longestEdge.getValue1())
                    && !graph.isEdgeBetween(v1, v2)){
                //longestEdgeLen = v1v2Len;
                longestEdge = new Pair<>(v1, v2);
            }
        }
        return longestEdge;
    }

    //InteriorNode part

    public ModelGraph createNewInteriorNodes(){
        //String initialIntNodeName = graph.getInteriorNodes().iterator().next().getId(); //first and only entry in Map so far
        graph.clearInteriorNodes();
        return graph.createInteriorNodesForNewlyFoundSubGraphs();
    }

}
