package common;
import model.Coordinates;
import model.GraphEdge;
import model.GraphNode;

import java.util.*;

//LayersFunction
public class LFunction {

    enum LAYER{
        BLUE, YELLOW, ORANGE;
    }

    public static LAYER FLayer(Coordinates point){
        if(point.getZ() <= LFunction.PlaneBlue(point) && point.getZ() > LFunction.PlaneYellow(point)){
            return LAYER.BLUE;
        }
        else if(point.getZ() <= LFunction.PlaneYellow(point) && point.getZ() > LFunction.PlaneOrange(point)){
            return LAYER.YELLOW;
        }

        return LAYER.ORANGE;
    }

    public static double F(Coordinates point){
        if(point.getZ() <= LFunction.PlaneBlue(point) && point.getZ() > LFunction.PlaneYellow(point)){
            return 0.7;
        }
        else if(point.getZ() <= LFunction.PlaneYellow(point) && point.getZ() > LFunction.PlaneOrange(point)){
            return 0.7;
        }

        return  0.3;//0.0;
    }

    public static Optional<Coordinates> getBreakPoint(GraphEdge egdeToBreak, double fValue){
        GraphNode a = egdeToBreak.getEdgeNodes().getValue0();
        GraphNode b = egdeToBreak.getEdgeNodes().getValue1();
        double deltaZ = Math.abs(b.getZCoordinate() - a.getZCoordinate());
        double eps = 0.0000001;
        if(deltaZ <= eps){
            return Optional.empty(); //possible division by 0 means edge has hanging node because we consider F in z dimension only
        }
        Coordinates bottomNode = (a.getZCoordinate() <= b.getZCoordinate()) ? a.getCoordinates() : b.getCoordinates();
        Coordinates upperNode = (a.getZCoordinate() > b.getZCoordinate()) ? a.getCoordinates() : b.getCoordinates();
        double bottomZValue = bottomNode.getZ();
        double k = Math.abs(fValue - bottomZValue) / deltaZ;
        double offsetX = k * (upperNode.getX() - bottomNode.getX());
        double offsetY = k * (upperNode.getY() - bottomNode.getY());
        double offsetZ = k * (upperNode.getZ() - bottomNode.getZ());
        double x = bottomNode.getX() + offsetX;
        double y = bottomNode.getY() + offsetY;
        double z = bottomNode.getZ() + offsetZ;

        return Optional.of(new Coordinates(x, y, z));
    }

    public static boolean areDifferentLayers(LAYER l1, LAYER l2){
        return l1.ordinal() != l2.ordinal();
    }

    public static double PlaneBlue(Coordinates point){
        double z = 1.0f; // plane equation
        return z;
    }

    public static double PlaneYellow(Coordinates point){
        double z = 0.7f; // plane equation
        return z;
    }

    public static double PlaneOrange(Coordinates point){
        double z = 0.3f; //0.0, plane equation
        return z;
    }


}
