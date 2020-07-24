import controller.ITransformator;
import controller.Transformator;
import controller.TransformatorForTests;
import model.*;
import org.javatuples.Triplet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import visualization.MatlabVisualizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class GraphTransformatorTest {

    private ModelGraph inputGraph;
    private Transformator transformator; //for inputGraph
    private ModelGraph expectedGraph;
    private TransformatorForTests transformatorForTests; //for expectedGraph

    static ModelGraph makeGraphTransformation(ITransformator transformator){
        ModelGraph tetrahedra = transformator.transform();
        //MatlabVisualizer matlabVisualizer = new MatlabVisualizer(tetrahedra, "vis");
        //matlabVisualizer.saveCode();
        return tetrahedra;
    }

    private void prepareInputGraph(){
        this.inputGraph = generateTetrahedra("InGraph");
        this.transformator = new Transformator(this.inputGraph);
    }

    private void prepareExpectedGraph(){
        this.expectedGraph = generateTetrahedra("ExGraph");
        this.transformatorForTests = new TransformatorForTests(this.expectedGraph);
    }

    private static ModelGraph generateTetrahedra(String name) {
        ModelGraph graph = new ModelGraph(name);

        List<Vertex> nodes = new ArrayList<>();
        nodes.add(graph.insertVertexAutoNamed(new Coordinates(0.6, 0.40, 0.80)));
        nodes.add(graph.insertVertexAutoNamed(new Coordinates(0.5, 0.2, 0.0)));
        nodes.add(graph.insertVertexAutoNamed(new Coordinates(1.0, 0.0, 0.0)));
        nodes.add(graph.insertVertexAutoNamed(new Coordinates(0.0, 0.0, 0.0)));

        graph.insertEdgeAutoNamed(nodes.get(0), nodes.get(1), true);
        graph.insertEdgeAutoNamed(nodes.get(1), nodes.get(2), true);
        graph.insertEdgeAutoNamed(nodes.get(2), nodes.get(0), true);
        graph.insertEdgeAutoNamed(nodes.get(0), nodes.get(3), true);
        graph.insertEdgeAutoNamed(nodes.get(1), nodes.get(3), true);
        graph.insertEdgeAutoNamed(nodes.get(2), nodes.get(3), true);

        graph.insertFaceAutoNamed(nodes.get(0), nodes.get(1), nodes.get(2));
        graph.insertFaceAutoNamed(nodes.get(0), nodes.get(1), nodes.get(3));
        graph.insertFaceAutoNamed(nodes.get(1), nodes.get(2), nodes.get(3)).setR(true);
        graph.insertFaceAutoNamed(nodes.get(2), nodes.get(0), nodes.get(3));

        return graph;
    }

    private Boolean isVertexPresentInInputGraph(Vertex v, ModelGraph transformed){
        Optional<Vertex> optVer =  transformed.getVertex(v.getId());
        return optVer.isPresent() && (v.getCoordinates()).equals(optVer.get().getCoordinates());
    }

    private Boolean isEdgePresentInInputGraph(GraphEdge e, ModelGraph transformed){
        Optional<GraphEdge> optEdge = transformed.getEdgeById(e.getId());
        return optEdge.isPresent() && transformed.isEdgeBetween(optEdge.get().getEdgeNodes().getValue0(),
                                                                optEdge.get().getEdgeNodes().getValue1());
    }

    private Boolean isFacePresentInInputGraph(FaceNode f, ModelGraph transformed){
        Optional<FaceNode> optFace = transformed.getFace(f.getId());
        if(!optFace.isPresent()) { return false; }
        Triplet<Vertex, Vertex, Vertex> inputTriangle = optFace.get().getTriangle();
        Triplet<Vertex, Vertex, Vertex> expectedTriangle = f.getTriangle();
        return (inputTriangle.getValue0().getCoordinates()).equals(expectedTriangle.getValue0().getCoordinates()) &&
                (inputTriangle.getValue1().getCoordinates()).equals(expectedTriangle.getValue1().getCoordinates()) &&
                (inputTriangle.getValue2().getCoordinates()).equals(expectedTriangle.getValue2().getCoordinates()) &&
                f.getCoordinates().equals(optFace.get().getCoordinates()); //this line for checking face representative node/vertex
    }

    @BeforeEach
    void setupGraphsAndTransformators(){
        prepareInputGraph();
        prepareExpectedGraph();
    }

    @Test
    void simpletest(){
       assertEquals(1, 1);
    }

    @Test
    void checkEqualNumOfVertexesInBothGraphs(){
        ModelGraph transformedExpGraph = makeGraphTransformation(this.transformatorForTests);

        ModelGraph transformedInputGraph = makeGraphTransformation(this.transformator);

        assertEquals(transformedExpGraph.getVerticesNum(), transformedInputGraph.getVerticesNum());
    }

    @Test
    void checkEqualNumOfEdgesInBothGraphs(){
        ModelGraph transformedExpGraph = makeGraphTransformation(this.transformatorForTests);

        ModelGraph transformedInputGraph = makeGraphTransformation(this.transformator);

        assertEquals(transformedExpGraph.getEdgesNum(), transformedInputGraph.getEdgesNum());
    }

    @Test
    void checkEqualNumOfFacesInBothGraphs(){
        ModelGraph transformedExpGraph = makeGraphTransformation(this.transformatorForTests);

        ModelGraph transformedInputGraph = makeGraphTransformation(this.transformator);

        assertEquals(transformedExpGraph.getFacesNum(), transformedInputGraph.getFacesNum());
    }

    @Test
    void checkSameVerticesInBothGraphs(){
        ModelGraph transformedExpGraph = makeGraphTransformation(this.transformatorForTests);

        ModelGraph transformedInputGraph = makeGraphTransformation(this.transformator);

        List<Boolean> results = transformedExpGraph.getVertices().stream().map(v -> isVertexPresentInInputGraph(v, transformedInputGraph))
                                                                          .collect(Collectors.toList());

        for(Boolean r: results) {
            assertTrue(r);
        }
    }

    @Test
    void checkSameEdgesInBothGraphs(){
        ModelGraph transformedExpGraph = makeGraphTransformation(this.transformatorForTests);
        /*List<Vertex> nodes = new ArrayList<>();
        nodes.add(transformedExpGraph.insertVertexAutoNamed(new Coordinates(-0.6, -0.40, -0.80)));
        nodes.add(transformedExpGraph.insertVertexAutoNamed(new Coordinates(0.3, 0.1, 0.0)));
        transformedExpGraph.insertEdgeAutoNamed(nodes.get(0), nodes.get(1), true);*/

        ModelGraph transformedInputGraph = makeGraphTransformation(this.transformator);

        List<Boolean> results = transformedExpGraph.getEdges().stream().map(e -> isEdgePresentInInputGraph(e, transformedInputGraph))
                .collect(Collectors.toList());

        for(Boolean r: results) {
            assertTrue(r);
        }
    }

    @Test
    void checkSameFacesInBothGraphs(){
        ModelGraph transformedExpGraph = makeGraphTransformation(this.transformatorForTests);

        ModelGraph transformedInputGraph = makeGraphTransformation(this.transformator);

        List<Boolean> results = transformedExpGraph.getFaces().stream().map(f -> isFacePresentInInputGraph(f, transformedInputGraph))
                .collect(Collectors.toList());

        for(Boolean r: results) {
            assertTrue(r);
        }
    }
}
