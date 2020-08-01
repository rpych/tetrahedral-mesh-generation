package app;

import model.ModelGraph;
import model.Vertex;
import model.Coordinates;
import controller.Transformator;
import visualization.MatlabVisualizer;

import java.util.ArrayList;
import java.util.List;

public class Main {
	
    public static void main(String[] args) {
        ModelGraph tetrahedra = generateTetrahedra();
        Transformator transformator = new Transformator(tetrahedra);
        tetrahedra = transformator.transform();
        MatlabVisualizer matlabVisualizer = new MatlabVisualizer(tetrahedra, "vis");
        matlabVisualizer.saveCode();
        
        ModelGraph tetrahedra2 = generateTetrahedra2();
        Transformator transformator2 = new Transformator(tetrahedra2);
        tetrahedra2 = transformator2.transform();
        MatlabVisualizer matlabVisualizer2 = new MatlabVisualizer(tetrahedra2, "vis2");
        matlabVisualizer2.saveCode();
        
        System.out.println("Program ended successfully");
    }
    
    private static ModelGraph generateTetrahedra() {
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
        graph.insertFaceAutoNamed(nodes.get(1), nodes.get(2), nodes.get(3)).setR(true);
        graph.insertFaceAutoNamed(nodes.get(2), nodes.get(0), nodes.get(3));

        graph.insertInteriorNodeAutoNamed(nodes.get(0), nodes.get(1), nodes.get(2), nodes.get(3));

        return graph;
    }
    
    private static ModelGraph generateTetrahedra2() {
        ModelGraph graph = new ModelGraph("Graph2");

        List<Vertex> nodes = new ArrayList<>();
        nodes.add(graph.insertVertexAutoNamed(new Coordinates(0.64, 0.50, 0.80)));
        nodes.add(graph.insertVertexAutoNamed(new Coordinates(0.33, 0.2, 1.0)));
        nodes.add(graph.insertVertexAutoNamed(new Coordinates(1.0, 2.0, 1.4)));
        nodes.add(graph.insertVertexAutoNamed(new Coordinates(2.0, 0.4, 1.5)));
        
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

        graph.insertInteriorNodeAutoNamed(nodes.get(0), nodes.get(1), nodes.get(2), nodes.get(3));

        return graph;
    }

}
