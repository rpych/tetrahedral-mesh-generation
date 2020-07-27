
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.javatuples.Pair;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


import model.Coordinates;
import model.FaceNode;
import model.GraphEdge;
import model.GraphNode;
import model.ModelGraph;
import model.Vertex;


public class GraphEdgeTests {
    @Test
    void isBetweenVerticesTest(){
    	ModelGraph g = new ModelGraph("graph");
       GraphNode v1 = new Vertex(g, "vertex", new Coordinates(0.0, 0.0, 0.0));
       GraphNode v2 = new Vertex(g, "vertex", new Coordinates(1.0, 1.0, 1.0));
       GraphNode f1 = new FaceNode(g, "faceNode", new Coordinates(2.0, 2.0, 2.0));
       GraphNode f2 = new FaceNode(g, "faceNode", new Coordinates(3.0, 3.0, 3.0));
       
       GraphEdge vToV = new GraphEdge("vToV",  new Pair<GraphNode, GraphNode>(v1, v2), false);
       GraphEdge vToF = new GraphEdge("vToF",  new Pair<GraphNode, GraphNode>(v1, f2), false);
       GraphEdge fToV = new GraphEdge("fToV",  new Pair<GraphNode, GraphNode>(f1, v1), false);
       GraphEdge fToF = new GraphEdge("fToF",  new Pair<GraphNode, GraphNode>(f1, f2), false);
       
       assertTrue(vToV.isBetweenVertices());
       assertFalse(vToF.isBetweenVertices());
       assertFalse(fToV.isBetweenVertices());
       assertFalse(fToF.isBetweenVertices());
    }
}
