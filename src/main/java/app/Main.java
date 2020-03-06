package app;

import model.*;
import visualization.Visualizer;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ModelGraph graph = generateTetrahedra();
        Visualizer visualizer = new Visualizer(graph);
        visualizer.visualize();
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

        graph.insertFace("f1", nodes.get(0), nodes.get(1), nodes.get(2));
        graph.insertFace("f2", nodes.get(0), nodes.get(1), nodes.get(3));
        graph.insertFace("f3", nodes.get(1), nodes.get(2), nodes.get(3));
        graph.insertFace("f4", nodes.get(2), nodes.get(0), nodes.get(3));

        return graph;
    }

}
