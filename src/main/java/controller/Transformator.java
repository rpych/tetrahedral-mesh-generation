package controller;

import model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.javatuples.Pair;
import org.javatuples.Triplet;

public class Transformator{
	public ModelGraph graph;
	
	public Transformator(ModelGraph graph) {
		this.graph = graph;
	}
	
	public ModelGraph transform() {
		return transform(this.graph);
	}
	
	public ModelGraph transform(ModelGraph graph) {
		FaceNode face = findFaceToBreak(graph);
		while(face != null) {
			graph = breakFace(graph, face);
			face = findFaceToBreak(graph);
		}
		return graph;
	}
	
	private FaceNode findFaceToBreak(ModelGraph graph) {
		for(FaceNode face : graph.getFaces()) {
			if(face.isR()) {
				return face;
			}
		}
		return null;
	}
	
	// to correct
	private ModelGraph breakFace(ModelGraph graph, FaceNode face) {
		Pair<Vertex, Vertex> vertexes = findLongestFaceEdge(graph, face);
		if(graph.isEdgeBetween(vertexes.getValue0(), vertexes.getValue1())){
			Vertex vForNewEdge= getVertexForNewEdge(face, vertexes);
			System.out.println("Edge will be deleted");
			GraphEdge eToSplit = graph.getEdgeNotOptional(vertexes.getValue0(), vertexes.getValue1());
			graph = addEdge(graph, vForNewEdge, eToSplit);
		} else {
			Pair<Vertex, Vertex> newEdgeVertexes = findNewEdgeVertexes(graph, face);
			
			graph.insertEdgeAutoNamed(newEdgeVertexes.getValue0(), newEdgeVertexes.getValue1(), false);
			
			Triplet<Vertex, Vertex, Vertex> triangle = face.getTriangle();
			graph = removeFace(graph, triangle);
			if(triangle.getValue0().getId() != newEdgeVertexes.getValue0().getId() &&
					triangle.getValue0().getId() != newEdgeVertexes.getValue1().getId()) {
				graph.insertFaceAutoNamed(newEdgeVertexes.getValue0(), newEdgeVertexes.getValue1(), triangle.getValue0());
			}
			if(triangle.getValue1().getId() != newEdgeVertexes.getValue0().getId() &&
					triangle.getValue1().getId() != newEdgeVertexes.getValue1().getId()) {
				graph.insertFaceAutoNamed(newEdgeVertexes.getValue0(), newEdgeVertexes.getValue1(), triangle.getValue1());
			}
			if(triangle.getValue2().getId() != newEdgeVertexes.getValue0().getId() &&
					triangle.getValue2().getId() != newEdgeVertexes.getValue1().getId()) {
				graph.insertFaceAutoNamed(newEdgeVertexes.getValue0(), newEdgeVertexes.getValue1(), triangle.getValue2());
			}
			
		}
		
		graph = markFacesToRefinement(graph);

		return graph;
	}
	
	//todo: if two or three edges are equal then return that with hanging node
	private Pair<Vertex, Vertex> findLongestFaceEdge(ModelGraph graph, FaceNode face) {
		Vertex v0, v1, v2;
		double len01, len02, len12;
		v0 = face.getTriangle().getValue0();
		v1 = face.getTriangle().getValue1();
		v2 = face.getTriangle().getValue2();
		len01 = Coordinates.distance(v0.getCoordinates(), v1.getCoordinates());
		len02 = Coordinates.distance(v0.getCoordinates(), v2.getCoordinates());
		len12 = Coordinates.distance(v1.getCoordinates(), v2.getCoordinates());
		if(len01 >= len02 && len01 >= len12) {
			return new Pair<Vertex, Vertex>(v0, v1);
		} else if(len02 >= len01 && len02 >= len12) {
			return new Pair<Vertex, Vertex>(v0, v2);
		}
		return new Pair<Vertex, Vertex>(v1, v2);
	}
	
	private Pair<Vertex, Vertex> findNewEdgeVertexes(ModelGraph graph, FaceNode face){
		Vertex v0, v1, v2;
		GraphEdge e01, e02, e12;
		double e01len, e02len, e12len;
		boolean wasEdge01 = true, wasEdge02 = true, wasEdge12 = true;
		v0 = face.getTriangle().getValue0();
		v1 = face.getTriangle().getValue1();
		v2 = face.getTriangle().getValue2();
		if(!graph.isEdgeBetween(v0, v1)) {
			graph.insertEdgeAutoNamed(v0, v1, false);
			wasEdge01 = false;
		}
		if(!graph.isEdgeBetween(v0, v2)) {
			graph.insertEdgeAutoNamed(v0, v2, false);
			wasEdge02 = false;
		}
		if(!graph.isEdgeBetween(v1, v2)) {
			graph.insertEdgeAutoNamed(v1, v2, false);
			wasEdge12 = false;
		}
		e01 = graph.getEdgeNotOptional(v0, v1);
		e01len = e01.getLength();
		e02 = graph.getEdgeNotOptional(v0, v2);
		e02len = e02.getLength();
		e12 = graph.getEdgeNotOptional(v1, v2);
		e12len = e12.getLength();
		if(e01len > e02len && e01len > e12len) {
			Coordinates coordinates = e01.getMiddlePointCoordinates();
			String id = graph.buildVertexName(coordinates);
			return new Pair<Vertex, Vertex>(v2, graph.getVertexNonOptional(id));
		}
		if(e02len > e12len) {
			Coordinates coordinates = e02.getMiddlePointCoordinates();
			String id = graph.buildVertexName(coordinates);
			return new Pair<Vertex, Vertex>(v1, graph.getVertexNonOptional(id));
		}
		Coordinates coordinates = e12.getMiddlePointCoordinates();
		String id = graph.buildVertexName(coordinates);
		return new Pair<Vertex, Vertex>(v0, graph.getVertexNonOptional(id));
	}
	
