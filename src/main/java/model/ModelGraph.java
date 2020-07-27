package model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.ElementAttributes;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.javatuples.Triplet;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class ModelGraph extends MultiGraph {

    private Map<String, Vertex> vertices = new HashMap<>();

    private Map<String, FaceNode> faces = new HashMap<>();

    private Map<String, GraphEdge> edges = new HashMap<>();

    public ModelGraph(String id) {
        super(id);
    }

    public ModelGraph(ModelGraph graph) {
        super(graph.id + 1);
        graph.vertices.values().forEach(this::insertVertex);
        graph.faces.values().forEach(this::insertFace);
        graph.edges.values().forEach(this::insertEdge);
    }

    public Optional<GraphEdge> getEdgeBetweenNodes(Vertex v1, Vertex v2) {
        Optional<Edge> edge = Optional.ofNullable(v1.getEdgeBetween(v2));
        return edge.map(Element::getId).flatMap(this::getEdgeById);
    }

    public Vertex insertVertex(Vertex vertex) {
        Node node = this.addNode(vertex.getId());
        node.setAttribute(ElementAttributes.FROZEN_LAYOUT);
        node.setAttribute(ElementAttributes.XYZ, vertex.getXCoordinate(), vertex.getYCoordinate(), vertex.getZCoordinate());
        vertices.put(vertex.getId(), vertex);
        return vertex;
    }

    public Vertex insertVertex(String id, Coordinates coordinates) {
        Vertex vertex = new Vertex.VertexBuilder(this, id)
                .setCoordinates(coordinates)
                .build();
        insertVertex(vertex);
        return vertex;
    }
    
    public String buildVertexName(Coordinates coordinates) {
    	String vertexName = "V_" + String.format ("%.2f", coordinates.getX()) + "_"
				+ String.format ("%.2f", coordinates.getY()) + "_"
				+ String.format ("%.2f", coordinates.getZ());
    	return vertexName;
    }
    
    public Vertex insertVertexAutoNamed(Coordinates coordinates) {
		String vertexName = buildVertexName(coordinates);
		return insertVertex(vertexName, coordinates);
    }

    public Optional<Vertex> removeVertex(String id) {
        Vertex vertex = vertices.remove(id);
        if (vertex != null) {
            this.removeVertex(id);
            faces.entrySet().stream()
                    .filter(face -> face.getValue().getTriangleVertices().contains(vertex))
                    .forEach(result -> removeFace(result.getKey()));
            edges.values().stream()
                    .filter(graphEdge -> graphEdge.getEdgeNodes().contains(vertex))
                    .map(GraphEdge::getId)
                    .forEach(this::removeEdge);
            return Optional.of(vertex);
        }
        return Optional.empty();
    }

    public FaceNode insertFace(FaceNode faceNode) {
        Node node = this.addNode(faceNode.getId());
        node.setAttribute(ElementAttributes.FROZEN_LAYOUT);
        node.setAttribute(ElementAttributes.XYZ, faceNode.getXCoordinate(), faceNode.getYCoordinate(), faceNode.getZCoordinate());
        node.addAttribute("ui.style", "fill-color: red;");
        faces.put(id, faceNode);
        return faceNode;
    }

    public FaceNode insertFace(String id, Coordinates coordinates) {
        FaceNode faceNode = new FaceNode(this, id, coordinates);
        return insertFace(faceNode);
    }

    public FaceNode insertFace(String id, Vertex v1, Vertex v2, Vertex v3) {
        FaceNode faceNode = new FaceNode(this, id, v1, v2, v3);
        Node node = this.addNode(faceNode.getId());
        node.setAttribute(ElementAttributes.FROZEN_LAYOUT);
        node.setAttribute(ElementAttributes.XYZ, faceNode.getXCoordinate(), faceNode.getYCoordinate(), faceNode.getZCoordinate());
        node.addAttribute("ui.class", "important");
        faces.put(id, faceNode);
        insertEdge(id.concat(v1.getId()), faceNode, v1, false, "fill-color: blue;");
        insertEdge(id.concat(v2.getId()), faceNode, v2, false, "fill-color: blue;");
        insertEdge(id.concat(v3.getId()), faceNode, v3, false, "fill-color: blue;");
        return faceNode;
    }

    public FaceNode insertFaceAutoNamed(Vertex v1, Vertex v2, Vertex v3) {
    	Coordinates  coordinates = new Coordinates(
    			(v1.getXCoordinate() + v2.getXCoordinate() + v3.getXCoordinate()) / 3d,
    			(v1.getYCoordinate() + v2.getYCoordinate() + v3.getYCoordinate()) / 3d,
    			(v1.getZCoordinate() + v2.getZCoordinate() + v3.getZCoordinate()) / 3d
    			);
    	String id = "F_" + String.format ("%.2f", coordinates.getX()) + "_"
				+ String.format ("%.2f", coordinates.getY()) + "_"
				+ String.format ("%.2f", coordinates.getZ());
//    	System.out.println(id);
        return insertFace(id, v1, v2, v3);
    }
    
    public void removeFace(String id) {
        List<String> edgesToRemove = edges.values().stream()
                .filter(graphEdge -> graphEdge.getEdgeNodes().contains(faces.get(id)))
                .map(GraphEdge::getId)
                .collect(Collectors.toList());
        edgesToRemove.forEach(this::deleteEdge);
        faces.remove(id);
        this.removeNode(id);
    }

    public GraphEdge insertEdge(String id, GraphNode n1, GraphNode n2, boolean border) {
        return insertEdge(id, n1, n2, border, null);
    }
    
    public GraphEdge insertEdgeAutoNamed(GraphNode n1, GraphNode n2, boolean border) {
    	String edgeName = "E" + n1.getId() + "to" + n2.getId();
        return insertEdge(edgeName, n1, n2, border, null);
    }

    public GraphEdge insertEdge(String id, GraphNode n1, GraphNode n2, boolean border, String uiStyle) {
        GraphEdge graphEdge = new GraphEdge.GraphEdgeBuilder(id, n1, n2, border).build();
        Edge edge = this.addEdge(graphEdge.getId(), n1, n2);
        if (uiStyle != null) {
            edge.addAttribute("ui.style", uiStyle);
        }
        edges.put(graphEdge.getId(), graphEdge);
        return graphEdge;
    }

    public GraphEdge insertEdge(GraphEdge graphEdge) {
        Edge edge = this.addEdge(graphEdge.getId(), graphEdge.getNode0().getId(), graphEdge.getNode1().getId());
        edges.put(graphEdge.getId(), graphEdge);
        return graphEdge;
    }

    public void deleteEdge(GraphNode n1, GraphNode n2) {
        Edge edge = n1.getEdgeBetween(n2);
        deleteEdge(edge.getId());
    }

    public void deleteEdge(String edgeId){
        edges.remove(edgeId);
        this.removeEdge(edgeId);
    }

    public Optional<GraphEdge> getEdgeById(String id) {
        return Optional.ofNullable(edges.get(id));
    }

    public List<Vertex> getVerticesBetween(Vertex beginning, Vertex end) {
        if(beginning.getEdgeBetween(end) != null){
            return new LinkedList<>();
        }
        return this.vertices
                .values()
                .stream()
                .filter(v -> isVertexBetween(v, beginning, end))
                .collect(Collectors.toList());
    }

    public Optional<Vertex> getVertexBetween(Vertex beginning, Vertex end) {
        return this.getVerticesBetween(beginning, end).stream().findFirst();
    }

    public GraphEdge getTriangleLongestEdge(FaceNode faceNode){
        Triplet<Vertex, Vertex, Vertex> triangle = faceNode.getTriangle();
        Vertex v1 = triangle.getValue0();
        Vertex v2 = triangle.getValue1();
        Vertex v3 = triangle.getValue2();

        GraphEdge edge1 = getEdgeBetweenNodes(v1, v2)
                .orElseThrow(() -> new RuntimeException("Unknown edge id"));
        GraphEdge edge2 = getEdgeBetweenNodes(v2, v3)
                .orElseThrow(() -> new RuntimeException("Unknown edge id"));
        GraphEdge edge3 = getEdgeBetweenNodes(v1, v3)
                .orElseThrow(() -> new RuntimeException("Unknown edge id"));

        if(edge1.getLength() >= edge2.getLength() && edge1.getLength() >= edge3.getLength()) {
            return edge1;
        }else if(edge2.getLength() >= edge3.getLength()) {
            return edge2;
        }
        return edge3;
    }

    public Optional<Vertex> getVertex(String id) {
        return Optional.ofNullable(vertices.get(id));
    }
    
    public Vertex getVertexNonOptional(String id) {
        return vertices.get(id);
    }

    public Collection<Vertex> getVertices() {
        return vertices.values();
    }

    public int getVerticesNum() { return vertices.size(); }

    public int getEdgesNum() { return edges.size(); }

    public int getFacesNum() { return faces.size(); }

    public Optional<FaceNode> getFace(String id) {
        return Optional.ofNullable(faces.get(id));
    }
    
    public FaceNode getFaceNonOptional(String id) {
        return faces.get(id);
    }
    
    public FaceNode getFace(Triplet<Vertex, Vertex, Vertex> triangle) {
    	for(FaceNode face : faces.values()) {
			Triplet<Vertex, Vertex, Vertex> t = face.getTriangle();
			if(t.contains(triangle.getValue0()) && t.contains(triangle.getValue1()) && t.contains(triangle.getValue2())) {
				return face;
			}
		}
    	return null;
    }

    public Collection<FaceNode> getFaces() {
        return faces.values();
    }

    public Optional<GraphEdge> getEdge(Vertex v1, Vertex v2) {
        return Optional.ofNullable(edges.get(v1.getEdgeBetween(v2).getId()));
    }
    
    /*public boolean isEdgeBetween(Vertex v1, Vertex v2) {
    	return null != v1.getEdgeBetween(v2);
    }*/

    public boolean isEdgeBetween(GraphNode v1, GraphNode v2) {
        return null != v1.getEdgeBetween(v2);
    }
    
    public GraphEdge getEdgeNotOptional(Vertex v1, Vertex v2) {
        return edges.get(v1.getEdgeBetween(v2).getId());
    }

    public Collection<GraphEdge> getEdges() {
        return edges.values();
    }
    
    public Collection<GraphEdge> getEdges(GraphNode node) {
        Collection<GraphEdge> adjacentEdges = new ArrayList<GraphEdge>();
    	for(GraphEdge edge : edges.values()) {
        	if(node == edge.getEdgeNodes().getValue0() ||
        	   node == edge.getEdgeNodes().getValue1()) {
        		adjacentEdges.add(edge);
        	}
        }
    	return adjacentEdges;
    }

    private boolean isVertexBetween(Vertex v, Vertex beginning, Vertex end) {
        double epsilon = .001;
        double xd = Math.abs(calculateInlineMatrixDeterminant(v, beginning, end));
        if(isVertexSameAs(v, beginning) || isVertexSameAs(v,end)){
            return false;
        } else return areCoordinatesMatching(v, beginning, end)
                && Math.abs(calculateInlineMatrixDeterminant(v, beginning, end)) < epsilon;
    }

    private boolean isVertexSameAs(Vertex a, Vertex b){
        return a.getCoordinates().equals(b.getCoordinates());
    }

    private boolean areCoordinatesMatching(Vertex v, Vertex beginning, Vertex end){
        return v.getXCoordinate() <= Math.max(beginning.getXCoordinate(), end.getXCoordinate())
                && v.getXCoordinate() >= Math.min(beginning.getXCoordinate(), end.getXCoordinate())
                && v.getYCoordinate() <= Math.max(beginning.getYCoordinate(), end.getYCoordinate())
                && v.getYCoordinate() >= Math.min(beginning.getYCoordinate(), end.getYCoordinate());
    }
    /*
    Basic matrix calculation to check if points are in line with each other
    The matrix looks like this:
    | a.x, a.y, a.z |
    | b.x, b.y, b.z |
    | c.x, c.y, c.z |

    so if we calculate det of that matrix and it is equal to 0 it means that all points are in straight line
     */
    private double calculateInlineMatrixDeterminant(Vertex v, Vertex beginning, Vertex end) {
        Coordinates a = v.getCoordinates();
        Coordinates b = beginning.getCoordinates();
        Coordinates c = end.getCoordinates();

        return a.getX()*b.getY()*c.getZ()
                + a.getY()*b.getZ()*c.getX()
                + a.getZ()*b.getX()*c.getY()
                - a.getZ()*b.getY()*c.getX()
                - a.getX()*b.getZ()*c.getY()
                - a.getY()*b.getX()*c.getZ();
    }

    public void rotate() {
        faces.values().forEach(GraphNode::rotate);
        vertices.values().forEach(GraphNode::rotate);
    }
    
    public boolean areVertexesLinked(FaceNode face) {
    	Triplet<Vertex, Vertex, Vertex> triangle = face.getTriangle();
    	return this.isEdgeBetween(triangle.getValue0(), triangle.getValue1()) &&
    			this.isEdgeBetween(triangle.getValue0(), triangle.getValue2()) &&
    			this.isEdgeBetween(triangle.getValue1(), triangle.getValue2());
    }
    
    public Collection<GraphEdge> getEdgesBetweenVertices(){
    	Collection<GraphEdge> ebv = new ArrayList<GraphEdge>();
    	Collection<GraphEdge> edges = this.getEdges();
    	for(GraphEdge edge : edges) {
    		if(edge.isBetweenVertices()) {
    			ebv.add(edge);
    		}
    	}
    	return ebv;
    }
    
    public Collection<Vertex> getCommonVertices(Vertex v1, Vertex v2){
    	Collection<Vertex> cv = new ArrayList<Vertex>();
    	Collection<GraphNode> nodes = v1.getAdjacentNodes(this);
    	for(GraphNode node : nodes) {
    		if(node instanceof Vertex && node.hasEdgeBetween(v2)) {
    			cv.add((Vertex)node);
    		}
    	}
    	return cv;
    }
    
    public boolean hasFaceNode(Vertex v1, Vertex v2, Vertex v3) {
		Set<Vertex> s = new HashSet<Vertex>();
		s.add(v1);
		s.add(v2);
		s.add(v3);
    	for(FaceNode f : faces.values()) {
    		Triplet<Vertex, Vertex, Vertex> traingle = f.getTriangle();
    		Set<Vertex> stmp = new HashSet<Vertex>();
    		stmp.add(traingle.getValue0());
    		stmp.add(traingle.getValue1());
    		stmp.add(traingle.getValue2());
    		if(s.equals(stmp)) {
    			return true;
    		}
    	}
    	return false;
    }
}
