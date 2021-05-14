package common;

import app.Config;
import model.Coordinates;
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

    public static boolean areBorderCoordinates(double coordDimV0, double coordDimV1, double coordDimV2, double bound){
        return ( (Double.compare(coordDimV0, bound) == 0) && (Double.compare(coordDimV1, bound) == 0) &&
                 (Double.compare(coordDimV2, bound) == 0) );
    }

    public enum INTERIOR_GEN_TYPE{
        BASIC_TYPE, EXTRA_REFINEMENT;
    }

}
