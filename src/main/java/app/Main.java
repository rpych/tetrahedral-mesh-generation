package app;

import model.*;
import visualization.MatlabVisualizer;
import controller.Transformator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
	private static Map<String, Integer> nodesIDs = new HashMap<String, Integer>();
	
    public static void main(String[] args) {
        ModelGraph tetrahedra = generateP4Tetrahedra();
        tetrahedra.getFaceNonOptional("F_0,50_0,07_0,00").setR(true);
        tetrahedra = Transformator.makeP4(tetrahedra);
        MatlabVisualizer matlabVisualizer = new MatlabVisualizer(tetrahedra, "vis");
        matlabVisualizer.saveCode();
        System.out.println("Program ended successfully");
    }

    private static ModelGraph generateTetrahedra() {
        ModelGraph graph = new ModelGraph("Graph");

        List<Vertex> nodes = new ArrayList<>();
        nodes.add(graph.insertVertex("n1", new Coordinates(0., 0., 0.)));
        nodes.add(graph.insertVertex("n2", new Coordinates(1., 0., 0.)));
        nodes.add(graph.insertVertex("n3", new Coordinates(0.5, 0.866025, 0.)));
        nodes.add(graph.insertVertex("n4", new Coordinates(0.5, 0.433013, 0.816497)));

        graph.insertEdge("e1", nodes.get(0), nodes.get(1), true);
        graph.insertEdge("e2", nodes.get(1), nodes.get(2), true);
        graph.insertEdge("e3", nodes.get(2), nodes.get(0), true);
        graph.insertEdge("e4", nodes.get(0), nodes.get(3), true);
        graph.insertEdge("e5", nodes.get(1), nodes.get(3), true);
        graph.insertEdge("e6", nodes.get(2), nodes.get(3), true);

        graph.insertFace("t4", nodes.get(0), nodes.get(1), nodes.get(2));
        graph.insertFace("t3", nodes.get(0), nodes.get(1), nodes.get(3));
        graph.insertFace("t1", nodes.get(1), nodes.get(2), nodes.get(3));
        graph.insertFace("t2", nodes.get(2), nodes.get(0), nodes.get(3));

        return graph;
    }
    
    private static ModelGraph generateP4Tetrahedra() {
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
        graph.insertFaceAutoNamed(nodes.get(1), nodes.get(2), nodes.get(3));
        graph.insertFaceAutoNamed(nodes.get(2), nodes.get(0), nodes.get(3));

        return graph;
    }
    
    private static ModelGraph generateP4Grid() {
        ModelGraph graph = new ModelGraph("Graph");

        List<Vertex> nodes = new ArrayList<>();
        nodes.add(graph.insertVertexAutoNamed(new Coordinates(0.0, 1.0, 0.)));
        nodes.add(graph.insertVertexAutoNamed(new Coordinates(1.0, 1.0, 0.)));
        nodes.add(graph.insertVertexAutoNamed(new Coordinates(2.0, 1.0, 0.)));
        nodes.add(graph.insertVertexAutoNamed(new Coordinates(0.5, 0., 0.)));
        nodes.add(graph.insertVertexAutoNamed(new Coordinates(1.5, 0., 0.)));
        nodes.add(graph.insertVertexAutoNamed(new Coordinates(1.0, -1.0, 0.)));

        
        graph.insertEdgeAutoNamed(nodes.get(0), nodes.get(1), true);
        graph.insertEdgeAutoNamed(nodes.get(1), nodes.get(2), true);
        graph.insertEdgeAutoNamed(nodes.get(0), nodes.get(3), true);
        graph.insertEdgeAutoNamed(nodes.get(3), nodes.get(1), true);
        graph.insertEdgeAutoNamed(nodes.get(1), nodes.get(4), true);
        graph.insertEdgeAutoNamed(nodes.get(4), nodes.get(2), true);
        graph.insertEdgeAutoNamed(nodes.get(3), nodes.get(4), true);
        graph.insertEdgeAutoNamed(nodes.get(3), nodes.get(5), true);
        graph.insertEdgeAutoNamed(nodes.get(4), nodes.get(5), true);

        graph.insertFaceAutoNamed(nodes.get(0), nodes.get(1), nodes.get(3));
        graph.insertFaceAutoNamed(nodes.get(1), nodes.get(2), nodes.get(4));
        graph.insertFaceAutoNamed(nodes.get(1), nodes.get(3), nodes.get(4));
        graph.insertFaceAutoNamed(nodes.get(3), nodes.get(4), nodes.get(5));

        return graph;
    }
}
