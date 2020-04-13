package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.javatuples.Pair;
import org.javatuples.Triplet;

public class Transformator{
	
	public static ModelGraph makeP4(ModelGraph tetrahedra, String startOppositeVertexID) {
		checkTetrahedra(tetrahedra);
		tetrahedra = breakFace(tetrahedra, startOppositeVertexID);
//		tetrahedra = breakFace(tetrahedra, startOppositeVertexID);
		return tetrahedra;
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
	
	// todo. Really brake faces
	private static ModelGraph breakFace(ModelGraph tetrahedra, String oppositeVertexID) {
//		System.out.println(oppositeVertexID);
		ArrayList<Vertex> vID = new ArrayList<Vertex>();
		for(Vertex vertex : tetrahedra.getVertices()) {
			if(oppositeVertexID.equals(vertex.getId())) {
				continue;
			}
			vID.add(vertex);
//			System.out.println("added vertex to brought face: " + vertex.getId());
		}
		GraphEdge e01, e02, e12;
		double e01len, e02len, e12len;
		
		e01 = tetrahedra.getEdgeNotOptional(vID.get(0), vID.get(1));
		e01len = e01.getLength();
		e02 = tetrahedra.getEdgeNotOptional(vID.get(0), vID.get(2));
		e02len = e02.getLength();
		e12 = tetrahedra.getEdgeNotOptional(vID.get(1), vID.get(2));
		e12len = e12.getLength();
		
		if(e01len > e02len && e01len > e12len) {
			tetrahedra = addEdge(tetrahedra, vID.get(2), e01);
			Triplet<Vertex, Vertex, Vertex> triangle = new Triplet<Vertex, Vertex, Vertex>(
					tetrahedra.getVertexNonOptional(oppositeVertexID),
					vID.get(0),
					vID.get(1)
					);
			FaceNode face = tetrahedra.getFace(triangle);
			face.setR(true);
		}else if(e02len > e12len) {
			tetrahedra = addEdge(tetrahedra, vID.get(1), e02);
			Triplet<Vertex, Vertex, Vertex> triangle = new Triplet<Vertex, Vertex, Vertex>(
					tetrahedra.getVertexNonOptional(oppositeVertexID),
					vID.get(0),
					vID.get(2)
					);
			FaceNode face = tetrahedra.getFace(triangle);
			face.setR(true);
		}else {
			tetrahedra = addEdge(tetrahedra, vID.get(0), e12);
			Triplet<Vertex, Vertex, Vertex> triangle = new Triplet<Vertex, Vertex, Vertex>(
					tetrahedra.getVertexNonOptional(oppositeVertexID),
					vID.get(1),
					vID.get(2)
					);
			FaceNode face = tetrahedra.getFace(triangle);
			face.setR(true);
		}
		
		return tetrahedra;
	}
	
	// todo set proper vertex name and edgeName
	private static ModelGraph addEdge(ModelGraph modelGraph, Vertex vertex, GraphEdge edge) {
		Vertex newVertex = modelGraph.insertVertexAutoNamed(edge.getMiddlePointCoordinates());
		
		modelGraph.insertEdgeAutoNamed(vertex, newVertex, false);
		
		modelGraph.deleteEdge(edge.getId());
		modelGraph.insertEdgeAutoNamed(edge.getEdgeNodes().getValue0(), newVertex, true);
		modelGraph.insertEdgeAutoNamed(edge.getEdgeNodes().getValue1(), newVertex, true);
		
		Triplet<Vertex, Vertex, Vertex> triangle = new Triplet<>(vertex, (Vertex)edge.getEdgeNodes().getValue0(), (Vertex)edge.getEdgeNodes().getValue1());
		modelGraph = removeFace(modelGraph, triangle);
		
        modelGraph.insertFaceAutoNamed(vertex, newVertex, (Vertex)edge.getEdgeNodes().getValue0());
        modelGraph.insertFaceAutoNamed(vertex, newVertex, (Vertex)edge.getEdgeNodes().getValue1());
		
		return modelGraph;
	}
	
	private static ModelGraph removeFace(ModelGraph modelGraph, Triplet<Vertex, Vertex, Vertex> triangle) {
		String idToRemove = "";
		FaceNode face = modelGraph.getFace(triangle);
//		for(FaceNode face : modelGraph.getFaces()) {
//			Triplet<Vertex, Vertex, Vertex> t = face.getTriangle();
//			if(t.contains(triangle.getValue0()) && t.contains(triangle.getValue1()) && t.contains(triangle.getValue2())) {
//				idToRemove = face.getId();
//				break;
//			}
//		}
		modelGraph.removeFace(face.getId());
		return modelGraph;
	}
}