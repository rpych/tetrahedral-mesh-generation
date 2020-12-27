package common;
import model.Coordinates;
import model.GraphEdge;
import model.GraphNode;

import java.util.*;

//LayersFunction
public class LFunction {

    private static double BlueLayerHeight = 1.0f;
    private static double YellowLayerHeight = 0.70f;
    private static double OrangeLayerHeight = 0.00f;
    private static double layersLowerBoundary = 0.0f;
    public static double layersUpCoords[] = new double[]{YellowLayerHeight, OrangeLayerHeight};

    enum LAYER{
        BLUE, YELLOW, ORANGE;
    }

    public static LAYER FLayer(Coordinates point){
        if(point.getZ() <= LFunction.PlaneBlue(point) && point.getZ() >= LFunction.PlaneYellow(point)){
            return LAYER.BLUE;
        }
        else if(point.getZ() <= LFunction.PlaneYellow(point) && point.getZ() >= LFunction.PlaneOrange(point)){
            return LAYER.YELLOW;
        }

        return LAYER.ORANGE;
    }

    public static double F(Coordinates point){
        if(point.getZ() <= LFunction.PlaneBlue(point) && point.getZ() > LFunction.PlaneYellow(point)){
            return YellowLayerHeight;
        }
        else if(point.getZ() <= LFunction.PlaneYellow(point) && point.getZ() > LFunction.PlaneOrange(point)){
            return OrangeLayerHeight;
        }

        return  -1.0;
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

    public static boolean areDifferentLayers(Coordinates a, Coordinates b){
        //return l1.ordinal() != l2.ordinal();
        if( (a.getZ() <= LFunction.PlaneBlue(a)) && (a.getZ() >= LFunction.PlaneYellow(a))
                && (b.getZ() <= LFunction.PlaneBlue(b)) && (b.getZ() >= LFunction.PlaneYellow(b)) ){
            return false;
        }
        else if( (a.getZ() <= LFunction.PlaneYellow(a)) && (a.getZ() >= LFunction.PlaneOrange(a))
                && (b.getZ() <= LFunction.PlaneYellow(b)) && (b.getZ() >= LFunction.PlaneOrange(b)) ){
            return false;
        } //when using 2 layers below condition is never checked
        /*else if((a.getZ() <= LFunction.PlaneOrange(a)) && (a.getZ() >= layersLowerBoundary)
                && (b.getZ() <= LFunction.PlaneOrange(b)) && (b.getZ() >= layersLowerBoundary)){
            return false;
        }*/
        return true;
    }

    public static double PlaneBlue(Coordinates point){
        double z = BlueLayerHeight; // plane equation
        return z;
    }

    public static double PlaneYellow(Coordinates point){
        double z = YellowLayerHeight; // plane equation
        return z;
    }

    public static double PlaneOrange(Coordinates point){
        double z = OrangeLayerHeight; // plane equation
        return z;
    }


}
