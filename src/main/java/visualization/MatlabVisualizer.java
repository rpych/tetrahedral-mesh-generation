package visualization;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import model.*;

public class MatlabVisualizer{
    private ModelGraph graph;
    private String functionName;
    private Map<String, Integer> nodesIDs;

    public MatlabVisualizer(ModelGraph graph, String functionName) {
        this.graph = graph;
        this.functionName = functionName;
        this.nodesIDs = new HashMap<String, Integer>();
    }
    
    public void saveCode() {
    	try {
    		String filename = this.functionName + ".m";
    		FileWriter fileWriter = new FileWriter(filename);
    		startFunctionDefinition(fileWriter, functionName);
    		writeNodesCoordinates(fileWriter);
    		writePlottingNodes(fileWriter);
    		writeNormalEdges(fileWriter);
    		writePlottingEdges(fileWriter, "red");
    		//writeFaceEdges(fileWriter);
    		//writePlottingEdges(fileWriter, "grey");
			//writeInteriorEdges(fileWriter);
			//writePlottingEdges(fileWriter, "green");
    		endFunctionDefinition(fileWriter);
    		fileWriter.close();
    	}catch(IOException e) {
    		System.out.println("In MatlabVisualizer.saveCode():\n"
    				+ "\tAn error ocured while writing to matlab source file.");
    		e.printStackTrace();
    	}
    }
    
    private void startFunctionDefinition(FileWriter fileWriter, String functionName) throws IOException {
    	fileWriter.write("function " + functionName + "()\n\n");
    }
    
    private void endFunctionDefinition(FileWriter fileWriter) throws IOException {
    	fileWriter.write("end\n");
    }
    
    private void writeNodesCoordinates(FileWriter fileWriter) throws IOException {
    	fileWriter.write("nodes = [ ...\n");
    	int idx = 1;
    	for(Vertex v : this.graph.getVertices()) {
    		fileWriter.write("\t" + v.getXCoordinate() + " " + v.getYCoordinate() + " " + v.getZCoordinate() + "; ...\n");
    		this.nodesIDs.put(v.getId(), idx++);
    	}
    	for(FaceNode n : this.graph.debugFaces) { //this.graph.getFaces()
    		fileWriter.write("\t" + n.getXCoordinate() + " " + n.getYCoordinate() + " " + n.getZCoordinate() + "; ...\n");
    		this.nodesIDs.put(n.getId(), idx++);
    	}
		/*for(InteriorNode in : this.graph.getInteriorNodes()) {
			fileWriter.write("\t" + in.getXCoordinate() + " " + in.getYCoordinate() + " " + in.getZCoordinate() + "; ...\n");
			this.nodesIDs.put(in.getId(), idx++);
		}*/
    	fileWriter.write("];\n\n");
    }
    
    private void writePlottingNodes(FileWriter fileWriter) throws IOException{
    	fileWriter.write("grey = [.25 .25 .25];\n");
    	fileWriter.write("scatter3(nodes(:,1),nodes(:,2),nodes(:,3),'filled','MarkerFaceColor',grey)\n");
    	fileWriter.write("axis vis3d\n");
    	fileWriter.write("grid off\n");
    	fileWriter.write("box on\n\n");
    }
    
    private void writeNormalEdges(FileWriter fileWriter) throws IOException {
    	writeNormalEdgesSources(fileWriter);
    	writeNormalEdgesEndings(fileWriter);
    }
    
    private void writeNormalEdgesSources(FileWriter fileWriter) throws IOException {
    	fileWriter.write("s = [");
    	 for(GraphEdge edge : this.graph.getEdges()) {
    		 if(edge.getNode0().getId().startsWith("V") && edge.getNode1().getId().startsWith("V")) {
    			 fileWriter.write(this.nodesIDs.get(edge.getNode0().getId()).toString() + " ");
    		 }
    	}
    	fileWriter.write("];\n");
    }
    
