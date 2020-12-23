package controller;

import model.*;
import common.LFunction;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class TransformatorForLayers implements ITransformator {
    public ModelGraph graph;


    public TransformatorForLayers(ModelGraph graph) {
        this.graph = graph;
    }

    public ModelGraph transform() {
        return transform(this.graph);
    }

    public ModelGraph transform(ModelGraph graph) {

        Optional<FaceNode> face = findFaceToBreak(graph);
        while(face.isPresent()){
            graph = breakFace(graph, face.get());
            graph = addNewFaces(graph);
            graph = markFacesToBreak(graph);
            face = findFaceToBreak(graph);
        }
        graph = addMissingFacesOnLayersBorder(graph);
        /*graph = createNewInteriorNodes();*/
        return graph;
    }

    public Optional<FaceNode> findFaceToBreak(ModelGraph graph){
        for(FaceNode face: graph.getFaces()){
            if(face.isR()) return Optional.of(face);
        }
        return Optional.empty();
    }

    public ModelGraph breakFace(ModelGraph graph, FaceNode face){
        List<GraphEdge> edgesOnBorder = getEdgesOnLayersBorder(graph, face);
        GraphEdge edgeToBreak = getEdgeToBreak(graph, edgesOnBorder);
        Coordinates biggerZCoord = (edgeToBreak.getEdgeNodes().getValue0().getZCoordinate() >
                                    edgeToBreak.getEdgeNodes().getValue1().getZCoordinate()) ?
                                    edgeToBreak.getEdgeNodes().getValue0().getCoordinates() :
                                    edgeToBreak.getEdgeNodes().getValue1().getCoordinates();
        System.out.println(":: edgeToBreak = "+ edgeToBreak.getId());

        double fValue = LFunction.F(biggerZCoord); // Z coord
        if(fValue < 0){ System.err.println("Error: FValue < 0.0 "); }
        Optional<Coordinates> breakPoint = LFunction.getBreakPoint(edgeToBreak, fValue);
        Pair<Vertex, Vertex> edgeToBreakVertices = new Pair<Vertex, Vertex>((Vertex)edgeToBreak.getEdgeNodes().getValue0(), (Vertex)edgeToBreak.getEdgeNodes().getValue1());
        Vertex vOpposite = getVertexForNewEdge(face, edgeToBreakVertices);
        graph = performBreaking(graph, vOpposite, edgeToBreak, breakPoint);
        return graph;
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



    public ModelGraph addMissingFacesOnLayersBorder(ModelGraph graph){
        for(int i=0;i<LFunction.layersUpCoords.length;++i) {
            double upperLayerBoundary = LFunction.layersUpCoords[i];
            Collection<FaceNode> folb = graph.getFacesPartiallyLaidOnLayerBorder(upperLayerBoundary);
            for(FaceNode face: folb){
                Optional<FaceNode> nearestCongruentFace = graph.getNearestCongruentFace(face, folb);

                if(nearestCongruentFace.isPresent()){
                    Optional<Pair<Vertex, Vertex>> uncommonVertices = face.getUncommonVerticesIfCongruent(nearestCongruentFace.get());
                    if(uncommonVertices.isPresent() && !graph.getEdgeBetweenNodes(uncommonVertices.get().getValue0(), uncommonVertices.get().getValue1()).isPresent()) {
                        System.out.println("Missing edge added = " + uncommonVertices.get().getValue0() + "-->" + uncommonVertices.get().getValue1());
                        graph.insertEdgeAutoNamedOrGet(uncommonVertices.get().getValue0(), uncommonVertices.get().getValue1(), false);
                    }
                }
            }
        }
        graph = addNewFaces(graph);
        return graph;
    }


    public ModelGraph markFacesToBreak(ModelGraph graph){
        boolean faceFoundForRefinement = false;
        for(FaceNode faceNode : graph.getFaces()) {
            if(!graph.areVertexesLinked(faceNode)) {
                faceNode.setR(true);
                faceFoundForRefinement = true;
                break;
            }
        }
        if( !faceFoundForRefinement){ //found new face with different layers on it
            for(FaceNode faceNode : graph.getFaces()) {
                List<GraphEdge> edgesOnBorder = getEdgesOnLayersBorder(graph, faceNode);
                if(edgesOnBorder.size() > 0){
                    faceNode.setR(true);
                    break;
                }
            }
        }
        return graph;
    }

    public List<GraphEdge> getEdgesOnLayersBorder(ModelGraph graph, FaceNode face){
        List<GraphEdge> edges = new LinkedList<>();
        Triplet<Vertex, Vertex, Vertex> triangle = face.getTriangle();

        if(LFunction.areDifferentLayers( triangle.getValue0().getCoordinates(),
                                         triangle.getValue1().getCoordinates() ))
        {
            GraphEdge edge1 = new GraphEdge(null, new Pair<>(triangle.getValue0(), triangle.getValue1()), false);
            edges.add(edge1);
        }
        if(LFunction.areDifferentLayers( triangle.getValue0().getCoordinates(),
                                         triangle.getValue2().getCoordinates()) )
        {
            GraphEdge edge2 = new GraphEdge(null, new Pair<>(triangle.getValue0(), triangle.getValue2()), false);
            edges.add(edge2);
        }
        if(LFunction.areDifferentLayers( triangle.getValue1().getCoordinates(),
                                         triangle.getValue2().getCoordinates()) )
        {
            GraphEdge edge3 = new GraphEdge(null, new Pair<>(triangle.getValue1(), triangle.getValue2()), false);
            edges.add(edge3);
        }
        //System.out.println("CANDIDATES = " + edges);
        return edges;
    }

    public GraphEdge getEdgeToBreak(ModelGraph graph, List<GraphEdge> edgesOnBorder){
        GraphEdge edgeToBreak = null;
        double longestEdgeLen = 0.0;
        for(GraphEdge edge: edgesOnBorder){
            double edgeLen = Coordinates.distance(edge.getEdgeNodes().getValue0().getCoordinates(),
                    edge.getEdgeNodes().getValue1().getCoordinates());
            if(edgeLen > longestEdgeLen){
                longestEdgeLen = edgeLen;
                edgeToBreak = edge;
            }
        }
        //edge already broken so we can use this one
        if( !graph.isEdgeBetween(edgeToBreak.getEdgeNodes().getValue0(), edgeToBreak.getEdgeNodes().getValue1()) ){
            edgeToBreak = graph.insertEdgeAutoNamed(edgeToBreak.getEdgeNodes().getValue0(), edgeToBreak.getEdgeNodes().getValue1(), false);
            //System.out.println("::add broken edge " + edgeToBreak.getId());
            return edgeToBreak;
        }
        else{
            Optional<GraphEdge> existingEdge = graph.getEdge((Vertex)edgeToBreak.getEdgeNodes().getValue0(), (Vertex)edgeToBreak.getEdgeNodes().getValue1());
            if(existingEdge.isPresent())
                edgeToBreak = existingEdge.get();
            //System.out.println("::existing edge = " + edgeToBreak.getId());
        }
        //another edge with the same length as edgeToBreak might have been already broken in another face
        double eps = 0.0000001;
        for(GraphEdge edge: edgesOnBorder){
            if(!edgeToBreak.equals(edge) && (Math.abs(edge.getLength() - edgeToBreak.getLength() ) < eps)
                    && !graph.isEdgeBetween(edge.getEdgeNodes().getValue0(), edge.getEdgeNodes().getValue1()))
            {
                edgeToBreak = edge;
                edgeToBreak = graph.insertEdgeAutoNamed(edgeToBreak.getEdgeNodes().getValue0(), edgeToBreak.getEdgeNodes().getValue1(), false);
                //System.out.println(" ::hanging node edge = " + edgeToBreak.getId());
            }
        }
        return edgeToBreak;
    }

    //from previuos version of algorithm
    public ModelGraph performBreaking(ModelGraph modelGraph, Vertex vertex, GraphEdge edge, Optional<Coordinates> breakPoint) {
        Vertex newVertex = null;
        newVertex = modelGraph.insertVertexAutoNamedOptional(edge, breakPoint);
        System.out.println(" NEWVERTEX = "+ newVertex.getId() + ", OPPVertex = "+ vertex.getId());
        modelGraph.insertEdgeAutoNamedOrGet(vertex, newVertex, false);

        modelGraph.deleteEdge(edge.getId());
        modelGraph.insertEdgeAutoNamedOrGet(edge.getEdgeNodes().getValue0(), newVertex, true);
        modelGraph.insertEdgeAutoNamedOrGet(edge.getEdgeNodes().getValue1(), newVertex, true);


        Triplet<Vertex, Vertex, Vertex> triangle = new Triplet<>(vertex, (Vertex)edge.getEdgeNodes().getValue0(), (Vertex)edge.getEdgeNodes().getValue1());
        modelGraph = removeFace(modelGraph, triangle);
        modelGraph.insertFaceAutoNamed(vertex, newVertex, (Vertex)edge.getEdgeNodes().getValue0());
        modelGraph.insertFaceAutoNamed(vertex, newVertex, (Vertex)edge.getEdgeNodes().getValue1());

        return modelGraph;
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

    //InteriorNode part

    public ModelGraph createNewInteriorNodes(){
        String initialIntNodeName = graph.getInteriorNodes().iterator().next().getId(); //first and only entry in Map so far
        graph.removeInteriorNode(initialIntNodeName);
        return graph.createInteriorNodesForNewlyFoundSubGraphs();
    }

}
