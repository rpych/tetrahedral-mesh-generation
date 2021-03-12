package common;

import model.Vertex;

public class Utils {

    public static boolean isVertexSameAs(Vertex a, Vertex b){
        return a.getId().equals(b.getId());
    }


}
