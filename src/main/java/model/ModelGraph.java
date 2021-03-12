package model;

import common.IdFormatter;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

public class ModelGraph extends Graph {

    private Map<String, Vertex> vertices = new ConcurrentSkipListMap<>();

    private Map<String, FaceNode> faces = new ConcurrentSkipListMap<>();

    private Map<String, GraphEdge> edges = new ConcurrentSkipListMap<>();

    private Map<String, InteriorNode> interiorNodes = new ConcurrentSkipListMap<>();

    private Map<String, InteriorNode> interiorNodesOld = new ConcurrentSkipListMap<>();

    public List<FaceNode> debugFaces = new LinkedList<>();

    public Integer falseEdgesCounter = 0;

    public ModelGraph(String id) {
        super(id);
    }

    public ModelGraph(ModelGraph graph) {
        super(graph.id + 1);
        graph.vertices.values().forEach(this::insertVertex);
        graph.faces.values().forEach(this::insertFace);
        graph.interiorNodes.values().forEach(this::insertInteriorNode);
        graph.edges.values().forEach(this::insertEdge);
    }

    public Optional<GraphEdge> getEdgeBetweenNodes(Vertex v1, Vertex v2) {
        Optional<GraphEdge> edge = Optional.ofNullable(v1.getEdgeBetween(v2));
        return edge.map(GraphEdge::getId).flatMap(this::getEdgeById); //Element
    }

    public Vertex insertVertex(Vertex vertex) {
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
        String vertexName = "V_" + IdFormatter.df.format(coordinates.getX()) + "_"
                + IdFormatter.df.format(coordinates.getY()) + "_"
                + IdFormatter.df.format(coordinates.getZ());
    	return vertexName;
    }
    
    public Vertex insertVertexAutoNamed(Coordinates coordinates) {
		String vertexName = buildVertexName(coordinates);
		return insertVertex(vertexName, coordinates);
    }

