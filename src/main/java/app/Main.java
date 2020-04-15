package app;

import model.*;
import visualization.Visualizer;

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
        saveMatlabVisualization(tetrahedra, "vis");
        Visualizer visualizer = new Visualizer(tetrahedra);
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
    
    private static void saveMatlabVisualization(ModelGraph graph, String functionName) {
    	try {
    		String filename = functionName + ".m";
    		FileWriter fileWriter = new FileWriter(filename);
    		startFunctionDefinition(fileWriter, functionName);
    		writeNodesCoordinates(fileWriter, graph);
    		writePlottingNodes(fileWriter);
    		writeNormalEdges(fileWriter, graph);
    		writePlottingEdges(fileWriter, "red");
    		writeFaceEdges(fileWriter, graph);
    		writePlottingEdges(fileWriter, "grey");
    		endFunctionDefinition(fileWriter);
    		fileWriter.close();
    	}catch(IOException e) {
    		System.out.println("An error ocured while writing to matlab source file.");
    		e.printStackTrace();
    	}
    }
    
    private static void startFunctionDefinition(FileWriter fileWriter, String functionName) throws IOException {
    	fileWriter.write("function " + functionName + "()\n\n");
    }
    
    private static void endFunctionDefinition(FileWriter fileWriter) throws IOException {
    	fileWriter.write("end\n");
    }
    
    private static void writeNodesCoordinates(FileWriter fileWriter, ModelGraph graph) throws IOException {
    	fileWriter.write("nodes = [ ...\n");
    	int idx = 1;
    	for(Vertex v : graph.getVertices()) {
    		fileWriter.write("\t" + v.getXCoordinate() + " " + v.getYCoordinate() + " " + v.getZCoordinate() + "; ...\n");
    		nodesIDs.put(v.getId(), idx++);
    	}
    	for(FaceNode n : graph.getFaces()) {
    		fileWriter.write("\t" + n.getXCoordinate() + " " + n.getYCoordinate() + " " + n.getZCoordinate() + "; ...\n");
    		nodesIDs.put(n.getId(), idx++);
    	}
    	fileWriter.write("];\n\n");
    }
    
    private static void writePlottingNodes(FileWriter fileWriter) throws IOException{
    	fileWriter.write("grey = [.25 .25 .25];\n");
    	fileWriter.write("scatter3(nodes(:,1),nodes(:,2),nodes(:,3),'filled','MarkerFaceColor',grey)\n");
    	fileWriter.write("axis vis3d\n");
    	fileWriter.write("grid off\n");
    	fileWriter.write("box on\n\n");
    }
    
    private static void writeNormalEdges(FileWriter fileWriter, ModelGraph graph) throws IOException {
    	writeNormalEdgesSources(fileWriter, graph);
    	writeNormalEdgesEndings(fileWriter, graph);
    }
    
    private static void writeNormalEdgesSources(FileWriter fileWriter, ModelGraph graph) throws IOException {
    	fileWriter.write("s = [");
    	 for(GraphEdge edge : graph.getEdges()) {
    		 if(edge.getNode0().getId().startsWith("V") && edge.getNode1().getId().startsWith("V")) {
    			 fileWriter.write(nodesIDs.get(edge.getNode0().getId()).toString() + " ");
    		 }
    	}
    	fileWriter.write("];\n");
    }
    
    private static void writeNormalEdgesEndings(FileWriter fileWriter, ModelGraph graph) throws IOException {
    	fileWriter.write("e = [");
    	int normalEdgesNumber = 0;
    	 for(GraphEdge edge : graph.getEdges()) {
    		 if(edge.getNode0().getId().startsWith("V") && edge.getNode1().getId().startsWith("V")) {
    			 fileWriter.write(nodesIDs.get(edge.getNode1().getId()).toString() + " ");
    			 normalEdgesNumber++;
    		 }
    	}
    	fileWriter.write("];\n");
    	fileWriter.write("n = nan(1," + normalEdgesNumber + ");\n\n");
    }
    
    private static void writeFaceEdges(FileWriter fileWriter, ModelGraph graph) throws IOException {
    	writeFaceEdgesSources(fileWriter, graph);
    	writeFaceEdgesEndings(fileWriter, graph);
    }
    
    private static void writeFaceEdgesSources(FileWriter fileWriter, ModelGraph graph) throws IOException {
    	fileWriter.write("s = [");
    	 for(GraphEdge edge : graph.getEdges()) {
    		 if(edge.getNode0().getId().startsWith("F") || edge.getNode1().getId().startsWith("F")) {
    			 fileWriter.write(nodesIDs.get(edge.getNode0().getId()).toString() + " ");
    		 }
    	}
    	fileWriter.write("];\n");
    }
    
    private static void writeFaceEdgesEndings(FileWriter fileWriter, ModelGraph graph) throws IOException {
    	fileWriter.write("e = [");
    	int faceEdgesNumber = 0;
    	 for(GraphEdge edge : graph.getEdges()) {
    		 if(edge.getNode0().getId().startsWith("F") || edge.getNode1().getId().startsWith("F")) {
    			 fileWriter.write(nodesIDs.get(edge.getNode1().getId()).toString() + " ");
    			 faceEdgesNumber++;
    		 }
    	}
    	fileWriter.write("];\n");
    	fileWriter.write("n = nan(1," + faceEdgesNumber + ");\n\n");
    }
    
    private static void writePlottingEdges(FileWriter fileWriter, String color) throws IOException{
    	if(color.equals("red")) {
        	fileWriter.write("color = [1 0 0];\n");
    	}else {
    		fileWriter.write("color = [0.25 0.25 0.25];\n");
    	}
    	fileWriter.write("lx = [nodes(s',1)'; nodes(e',1)'; n];\n");
    	fileWriter.write("ly = [nodes(s',2)'; nodes(e',2)'; n];\n");
    	fileWriter.write("lz = [nodes(s',3)'; nodes(e',3)'; n];\n");
    	fileWriter.write("l = line(lx(:),ly(:),lz(:),'Color',color);\n\n");
    }
}