    private void writeNormalEdgesEndings(FileWriter fileWriter) throws IOException {
    	fileWriter.write("e = [");
    	int normalEdgesNumber = 0;
    	 for(GraphEdge edge : this.graph.getEdges()) {
    		 if(edge.getNode0().getId().startsWith("V") && edge.getNode1().getId().startsWith("V")) {
    			 fileWriter.write(this.nodesIDs.get(edge.getNode1().getId()).toString() + " ");
    			 normalEdgesNumber++;
    		 }
    	}
    	fileWriter.write("];\n");
    	fileWriter.write("n = nan(1," + normalEdgesNumber + ");\n\n");
    }
    
    private void writeFaceEdges(FileWriter fileWriter) throws IOException {
    	writeFaceEdgesSources(fileWriter);
    	writeFaceEdgesEndings(fileWriter);
    }
    
    private void writeFaceEdgesSources(FileWriter fileWriter) throws IOException {
    	fileWriter.write("s = [");
    	 for(GraphEdge edge : this.graph.getEdges()) {
    		 if(edge.getNode0().getId().startsWith("F") || edge.getNode1().getId().startsWith("F")) {
    			 fileWriter.write(this.nodesIDs.get(edge.getNode0().getId()).toString() + " ");
    		 }
    	}
    	fileWriter.write("];\n");
    }
    
    private void writeFaceEdgesEndings(FileWriter fileWriter) throws IOException {
    	fileWriter.write("e = [");
    	int faceEdgesNumber = 0;
    	 for(GraphEdge edge : this.graph.getEdges()) {
    		 if(edge.getNode0().getId().startsWith("F") || edge.getNode1().getId().startsWith("F")) {
    			 fileWriter.write(this.nodesIDs.get(edge.getNode1().getId()).toString() + " ");
    			 faceEdgesNumber++;
    		 }
    	}
    	fileWriter.write("];\n");
    	fileWriter.write("n = nan(1," + faceEdgesNumber + ");\n\n");
    }

	private void writeInteriorEdges(FileWriter fileWriter) throws IOException {
		writeInteriorEdgesSources(fileWriter);
		writeInteriorEdgesEndings(fileWriter);
	}

	private void writeInteriorEdgesSources(FileWriter fileWriter) throws IOException {
		fileWriter.write("s = [");
		for(GraphEdge edge : this.graph.getEdges()) {
			if(edge.getNode0().getId().startsWith("I") || edge.getNode1().getId().startsWith("I")) {
				fileWriter.write(this.nodesIDs.get(edge.getNode0().getId()).toString() + " ");
			}
		}
		fileWriter.write("];\n");
	}

	private void writeInteriorEdgesEndings(FileWriter fileWriter) throws IOException {
		fileWriter.write("e = [");
		int interiorEdgesNumber = 0;
		for(GraphEdge edge : this.graph.getEdges()) {
			if(edge.getNode0().getId().startsWith("I") || edge.getNode1().getId().startsWith("I")) {
				fileWriter.write(this.nodesIDs.get(edge.getNode1().getId()).toString() + " ");
				interiorEdgesNumber++;
			}
		}
		fileWriter.write("];\n");
		fileWriter.write("n = nan(1," + interiorEdgesNumber + ");\n\n");
	}
    
    private static void writePlottingEdges(FileWriter fileWriter, String color) throws IOException{
    	if(color.equals("red")) {
        	fileWriter.write("color = [1 0 0];\n");
    	}
    	else if(color.equals("green")){
			fileWriter.write("color = [0 1 0];\n");
		}
    	else {
    		fileWriter.write("color = [0.25 0.25 0.25];\n");
    	}
    	fileWriter.write("lx = [nodes(s',1)'; nodes(e',1)'; n];\n");
    	fileWriter.write("ly = [nodes(s',2)'; nodes(e',2)'; n];\n");
    	fileWriter.write("lz = [nodes(s',3)'; nodes(e',3)'; n];\n");
    	fileWriter.write("l = line(lx(:),ly(:),lz(:),'Color',color);\n\n");
    }
}