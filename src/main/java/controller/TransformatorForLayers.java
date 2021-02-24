package controller;

import common.LFunction;
import model.*;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Triplet;
import visualization.MatlabVisualizer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class TransformatorForLayers {
    public ModelGraph graph;
    public Stack<GraphEdge> hangingEdges;
    public double breakRatio;
    public Integer counter = 0;
    FileWriter edgesRatioFileWriter;;
    FileWriter anglesFileWriter;

    public TransformatorForLayers(ModelGraph graph) {
        this.graph = graph;
        hangingEdges = new Stack<GraphEdge>();
        createFileWriters();
    }

    public ModelGraph transform() {
        return transform(this.graph);
    }

    public ModelGraph transform(ModelGraph graph) {
        Optional<FaceNode> face = findFaceToBreak(graph);
        counter  = 0;
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
                graph = processLastHangingNode(graph, faceHN.get());
                graph = addNewFaces(graph);
            }
            graph = createNewInteriorNodes();
            graph = markFacesToBreak(graph);
            face = findFaceToBreak(graph); //face on different layers

            //breaking ratio checking
            checkAdaptationProperties(graph);

            if(counter % 20 == 0) {
                if (!checkAllFacesBelongToInteriorNode(graph)) {
                    System.err.println("Some FACES do not belong to any interiorNode " + counter);
                } else
                    System.out.println("Faces correctly matched with Interiors " + counter + " ......................................");
                checkInteriorNodesMinMaxBreakingRatio(graph);
                System.out.println("FACES = "+ graph.getFaces().size() + ", INTERIORS = "+graph.getInteriorNodes().size() +
                        ", VERTICES = "+ graph.getVertices().size() + ", EDGES = "+(graph.getEdges().size()-graph.falseEdgesCounter) +
                        ", false edges = "+ graph.falseEdgesCounter);
                MatlabVisualizer matlabVisualizer = new MatlabVisualizer(graph, "visLayCuboid18_01_60_" + counter);
                matlabVisualizer.saveCode();
            }
            if (isEnoughBreakingAccuracy(graph)) {
                System.out.println("ENOUGH accuracy met");
                if (!checkAllFacesBelongToInteriorNode(graph)) {
                    System.err.println("Some FACES do not belong to any interiorNode " + counter);
                } else
                    System.out.println("Faces correctly matched with Interiors " + counter + " ......................................");
                System.out.println("FACES = "+ graph.getFaces().size() + ", INTERIORS = "+graph.getInteriorNodes().size() +
                        ", VERTICES = "+ graph.getVertices().size() + ", EDGES = "+ (graph.getEdges().size() - graph.falseEdgesCounter) +
                        ", false edges = "+ graph.falseEdgesCounter);
                MatlabVisualizer matlabVisualizer = new MatlabVisualizer(graph, "visLayCuboid18_01_60_" + counter);
                matlabVisualizer.saveCode();
                closeFiles();
                break;
            }
            //checkFacesConnected(graph);


            //if(counter == 180) break;
        }
        return graph;
    }

    public void createFileWriters() {
        try {
            edgesRatioFileWriter = new FileWriter("edgesRatio60.txt");
            anglesFileWriter = new FileWriter("angles60.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToEdgeRatioFileWriter(String data){
        try {
            edgesRatioFileWriter.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToAnglesFileWriter(String data){
        try {
            anglesFileWriter.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeFiles(){
        try {
            edgesRatioFileWriter.close();
            anglesFileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void checkAdaptationProperties(ModelGraph graph){
        checkInteriorNodesEdgesLenRatio(graph);
        checkAnglesBetweenEdgesInInteriorNode(graph);
    }


    public void checkInteriorNodesEdgesLenRatio(ModelGraph graph){
        List<InteriorNode> interiorNodes = graph.interiorNodesNew;
        for(InteriorNode node: interiorNodes){
            Map<GraphEdge, Double> edges = new HashMap<>();
            LinkedHashMap<GraphEdge, Double> sortedMap = new LinkedHashMap<>();
            Quartet<Vertex, Vertex, Vertex, Vertex> intNodeVertices = node.getQuartet();
            GraphEdge e1 = graph.getEdgeBetweenNodes(intNodeVertices.getValue0(), intNodeVertices.getValue1()).get();
            edges.put(e1, Coordinates.distance(e1.getEdgeNodes().getValue0().getCoordinates(),
                    e1.getEdgeNodes().getValue1().getCoordinates()));

            GraphEdge e2 = graph.getEdgeBetweenNodes(intNodeVertices.getValue0(), intNodeVertices.getValue2()).get();
            edges.put(e2, Coordinates.distance(e2.getEdgeNodes().getValue0().getCoordinates(),
                    e2.getEdgeNodes().getValue1().getCoordinates()));

            GraphEdge e3 = graph.getEdgeBetweenNodes(intNodeVertices.getValue0(), intNodeVertices.getValue3()).get();
            edges.put(e3, Coordinates.distance(e3.getEdgeNodes().getValue0().getCoordinates(),
                    e3.getEdgeNodes().getValue1().getCoordinates()));

            GraphEdge e4 = graph.getEdgeBetweenNodes(intNodeVertices.getValue1(), intNodeVertices.getValue2()).get();
            edges.put(e4, Coordinates.distance(e4.getEdgeNodes().getValue0().getCoordinates(),
                    e4.getEdgeNodes().getValue1().getCoordinates()));

            GraphEdge e5 = graph.getEdgeBetweenNodes(intNodeVertices.getValue1(), intNodeVertices.getValue3()).get();
            edges.put(e5, Coordinates.distance(e5.getEdgeNodes().getValue0().getCoordinates(),
                    e5.getEdgeNodes().getValue1().getCoordinates()));

            GraphEdge e6 = graph.getEdgeBetweenNodes(intNodeVertices.getValue2(), intNodeVertices.getValue3()).get();
            edges.put(e6, Coordinates.distance(e6.getEdgeNodes().getValue0().getCoordinates(),
                    e6.getEdgeNodes().getValue1().getCoordinates()));

            edges.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));

            Double ovsum = 0.0,  threeLongestSum = 0.0;
            int c = 0;
            for(Double len: sortedMap.values()){
                ovsum += len;
                if(c<3){
                    threeLongestSum += len;
                }
                c++;
                //System.out.println("Sorted edges "+node.getId() +", len = "+ len);
            }
            double ratio = (threeLongestSum/ovsum);
            writeToEdgeRatioFileWriter(counter+";"+node.getId()+";"+ratio+"\n");
            System.out.println("Edge Ratio = "+ ratio);
        }
    }

    public void checkAnglesBetweenEdgesInInteriorNode(ModelGraph graph){
        List<InteriorNode> interiorNodes = graph.interiorNodesNew;
        for(InteriorNode node: interiorNodes){
            List<Triplet<Vertex, Vertex, Vertex>> facesVertices = new LinkedList<>();
            Quartet<Vertex, Vertex, Vertex, Vertex> quartet = node.getQuartet();
            facesVertices.add(new Triplet<>(quartet.getValue0(), quartet.getValue1(), quartet.getValue2()));
            facesVertices.add(new Triplet<>(quartet.getValue0(), quartet.getValue1(), quartet.getValue3()));
            facesVertices.add(new Triplet<>(quartet.getValue0(), quartet.getValue2(), quartet.getValue3()));
            facesVertices.add(new Triplet<>(quartet.getValue1(), quartet.getValue2(), quartet.getValue3()));
            Double minAngle = 181.0;
            for(Triplet<Vertex, Vertex, Vertex> faceVert: facesVertices){
                Double angle = getMinAngleBetweenFaceEdges(faceVert);
                if(angle < minAngle) minAngle = angle;
            }
            writeToAnglesFileWriter(counter+";"+node.getId()+";"+minAngle+"\n");
            System.out.println("Min angle in interiorNode = "+ minAngle);

        }
    }

    public Double getMinAngleBetweenFaceEdges(Triplet<Vertex, Vertex, Vertex> faceVert){

        Double angle1 = calcAngle(faceVert.getValue0(), faceVert.getValue1(), faceVert.getValue2());
        Double angle2 = calcAngle(faceVert.getValue1(), faceVert.getValue0(), faceVert.getValue2());
        Double angle3 = calcAngle(faceVert.getValue2(), faceVert.getValue0(), faceVert.getValue1());

        //System.out.println("Angle1 = "+angle1 + ", angle2 = "+ angle2 + ", angle3 = "+ angle3);

        return Math.min(angle1, Math.min(angle2, angle3));
    }

    public Double calcAngle(Vertex commonVert, Vertex a, Vertex b){
        Double deltaAX = (a.getXCoordinate() - commonVert.getXCoordinate());
        Double deltaBX = (b.getXCoordinate() - commonVert.getXCoordinate());

        Double deltaAY = (a.getYCoordinate() - commonVert.getYCoordinate());
        Double deltaBY = (b.getYCoordinate() - commonVert.getYCoordinate());

        Double deltaAZ = (a.getZCoordinate() - commonVert.getZCoordinate());
        Double deltaBZ = (b.getZCoordinate() - commonVert.getZCoordinate());

        Double aLen = Coordinates.distance(a.getCoordinates(), commonVert.getCoordinates());
        Double bLen = Coordinates.distance(b.getCoordinates(), commonVert.getCoordinates());

        double scalarProduct = (deltaAX * deltaBX) + (deltaAY * deltaBY) + (deltaAZ * deltaBZ);
        double cosine = scalarProduct / (aLen * bLen);

        double angle = Math.acos(cosine);
        double angleInDegrees = (angle/Math.PI) * 180;

        //System.out.println("AngleRad = "+angle + ", angleInDegrees = "+ angleInDegrees);
        return  angleInDegrees;
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
        graph.setOldInteriorNodes(graph); //ugly code
        graph.clearInteriorNodes();
        //System.out.println("OLD size = "+ graph.interiorNodesOld.size() + ", interiorNodes size = "+graph.getInteriorNodes().size());
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
        int numOfReqIntNodesBelowThresh = 60, numOfIntNodesBelowThreshIntermed = 0, numOfIntNodesBelowThreshLow = 0;
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
        breakRatio = ratio;
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
