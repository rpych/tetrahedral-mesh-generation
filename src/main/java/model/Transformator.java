package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.javatuples.Pair;
import org.javatuples.Triplet;

public class Transformator{
	
	public static ModelGraph makeP4(ModelGraph tetrahedra) {
		checkTetrahedra(tetrahedra);
		FaceNode face = findFaceToBreak(tetrahedra);
//		tetrahedra = breakFace(tetrahedra, face);
//		face = findFaceToBreak(tetrahedra);
//		tetrahedra = breakFace(tetrahedra, face);
		while(face != null) {
			tetrahedra = breakFace(tetrahedra, face);
	        System.out.println("\nInside while:");
			for(GraphEdge e : tetrahedra.getEdges()) {
				System.out.println(e.getNode0().getId() + " " + e.getNode1().getId());
			}
			face = findFaceToBreak(tetrahedra);
	        System.out.println("\nInside while2:");
			for(GraphEdge e : tetrahedra.getEdges()) {
				System.out.println(e.getNode0().getId() + " " + e.getNode1().getId());
			}
		}
		return tetrahedra;
	}
	
	public static ModelGraph makeP4Grid(ModelGraph grid) {
//		checkTetrahedra(tetrahedra);
		FaceNode face = findFaceToBreak(grid);
		grid = breakFace(grid, face);
		face = findFaceToBreak(grid);
		grid = breakFace(grid, face);
//		while(face != null) {
//			tetrahedra = breakFace(tetrahedra, face);
//			face = findFaceToBreak(tetrahedra);
//		}
		return grid;
	}
	
	//todo subfunctions
	private static void checkTetrahedra(ModelGraph modelGraph){
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
	private static boolean hasFourVertexes(ModelGraph modelGraph) {
		return 4 == modelGraph.getVertices().size();
	}
	
	//todo
	private static boolean hasCorrectEdges(ModelGraph modelGraph) {
		if(6 + 3 * 4 != modelGraph.getEdges().size()) {
//			for(GraphEdge edge : modelGraph.getEdges()) {
//				System.out.println(edge.getEdgeNodes());
//			}
//			System.out.println(modelGraph.getEdges().size());
			return false;
		}
//		Collection<Vertex> vertices = (Collection<Vertex>)modelGraph.getVertices();
//		System.out.println(vertices.size());
//		return null != modelGraph.getEdge(vertices., vertices.get(1)) &&
//				null != modelGraph.getEdge(vertices.get(0), vertices.get(2)) &&
//				null != modelGraph.getEdge(vertices.get(0), vertices.get(3)) &&
//				null != modelGraph.getEdge(vertices.get(1), vertices.get(2)) &&
//				null != modelGraph.getEdge(vertices.get(1), vertices.get(3)) &&
//				null != modelGraph.getEdge(vertices.get(2), vertices.get(3));
		
		return true;
	}
	
	//todo - check faces positions
	private static boolean hasCorrectFaces(ModelGraph modelGraph) {
		if(4 != modelGraph.getFaces().size()) {
			return false;
		}
		return true;
	}
	
	private static FaceNode findFaceToBreak(ModelGraph tetrahedra) {
		for(FaceNode face : tetrahedra.getFaces()) {
			if(face.isR()) {
				return face;
			}
		}
		return null;
	}
	
	//totest
	private static ModelGraph breakFace(ModelGraph graph, FaceNode face) {
		GraphEdge eToSplit= findLongestFaceEdge(graph, face);
		if(null != eToSplit) {
//			System.out.println(eToSplit.getId());
			Vertex vForNewEdge= getVertexForNewEdge(face, eToSplit);
//			System.out.println(vForNewEdge.getId());
			System.out.println("Edge will be deleted");
			graph = addEdge(graph, vForNewEdge, eToSplit);
		}else {
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
//		System.out.println("\nAfter breaking face, setting faces to refinement:");
		for(FaceNode faceNode : graph.getFaces()) {
//			System.out.println(faceNode.getId() + " " + graph.areVertexesLinked(faceNode));
			if(!graph.areVertexesLinked(faceNode)) {
				faceNode.setR(true);
			}
		}

		return graph;
	}
	
	// toCorrect
	private static GraphEdge findLongestFaceEdge(ModelGraph graph, FaceNode face) {
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
		if(!wasEdge01) {
			graph.removeEdge(e01.getId());
		}
		if(!wasEdge02) {
			graph.removeEdge(e02.getId());
		}
		if(!wasEdge12) {
			graph.removeEdge(e12.getId());
		}
		if(e01len > e02len && e01len > e12len) {
			if(wasEdge01) {
				return e01;			
			}
			return null;
		}
		if(e02len > e12len) {
			if(wasEdge02) {
				return e02;
			}
			return null;
		}
		if(wasEdge12) {
			return e12;
		}
		return null;
	}
	
	private static Pair<Vertex, Vertex> findNewEdgeVertexes(ModelGraph graph, FaceNode face){
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
	
	private static Vertex getVertexForNewEdge(FaceNode face, GraphEdge eToSplit){
		Pair<GraphNode, GraphNode> edgeNodes = eToSplit.getEdgeNodes();
		Triplet<Vertex, Vertex, Vertex> triangle = face.getTriangle();
		
		String vId = triangle.getValue0().getId();
//		System.out.println(vId);
//		System.out.println("edge first vertex id: " + edgeNodes.getValue0().getId() + ", edge second vertex id: " + edgeNodes.getValue1().getId());
		
		if(!vId.equals(edgeNodes.getValue0().getId()) && !vId.equals(edgeNodes.getValue1().getId())) {
			return triangle.getValue0();
		}
		vId = triangle.getValue1().getId();
//		System.out.println(vId);
		if(!vId.equals(edgeNodes.getValue0().getId()) && !vId.equals(edgeNodes.getValue1().getId())) {
			return triangle.getValue1();
		}
//		System.out.println(vId);
		return triangle.getValue2();
	}
	
	// todo set proper vertex name and edgeName
	private static ModelGraph addEdge(ModelGraph modelGraph, Vertex vertex, GraphEdge edge) {
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
	
	private static ModelGraph removeFace(ModelGraph modelGraph, Triplet<Vertex, Vertex, Vertex> triangle) {
		FaceNode face = modelGraph.getFace(triangle);
		modelGraph.removeFace(face.getId());
		return modelGraph;
	}
}