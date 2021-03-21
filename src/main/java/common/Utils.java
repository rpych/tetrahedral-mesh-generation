package common;

import model.GraphEdge;
import model.GraphNode;
import model.Vertex;

import java.util.Optional;

public class Utils {

    public static boolean isVertexSameAs(Vertex a, Vertex b){
        return a.getId().equals(b.getId());
    }

    public static boolean isEdgeBetween(GraphNode v1, GraphNode v2) {
        return null != v1.getEdgeBetween(v2);
    }

}