    public Vertex insertVertexAutoNamedOrGet(Coordinates coordinates) {
        String vertexName = buildVertexName(coordinates);
        Optional<Vertex> optVertex = getVertex(vertexName);
        if(optVertex.isPresent())
            return optVertex.get();
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
                    .forEach(this::deleteEdge);
            return Optional.of(vertex);
        }
        return Optional.empty();
    }

    public FaceNode insertFace(FaceNode faceNode) {
        faces.put(id, faceNode);
        return faceNode;
    }

    public FaceNode insertFace(String id, Coordinates coordinates) {
        FaceNode faceNode = new FaceNode(this, id, coordinates);
        return insertFace(faceNode);
    }

    public FaceNode insertFace(String id, Vertex v1, Vertex v2, Vertex v3) {
        FaceNode faceNode = new FaceNode(this, id, v1, v2, v3);
        faces.put(id, faceNode);
        //insertEdge(id.concat(v1.getId()), faceNode, v1, false, "fill-color: blue;");
        //insertEdge(id.concat(v2.getId()), faceNode, v2, false, "fill-color: blue;");
        //insertEdge(id.concat(v3.getId()), faceNode, v3, false, "fill-color: blue;");
        falseEdgesCounter = falseEdgesCounter + 3;
        return faceNode;
    }

    public FaceNode insertFaceAutoNamed(Vertex v1, Vertex v2, Vertex v3) {
    	Coordinates  coordinates = new Coordinates(
    			(v1.getXCoordinate() + v2.getXCoordinate() + v3.getXCoordinate()) / 3d,
    			(v1.getYCoordinate() + v2.getYCoordinate() + v3.getYCoordinate()) / 3d,
    			(v1.getZCoordinate() + v2.getZCoordinate() + v3.getZCoordinate()) / 3d
    			);

        String id = "F_" + IdFormatter.df.format(coordinates.getX()) + "_"
                + IdFormatter.df.format(coordinates.getY()) + "_"
                + IdFormatter.df.format(coordinates.getZ());

        return insertFace(id, v1, v2, v3);
    }

    
    public void removeFace(String id) {
        List<String> edgesToRemove = edges.values().stream()
                .filter(graphEdge -> graphEdge.getEdgeNodes().contains(faces.get(id)))
                .map(GraphEdge::getId)
                .collect(Collectors.toList());
        edgesToRemove.forEach(this::deleteEdge);
        faces.remove(id);
    }

    public GraphEdge insertEdge(String id, GraphNode n1, GraphNode n2, boolean border) {
        return insertEdge(id, n1, n2, border, null);
    }
    
    public GraphEdge insertEdgeAutoNamed(GraphNode n1, GraphNode n2, boolean border) {
    	String edgeName = "E" + n1.getId() + "to" + n2.getId();
        return insertEdge(edgeName, n1, n2, border, null);
    }

    public GraphEdge insertEdgeAutoNamedOrGet(GraphNode n1, GraphNode n2, boolean border) {
        String edgeName = "E" + n1.getId() + "to" + n2.getId();
        Optional<GraphEdge> optEdge = getEdgeBetweenNodes((Vertex)n1, (Vertex)n2);
        if(optEdge.isPresent())
            return optEdge.get();
        return insertEdge(edgeName, n1, n2, border, null);
    }

    public GraphEdge insertEdge(String id, GraphNode n1, GraphNode n2, boolean border, String uiStyle) {
        GraphEdge graphEdge = new GraphEdge.GraphEdgeBuilder(id, n1, n2, border).build();
        return insertEdge(graphEdge);
    }

    public GraphEdge insertEdge(GraphEdge graphEdge) {
        graphEdge.getEdgeNodes().getValue0().addNeighbourEdge(graphEdge);
        graphEdge.getEdgeNodes().getValue1().addNeighbourEdge(graphEdge);
        edges.put(graphEdge.getId(), graphEdge);
        return graphEdge;
    }

    public void deleteEdge(GraphNode n1, GraphNode n2) {
        GraphEdge edge = n1.getEdgeBetween(n2);
        deleteEdge(edge.getId());
    }

    public void deleteEdge(String edgeId){
        GraphEdge graphEdge = edges.remove(edgeId);
        graphEdge.getEdgeNodes().getValue0().removeNeighbourEdge(graphEdge);
        graphEdge.getEdgeNodes().getValue1().removeNeighbourEdge(graphEdge);
        if(edgeId.contains("I") || edgeId.contains("F"))
            falseEdgesCounter -= 1;
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

    public Map<String, Vertex> getVerticesMap() {
        return vertices;
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

    //change on a.getId().equals(b.getId()) from a.getCoordinates().equals(b.getCoordinates());
    private boolean isVertexSameAs(Vertex a, Vertex b){
        return a.getId().equals(b.getId());
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

    public Optional<FaceNode> getNearestCongruentFace(FaceNode face, Collection<FaceNode> faces){
        double minDistance = 100000;
        FaceNode nearestFace = null;
        for(FaceNode faceOnBorder: faces){
            if(!face.equals(faceOnBorder)){
                Optional<Pair<Vertex, Vertex>> uncommonVertices = face.getUncommonVerticesIfCongruent(faceOnBorder);
                if(! uncommonVertices.isPresent()) continue;

                Vertex uv0 = uncommonVertices.get().getValue0();
                Vertex uv1 = uncommonVertices.get().getValue1();

                double distance = Coordinates.distance(uv0.getCoordinates(), uv1.getCoordinates());
                if(distance < minDistance){
                    minDistance = distance;
                    nearestFace = faceOnBorder;
                }
            }
        }
        return Optional.ofNullable(nearestFace);
    }

    //InteriorNode part

    public Collection<InteriorNode> getInteriorNodes() { return interiorNodes.values(); }

    public Map<String, InteriorNode> getInteriorNodesMap() { return interiorNodes; }

    public void clearInteriorNodes(){
        List<String> interiorNodesKeysCopy = new LinkedList<>(interiorNodes.keySet());
        for(String interiorId: interiorNodesKeysCopy){
            removeInteriorNode(interiorId);
        }
    }

    public InteriorNode insertInteriorNode(InteriorNode interiorNode) {
        interiorNodes.put(interiorNode.getId(), interiorNode);
        return interiorNode;
    }

    public InteriorNode insertInteriorNodeAutoNamed(GraphNode n1, GraphNode n2, GraphNode n3, GraphNode n4){
        //String nodeName = InteriorNode.INTERIOR_SYMBOL + n1.getId() + "_" + n2.getId() + "_" + n3.getId() + "_" + n4.getId();
        Coordinates  coordinates = new Coordinates(
                (n1.getXCoordinate() + n2.getXCoordinate() + n3.getXCoordinate() + n4.getXCoordinate()) / 4d,
                (n1.getYCoordinate() + n2.getYCoordinate() + n3.getYCoordinate() + n4.getYCoordinate()) / 4d,
                (n1.getZCoordinate() + n2.getZCoordinate() + n3.getZCoordinate() + n4.getZCoordinate()) / 4d
        );

        String nodeName = InteriorNode.INTERIOR_SYMBOL + IdFormatter.df.format(coordinates.getX()) + "_"
                + IdFormatter.df.format(coordinates.getY()) + "_"
                + IdFormatter.df.format(coordinates.getZ());
        //System.out.println(nodeName);
        return insertInteriorNode(nodeName, (Vertex)n1, (Vertex)n2, (Vertex)n3, (Vertex)n4, null);
    }

    public InteriorNode insertInteriorNode(String interiorNodeName, Vertex v1, Vertex v2, Vertex v3, Vertex v4, String uiStyle){
        InteriorNode interiorNode = new InteriorNode(this, interiorNodeName, v1, v2, v3, v4);
        if(!interiorNodes.containsKey(interiorNodeName))
            interiorNodes.put(interiorNodeName, interiorNode);

        /*insertEdge(interiorNodeName.concat(v1.getId()), interiorNode, v1, false, "fill-color: orange;");
        insertEdge(interiorNodeName.concat(v2.getId()), interiorNode, v2, false, "fill-color: orange;");
        insertEdge(interiorNodeName.concat(v3.getId()), interiorNode, v3, false, "fill-color: orange;");
        insertEdge(interiorNodeName.concat(v4.getId()), interiorNode, v4, false, "fill-color: orange;");*/
        falseEdgesCounter = falseEdgesCounter + 4;

        return interiorNode;
    }

    public void removeInteriorNode(String id) {
        List<String> edgesToRemove = edges.values().stream()
                .filter(graphEdge -> graphEdge.getEdgeNodes().contains(interiorNodes.get(id)))
                .map(GraphEdge::getId)
                .collect(Collectors.toList());
        edgesToRemove.forEach(this::deleteEdge);
        interiorNodes.remove(id);
    }

    //main method for InteriorNode
    public ModelGraph createInteriorNodesForNewlyFoundSubGraphs(){
        for(FaceNode face: this.getFaces() ){
            List<Quartet<Vertex, Vertex, Vertex, Vertex>> candSubGraphs = this.findVerticesWhichFormsCandSubGraph(face);
            for(Quartet<Vertex, Vertex, Vertex, Vertex> candSubGraph: candSubGraphs){
                if( candSubGraph != null && !checkVerticesWithinSubgraphAlreadyProcessed(candSubGraph) ){
                    InteriorNode interiorNode = insertInteriorNodeAutoNamed(candSubGraph.getValue0(), candSubGraph.getValue1(), candSubGraph.getValue2(), candSubGraph.getValue3());
                    if(isInteriorNodeAddedInCurrentAlgStep(candSubGraph)){
                        interiorNode.setIsNewlyAdded(true);
                    }
                }
            }
        }
        return this;
    }

    public void setOldInteriorNodes(ModelGraph graph){
        Map<String, InteriorNode> intNodesCopy = interiorNodes.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        interiorNodesOld = intNodesCopy;
    }

    public boolean isInteriorNodeAddedInCurrentAlgStep(Quartet<Vertex, Vertex, Vertex, Vertex> newIntNodeVertices){
        for(InteriorNode intNode: interiorNodesOld.values()){
            if(intNode.checkSameVerticesInInteriorNode(newIntNodeVertices)) return false;
        }
        return true;
    }

    private boolean checkVerticesWithinSubgraphAlreadyProcessed(Quartet<Vertex, Vertex, Vertex, Vertex> candSubGraph){
        for(Map.Entry<String, InteriorNode> intNode: this.interiorNodes.entrySet()){
            if(intNode.getValue().checkSameVerticesInInteriorNode(candSubGraph)){
                return true;
            }
        }
        return false;
    }

    private List<Quartet<Vertex, Vertex, Vertex, Vertex>> findVerticesWhichFormsCandSubGraph(FaceNode face){
       Map<String, Integer> candSubGraphTopVertices = new HashMap<>();
       Triplet<Vertex, Vertex, Vertex> triangle = face.getTriangle();
       Vertex v0 = triangle.getValue0(), v1 = triangle.getValue1(), v2 = triangle.getValue2();
       for(FaceNode f: this.getFaces() ){
           if( !f.equals(face) && f.isFaceCongruent(face) ){
               processFaceToFindSubgraphTopVertex(f, triangle, candSubGraphTopVertices);
           }
       }
        List<Vertex> topVertices = getSubgraphTopVertex(triangle, candSubGraphTopVertices, face);
        List<Quartet<Vertex, Vertex, Vertex, Vertex>> subGraphVertices = new LinkedList<>();

       if(topVertices.size() > 0){
           for(Vertex topVertex: topVertices){
               subGraphVertices.add(new Quartet<>(v0, v1, v2, topVertex));
           }

       }
       return subGraphVertices;
    }

    private void processFaceToFindSubgraphTopVertex(FaceNode face, Triplet<Vertex, Vertex, Vertex> triangleBase, Map<String, Integer> topVertices){
        Triplet<Vertex, Vertex, Vertex> congruentTriangle = face.getTriangle();
        Vertex v0 = congruentTriangle.getValue0(), v1 = congruentTriangle.getValue1(), v2 = congruentTriangle.getValue2();
        if( !v0.getId().equals(triangleBase.getValue0().getId()) && !v0.getId().equals(triangleBase.getValue1().getId())
                && !v0.getId().equals(triangleBase.getValue2().getId()) ){
            Integer counter = topVertices.getOrDefault(v0.getId(), 0);
            topVertices.put(v0.getId(), ++counter);
        }
        else if( !v1.getId().equals(triangleBase.getValue0().getId()) && !v1.getId().equals(triangleBase.getValue1().getId())
                && !v1.getId().equals(triangleBase.getValue2().getId()) ){
            Integer counter = topVertices.getOrDefault(v1.getId(), 0);
            topVertices.put(v1.getId(), ++counter);
        }
        else if( !v2.getId().equals(triangleBase.getValue0().getId()) && !v2.getId().equals(triangleBase.getValue1().getId())
                && !v2.getId().equals(triangleBase.getValue2().getId()) ){
            Integer counter = topVertices.getOrDefault(v2.getId(), 0);
            topVertices.put(v2.getId(), ++counter);
        }
    }

    public List<Vertex> getSubgraphTopVertex(Triplet<Vertex, Vertex, Vertex> triangle, Map<String, Integer> candSubGraphTopVertices, FaceNode face){
        List<Vertex> topVertices = new LinkedList<>();
        for(Map.Entry<String, Integer> candVertex: candSubGraphTopVertices.entrySet()){
            if(candVertex.getValue() == 3) { //common vertex for all 3 faces congruent with face formed by triangle parameter
                topVertices.add(vertices.get(candVertex.getKey()));
            }
        }
        return topVertices;
    }

}
