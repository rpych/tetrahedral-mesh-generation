package parallel;

import model.*;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class TetrahedraGenerator implements Callable<Integer> { //Runnable

    ModelGraph graph;
    public Integer startFaceIdx;
    public Integer facesNumToCheck;


    public TetrahedraGenerator(ModelGraph graph, Integer startFaceIdx, Integer facesNumToCheck){
        this.graph = graph;
        this.startFaceIdx = startFaceIdx;
        this.facesNumToCheck = facesNumToCheck;
    }

    @Override
    public Integer call(){
        createInteriorNodesForNewlyFoundSubGraphs();
        return 0;
    }

    public ModelGraph createInteriorNodesForNewlyFoundSubGraphs(){
        Integer counter = 0;
        for(FaceNode face: graph.getFaces() ){
            counter++;
            if(counter < startFaceIdx || counter >= startFaceIdx + facesNumToCheck) continue;
            List<Quartet<Vertex, Vertex, Vertex, Vertex>> candSubGraphs = findVerticesWhichFormsCandSubGraph(face);
            for(Quartet<Vertex, Vertex, Vertex, Vertex> candSubGraph: candSubGraphs){
                if( candSubGraph != null && !checkVerticesWithinSubgraphAlreadyProcessed(candSubGraph) ){
                    InteriorNode interiorNode = graph.insertInteriorNodeAutoNamed(candSubGraph.getValue0(), candSubGraph.getValue1(), candSubGraph.getValue2(), candSubGraph.getValue3());
                    if(graph.isInteriorNodeAddedInCurrentAlgStep(candSubGraph)){
                        interiorNode.setIsNewlyAdded(true);
                    }
                }
            }
        }
        return graph;
    }

    private boolean checkVerticesWithinSubgraphAlreadyProcessed(Quartet<Vertex, Vertex, Vertex, Vertex> candSubGraph){
        for(Map.Entry<String, InteriorNode> intNode: graph.getInteriorNodesMap().entrySet()){
            if(intNode.getValue().checkSameVerticesInInteriorNode(candSubGraph)){
                return true;
            }
        }
        return false;
    }

    private List<Quartet<Vertex, Vertex, Vertex, Vertex>> findVerticesWhichFormsCandSubGraph(FaceNode face){
        Map<String, Integer> candSubGraphTopVertices = new HashMap<>();
        Triplet<Vertex, Vertex, Vertex> triangle = face.getTriangle();
        Vertex v0 = triangle.getValue0(), v1 = triangle.getValue1(), v2 = triangle.getValue2();
        for(FaceNode f: graph.getFaces() ){
            if( !f.equals(face) && f.isFaceCongruent(face) ){
                processFaceToFindSubgraphTopVertex(f, triangle, candSubGraphTopVertices);
            }
        }
        List<Vertex> topVertices = getSubgraphTopVertex(triangle, candSubGraphTopVertices, face);
        List<Quartet<Vertex, Vertex, Vertex, Vertex>> subGraphVertices = new LinkedList<>();

        if(topVertices.size() > 0){
            for(Vertex topVertex: topVertices){
                subGraphVertices.add(new Quartet<>(v0, v1, v2, topVertex));
            }

        }
        return subGraphVertices;
    }

    private void processFaceToFindSubgraphTopVertex(FaceNode face, Triplet<Vertex, Vertex, Vertex> triangleBase, Map<String, Integer> topVertices){
        Triplet<Vertex, Vertex, Vertex> congruentTriangle = face.getTriangle();
        Vertex v0 = congruentTriangle.getValue0(), v1 = congruentTriangle.getValue1(), v2 = congruentTriangle.getValue2();
        if( !v0.getId().equals(triangleBase.getValue0().getId()) && !v0.getId().equals(triangleBase.getValue1().getId())
                && !v0.getId().equals(triangleBase.getValue2().getId()) ){
            Integer counter = topVertices.getOrDefault(v0.getId(), 0);
            topVertices.put(v0.getId(), ++counter);
        }
        else if( !v1.getId().equals(triangleBase.getValue0().getId()) && !v1.getId().equals(triangleBase.getValue1().getId())
                && !v1.getId().equals(triangleBase.getValue2().getId()) ){
            Integer counter = topVertices.getOrDefault(v1.getId(), 0);
            topVertices.put(v1.getId(), ++counter);
        }
        else if( !v2.getId().equals(triangleBase.getValue0().getId()) && !v2.getId().equals(triangleBase.getValue1().getId())
                && !v2.getId().equals(triangleBase.getValue2().getId()) ){
            Integer counter = topVertices.getOrDefault(v2.getId(), 0);
            topVertices.put(v2.getId(), ++counter);
        }
    }

    public List<Vertex> getSubgraphTopVertex(Triplet<Vertex, Vertex, Vertex> triangle, Map<String, Integer> candSubGraphTopVertices, FaceNode face){
        List<Vertex> topVertices = new LinkedList<>();
        for(Map.Entry<String, Integer> candVertex: candSubGraphTopVertices.entrySet()){
            if(candVertex.getValue() == 3) { //common vertex for all 3 faces congruent with face formed by triangle parameter
                topVertices.add(graph.getVerticesMap().get(candVertex.getKey()));
            }
        }
        return topVertices;
    }
}