	private Vertex getVertexForNewEdge(FaceNode face, Pair<Vertex, Vertex> vertexes){
		Triplet<Vertex, Vertex, Vertex> triangle = face.getTriangle();
		if(!triangle.getValue0().getId().equals(vertexes.getValue0().getId()) &&
				!triangle.getValue0().getId().equals(vertexes.getValue1().getId())) {
			return triangle.getValue0();
		}else if(!triangle.getValue1().getId().equals(vertexes.getValue0().getId()) &&
				!triangle.getValue1().getId().equals(vertexes.getValue1().getId())) {
			return triangle.getValue1();
		}
		return triangle.getValue2();
	}
	
	// todo set proper vertex name and edgeName
	private ModelGraph addEdge(ModelGraph modelGraph, Vertex vertex, GraphEdge edge) {
		Vertex newVertex = modelGraph.insertVertexAutoNamed(edge.getMiddlePointCoordinates());
		
		modelGraph.insertEdgeAutoNamed(vertex, newVertex, false);
		
		System.out.println("\nInside addEdge");
		for(GraphEdge e : modelGraph.getEdges()) {
			System.out.println(e.getNode0().getId() + " " + e.getNode1().getId());
		}
		System.out.println(edge.getId() + " will be deleted");
		modelGraph.deleteEdge(edge.getId());
		for(GraphEdge e : modelGraph.getEdges()) {
			System.out.println(e.getNode0().getId() + " " + e.getNode1().getId());
		}
		modelGraph.insertEdgeAutoNamed(edge.getEdgeNodes().getValue0(), newVertex, true);
		modelGraph.insertEdgeAutoNamed(edge.getEdgeNodes().getValue1(), newVertex, true);
		
		Triplet<Vertex, Vertex, Vertex> triangle = new Triplet<>(vertex, (Vertex)edge.getEdgeNodes().getValue0(), (Vertex)edge.getEdgeNodes().getValue1());
		modelGraph = removeFace(modelGraph, triangle);
		
        modelGraph.insertFaceAutoNamed(vertex, newVertex, (Vertex)edge.getEdgeNodes().getValue0());
        modelGraph.insertFaceAutoNamed(vertex, newVertex, (Vertex)edge.getEdgeNodes().getValue1());
		
        System.out.println("\nAt the end of addEdge");
		for(GraphEdge e : modelGraph.getEdges()) {
			System.out.println(e.getNode0().getId() + " " + e.getNode1().getId());
		}
        
		return modelGraph;
	}
	
	private ModelGraph removeFace(ModelGraph modelGraph, Triplet<Vertex, Vertex, Vertex> triangle) {
		FaceNode face = modelGraph.getFace(triangle);
		modelGraph.removeFace(face.getId());
		return modelGraph;
	}
	
	private ModelGraph markFacesToRefinement(ModelGraph graph) {
		for(FaceNode faceNode : graph.getFaces()) {
			if(!graph.areVertexesLinked(faceNode)) {
				faceNode.setR(true);
			}
		}
		return graph;
	}
	
	// FUNCTIONS NOT USED FOR NOW. MAYBE THEY WONT BE USED AT ALL.
	//todo subfunctions
	private void checkTetrahedra(ModelGraph modelGraph){
		String commonError = "\nGiven ModelGraph for makeP4 is not correct tetrahedra!!!\n";
		if(!hasFourVertexes(modelGraph)) {
			throw new IllegalArgumentException(commonError + "Graph has not four vertexes");
		}
		if(!hasCorrectEdges(modelGraph)) {
			throw new IllegalArgumentException(commonError + "Graph has not correct edges");
		}
		if(!hasCorrectFaces(modelGraph)) {
			throw new IllegalArgumentException(commonError + "Graph has not correct faces");
		}
	}
	
	// todo - check if vertexes are different
	private boolean hasFourVertexes(ModelGraph modelGraph) {
		return 4 == modelGraph.getVertices().size();
	}
	
	//todo
	private boolean hasCorrectEdges(ModelGraph modelGraph) {
		if(6 + 3 * 4 != modelGraph.getEdges().size()) {
//				for(GraphEdge edge : modelGraph.getEdges()) {
//					System.out.println(edge.getEdgeNodes());
//				}
//				System.out.println(modelGraph.getEdges().size());
			return false;
		}
//			Collection<Vertex> vertices = (Collection<Vertex>)modelGraph.getVertices();
//			System.out.println(vertices.size());
//			return null != modelGraph.getEdge(vertices., vertices.get(1)) &&
//					null != modelGraph.getEdge(vertices.get(0), vertices.get(2)) &&
//					null != modelGraph.getEdge(vertices.get(0), vertices.get(3)) &&
//					null != modelGraph.getEdge(vertices.get(1), vertices.get(2)) &&
//					null != modelGraph.getEdge(vertices.get(1), vertices.get(3)) &&
//					null != modelGraph.getEdge(vertices.get(2), vertices.get(3));
		
		return true;
	}
	
	//todo - check faces positions
	private boolean hasCorrectFaces(ModelGraph modelGraph) {
		if(4 != modelGraph.getFaces().size()) {
			return false;
		}
		return true;
	}
}