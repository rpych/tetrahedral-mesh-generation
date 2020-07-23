package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import controller.Transformator;
import model.Coordinates;
import model.FaceNode;
import model.GraphEdge;
import model.ModelGraph;
import model.Vertex;

class TransformatorTests {

	@Test
	void sampleTransformationTest() {
		ModelGraph graph = createGraph1();
		        
        Transformator transformator = new Transformator(graph);
        graph = transformator.transform();
        
        checkVertices1(graph);
        // TODO:
        // checkEdges1(graph);
        checkFaces1(graph);
	}
	
	ModelGraph createGraph1() {
		ModelGraph graph = new ModelGraph("Graph");

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
        graph.insertFaceAutoNamed(nodes.get(1), nodes.get(2), nodes.get(3)).setR(true);;
        graph.insertFaceAutoNamed(nodes.get(2), nodes.get(0), nodes.get(3));

        return graph;
	}
	
	void checkVertices1(ModelGraph graph) {
        Collection<Vertex> vertices = graph.getVertices();
        assertEquals(6, vertices.size());
        
        Optional<Vertex> v;
        
        v = graph.getVertex("V_0,60_0,40_0,80");
        assertTrue(v.isPresent());
        assertEquals(0.60, v.get().getXCoordinate(), 0.01);
        assertEquals(0.40, v.get().getYCoordinate(), 0.01);
        assertEquals(0.80, v.get().getZCoordinate(), 0.01);
        
        v = graph.getVertex("V_0,50_0,20_0,00");
        assertTrue(v.isPresent());
        assertEquals(0.50, v.get().getXCoordinate(), 0.01);
        assertEquals(0.20, v.get().getYCoordinate(), 0.01);
        assertEquals(0.00, v.get().getZCoordinate(), 0.01);

        v = graph.getVertex("V_1,00_0,00_0,00");
        assertTrue(v.isPresent());
        assertEquals(1.00, v.get().getXCoordinate(), 0.01);
        assertEquals(0.00, v.get().getYCoordinate(), 0.01);
        assertEquals(0.00, v.get().getZCoordinate(), 0.01);
        
        v = graph.getVertex("V_0,00_0,00_0,00");
        assertTrue(v.isPresent());
        assertEquals(0.00, v.get().getXCoordinate(), 0.01);
        assertEquals(0.00, v.get().getYCoordinate(), 0.01);
        assertEquals(0.00, v.get().getZCoordinate(), 0.01);
        
        v = graph.getVertex("V_0,50_0,00_0,00");
        assertTrue(v.isPresent());
        assertEquals(0.50, v.get().getXCoordinate(), 0.01);
        assertEquals(0.00, v.get().getYCoordinate(), 0.01);
        assertEquals(0.00, v.get().getZCoordinate(), 0.01);
                
        v = graph.getVertex("V_0,30_0,20_0,40");
        assertTrue(v.isPresent());
        assertEquals(0.30, v.get().getXCoordinate(), 0.01);
        assertEquals(0.20, v.get().getYCoordinate(), 0.01);
        assertEquals(0.40, v.get().getZCoordinate(), 0.01);
	}
	
	void checkEdges1(ModelGraph graph) {
		Collection<GraphEdge> edges = graph.getEdges();
		for(GraphEdge edge : edges) {
			System.out.println(edge.getId());
		}
	}
	
	void checkFaces1(ModelGraph graph) {
		Collection<FaceNode> faces = graph.getFaces();
        assertEquals(8, faces.size());
      
        Optional<FaceNode> f;
        
        f = graph.getFace("F_0,27_0,07_0,13");
        assertTrue(f.isPresent());
        assertEquals(0.27, f.get().getXCoordinate(), 0.01);
        assertEquals(0.07, f.get().getYCoordinate(), 0.01);
        assertEquals(0.13, f.get().getZCoordinate(), 0.01);
        assertFalse(f.get().isR());
        
        f = graph.getFace("F_0,60_0,07_0,13");
        assertTrue(f.isPresent());
        assertEquals(0.60, f.get().getXCoordinate(), 0.01);
        assertEquals(0.07, f.get().getYCoordinate(), 0.01);
        assertEquals(0.13, f.get().getZCoordinate(), 0.01);
        assertFalse(f.get().isR());

        f = graph.getFace("F_0,63_0,20_0,40");
        assertTrue(f.isPresent());
        assertEquals(0.63, f.get().getXCoordinate(), 0.01);
        assertEquals(0.20, f.get().getYCoordinate(), 0.01);
        assertEquals(0.40, f.get().getZCoordinate(), 0.01);
        assertFalse(f.get().isR());

        f = graph.getFace("F_0,33_0,07_0,00");
        assertTrue(f.isPresent());
        assertEquals(0.33, f.get().getXCoordinate(), 0.01);
        assertEquals(0.07, f.get().getYCoordinate(), 0.01);
        assertEquals(0.00, f.get().getZCoordinate(), 0.01);
        assertFalse(f.get().isR());

        f = graph.getFace("F_0,67_0,07_0,00");
        assertTrue(f.isPresent());
        assertEquals(0.67, f.get().getXCoordinate(), 0.01);
        assertEquals(0.07, f.get().getYCoordinate(), 0.01);
        assertEquals(0.00, f.get().getZCoordinate(), 0.01);
        assertFalse(f.get().isR());
        
        f = graph.getFace("F_0,27_0,13_0,13");
        assertTrue(f.isPresent());
        assertEquals(0.27, f.get().getXCoordinate(), 0.01);
        assertEquals(0.13, f.get().getYCoordinate(), 0.01);
        assertEquals(0.13, f.get().getZCoordinate(), 0.01);
        assertFalse(f.get().isR());
        
        f = graph.getFace("F_0,47_0,27_0,40");
        assertTrue(f.isPresent());
        assertEquals(0.47, f.get().getXCoordinate(), 0.01);
        assertEquals(0.27, f.get().getYCoordinate(), 0.01);
        assertEquals(0.40, f.get().getZCoordinate(), 0.01);
        assertFalse(f.get().isR());
        
        f = graph.getFace("F_0,70_0,20_0,27");
        assertTrue(f.isPresent());
        assertEquals(0.70, f.get().getXCoordinate(), 0.01);
        assertEquals(0.20, f.get().getYCoordinate(), 0.01);
        assertEquals(0.27, f.get().getZCoordinate(), 0.01);
        assertFalse(f.get().isR());
	}
}
