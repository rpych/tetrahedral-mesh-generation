package common;

import controller.TransformatorForLayers;
import model.*;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class BreakingStats {

    boolean arePropertiesStatsActive;
    boolean areStructureStatsActive;

    Double breakRatio = 0.0d;

    FileWriter edgesRatioFileWriter;;
    FileWriter anglesFileWriter;

    public BreakingStats(boolean arePropertiesStatsActive, boolean areStructureStatsActive){
        this.arePropertiesStatsActive = arePropertiesStatsActive;
        this.areStructureStatsActive = areStructureStatsActive;
        createFileWriters();
    }

    public void checkFacesConnected(ModelGraph graph){
        if(!areStructureStatsActive) return;

        for(FaceNode face: graph.getFaces()){
            if(!graph.areVertexesLinked(face)){
                System.out.println("Face exists but its vertices are not linked !!!!!" + face.getId());
            }
        }
    }

    public boolean checkAllFacesBelongToInteriorNode(ModelGraph graph){
        if(!areStructureStatsActive) return true;

        int counter = 0;
        Map<String, Integer> facesInInteriorCount = new HashMap<>();
        Collection<FaceNode> faces = graph.getFaces();
        for(FaceNode face: faces){
            int out = isFaceInAnyInteriorNode(graph, face);
            counter += out;
            if(out == 0) counter += 1;
            facesInInteriorCount.put(face.getId(), out);
            if(out == 0 ){
                graph.debugFaces.add(face);
            }
        }
        boolean f = true;
        for(Map.Entry<String, Integer> entry: facesInInteriorCount.entrySet()){
            if(entry.getValue().equals(0) || entry.getValue() > 2){
                f = false;
                System.out.println("APPEARANCE IN INTERIORS = "+entry.getKey() + " :"+ entry.getValue());
                graph.getFacesMap().remove(entry.getKey());
            }
        }
        System.out.println("Faces counter = "+counter + ", whereas InteriorNodes count = "+graph.getInteriorNodes().size() + " and " +
                " InteriorNodes*4 = "+ graph.getInteriorNodes().size()*4);
        return f;
    }

    public int isFaceInAnyInteriorNode(ModelGraph graph, FaceNode face){
        int count = 0;
        Collection<InteriorNode> interiorNodes = graph.getInteriorNodes();
        for(InteriorNode node: interiorNodes){
            if(node.containsFace(face)) count++;
        }
        return count;
    }

    public void checkAdaptationProperties(ModelGraph graph){
        if(arePropertiesStatsActive) {
            checkInteriorNodesEdgesLenRatio(graph);
            checkAnglesBetweenEdgesInInteriorNode(graph);
        }
    }

    public void checkInteriorNodesEdgesLenRatio(ModelGraph graph){
        for(InteriorNode node: graph.getInteriorNodes()){
            if(!node.isNewlyAdded()) continue;
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
            }
            double ratio = (threeLongestSum/ovsum);
            writeToEdgeRatioFileWriter(TransformatorForLayers.counter+";"+node.getId()+";"+ratio+"\n");
            System.out.println("Edge Ratio = "+ ratio);
        }
    }

    public void checkAnglesBetweenEdgesInInteriorNode(ModelGraph graph){
        for(InteriorNode node: graph.getInteriorNodes()){
            if(!node.isNewlyAdded()) continue;
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
            writeToAnglesFileWriter(TransformatorForLayers.counter+";"+node.getId()+";"+minAngle+"\n");
            System.out.println("Min angle in interiorNode = "+ minAngle);

        }
    }

    public Double getMinAngleBetweenFaceEdges(Triplet<Vertex, Vertex, Vertex> faceVert){

        Double angle1 = calcAngle(faceVert.getValue0(), faceVert.getValue1(), faceVert.getValue2());
        Double angle2 = calcAngle(faceVert.getValue1(), faceVert.getValue0(), faceVert.getValue2());
        Double angle3 = calcAngle(faceVert.getValue2(), faceVert.getValue0(), faceVert.getValue1());

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

        return  angleInDegrees;
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

    public void checkInteriorNodesMinMaxBreakingRatio(ModelGraph graph){
        if(!arePropertiesStatsActive) return;

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
        System.out.println("Current breaking ratio = " + ratio);
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

}
