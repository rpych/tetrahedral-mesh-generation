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
        
        ModelGraph cuboid = generateCuboid(new Coordinates(0.0, 0.0, 0.0), 2.0, 1.0, 1.0);
//        Transformator transformator3 = new Transformator(cuboid);
//        cuboid = transformator3.transform();
        MatlabVisualizer matlabVisualizer3 = new MatlabVisualizer(cuboid, "vis3");
        matlabVisualizer3.saveCode();
        
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

    private static ModelGraph generateCuboid(Coordinates basePoint, double x, double y, double z) {
        ModelGraph graph = new ModelGraph("Graph");

        List<Vertex> nodes = new ArrayList<>();
        nodes.add(graph.insertVertexAutoNamed(new Coordinates(basePoint.getX(), basePoint.getY(), basePoint.getZ())));
        nodes.add(graph.insertVertexAutoNamed(new Coordinates(basePoint.getX() + x, basePoint.getY(), basePoint.getZ())));
        nodes.add(graph.insertVertexAutoNamed(new Coordinates(basePoint.getX() + x, basePoint.getY() + y, basePoint.getZ())));
        nodes.add(graph.insertVertexAutoNamed(new Coordinates(basePoint.getX(), basePoint.getY() + y, basePoint.getZ())));
        nodes.add(graph.insertVertexAutoNamed(new Coordinates(basePoint.getX(), basePoint.getY(), basePoint.getZ() + z)));
        nodes.add(graph.insertVertexAutoNamed(new Coordinates(basePoint.getX() + x, basePoint.getY(), basePoint.getZ() + z)));
        nodes.add(graph.insertVertexAutoNamed(new Coordinates(basePoint.getX() + x, basePoint.getY() + y, basePoint.getZ() + z)));
        nodes.add(graph.insertVertexAutoNamed(new Coordinates(basePoint.getX(), basePoint.getY() + y, basePoint.getZ() + z)));
        
        graph.insertEdgeAutoNamed(nodes.get(0), nodes.get(1), true);
        graph.insertEdgeAutoNamed(nodes.get(0), nodes.get(3), true);
        graph.insertEdgeAutoNamed(nodes.get(0), nodes.get(4), true);
        graph.insertEdgeAutoNamed(nodes.get(1), nodes.get(2), true);
        graph.insertEdgeAutoNamed(nodes.get(1), nodes.get(3), true);
        graph.insertEdgeAutoNamed(nodes.get(1), nodes.get(4), true);
        graph.insertEdgeAutoNamed(nodes.get(1), nodes.get(5), true);
        graph.insertEdgeAutoNamed(nodes.get(1), nodes.get(6), true);
        graph.insertEdgeAutoNamed(nodes.get(2), nodes.get(3), true);
        graph.insertEdgeAutoNamed(nodes.get(2), nodes.get(6), true);
        graph.insertEdgeAutoNamed(nodes.get(3), nodes.get(4), true);
        graph.insertEdgeAutoNamed(nodes.get(3), nodes.get(6), true);
        graph.insertEdgeAutoNamed(nodes.get(3), nodes.get(7), true);
        graph.insertEdgeAutoNamed(nodes.get(4), nodes.get(5), true);
        graph.insertEdgeAutoNamed(nodes.get(4), nodes.get(6), true);
        graph.insertEdgeAutoNamed(nodes.get(4), nodes.get(7), true);
        graph.insertEdgeAutoNamed(nodes.get(5), nodes.get(6), true);
        graph.insertEdgeAutoNamed(nodes.get(6), nodes.get(7), true);

        graph.insertFaceAutoNamed(nodes.get(0), nodes.get(1), nodes.get(3)).setR(true);;
        graph.insertFaceAutoNamed(nodes.get(0), nodes.get(1), nodes.get(4));
        graph.insertFaceAutoNamed(nodes.get(0), nodes.get(3), nodes.get(4));
        graph.insertFaceAutoNamed(nodes.get(1), nodes.get(2), nodes.get(3));
        graph.insertFaceAutoNamed(nodes.get(1), nodes.get(2), nodes.get(6));
        graph.insertFaceAutoNamed(nodes.get(1), nodes.get(3), nodes.get(4));
        graph.insertFaceAutoNamed(nodes.get(1), nodes.get(3), nodes.get(6));
        graph.insertFaceAutoNamed(nodes.get(1), nodes.get(4), nodes.get(6));
        graph.insertFaceAutoNamed(nodes.get(1), nodes.get(4), nodes.get(5));
        graph.insertFaceAutoNamed(nodes.get(1), nodes.get(5), nodes.get(6));
        graph.insertFaceAutoNamed(nodes.get(2), nodes.get(3), nodes.get(6));
        graph.insertFaceAutoNamed(nodes.get(3), nodes.get(4), nodes.get(6));
        graph.insertFaceAutoNamed(nodes.get(3), nodes.get(4), nodes.get(7));
        graph.insertFaceAutoNamed(nodes.get(3), nodes.get(6), nodes.get(7));
        graph.insertFaceAutoNamed(nodes.get(4), nodes.get(5), nodes.get(6));
        graph.insertFaceAutoNamed(nodes.get(4), nodes.get(6), nodes.get(7));

        graph.insertInteriorNodeAutoNamed(nodes.get(0), nodes.get(1), nodes.get(3), nodes.get(4));
        graph.insertInteriorNodeAutoNamed(nodes.get(1), nodes.get(2), nodes.get(3), nodes.get(6));
        graph.insertInteriorNodeAutoNamed(nodes.get(1), nodes.get(3), nodes.get(4), nodes.get(6));
        graph.insertInteriorNodeAutoNamed(nodes.get(1), nodes.get(4), nodes.get(5), nodes.get(6));
        graph.insertInteriorNodeAutoNamed(nodes.get(3), nodes.get(4), nodes.get(6), nodes.get(7));

        return graph;
    }
}
