package common;
import model.Coordinates;
import model.GraphEdge;
import model.GraphNode;

import java.util.*;

//LayersFunction
public class LFunction {
    /* 3 members for only parallel layers*/
    private static double BlueLayerHeight = 1.0f;
    private static double YellowLayerHeight = 0.3f;
    private static double OrangeLayerHeight = 0.0f;
    //members for default solution with any layer form
    private static double layersLowerBoundary = 0.0f;
    private static double brThreshold = 0.01; //epsilon
    public static Map<LAYER, Plane> planes;
    static {
        planes = new HashMap<LAYER, Plane>();
        planes.put(LAYER.HIGHEST, (new Plane(0.0, 0.0, 1.0, -1.0)));
        planes.put(LAYER.INTERMEDIATE, (new Plane(0.0, 0.0, 1.0, -0.75)));
        //planes.put(LAYER.INTERMEDIATE, (new Plane(100.0, -200.0, 1180.0, -693.0)));
        //planes.put(LAYER.INTERMEDIATE, (new Plane(100.0, -200.0, 1180.0, -811.0)));
        //planes.put(LAYER.INTERMEDIATE, (new Plane(70.0, 85.0, 35.0, -78.0)));
        //planes.put(LAYER.INTERMEDIATE, (new Plane(0.0, 0.0, 1.0, -0.7)));
        //planes.put(LAYER.LOWEST, (new Plane(0.0, 0.0, 1.0, -0.3)));
        //planes.put(LAYER.LOWEST, (new Plane(50.0, -100.0, 590.0, -199.0)));
        planes.put(LAYER.LOWEST, (new Plane(1.0, 0.0, 10.0, -4.0)));
    }

    public enum LAYER{
        HIGHEST, INTERMEDIATE, LOWEST;
    }

    public static LAYER FLayer(Coordinates point){
        if(point.getZ() <= LFunction.PlaneBlue(point) && point.getZ() >= LFunction.PlaneYellow(point)){
            return LAYER.HIGHEST;
        }
        else if(point.getZ() <= LFunction.PlaneYellow(point) && point.getZ() >= LFunction.PlaneOrange(point)){
            return LAYER.INTERMEDIATE;
        }

        return LAYER.LOWEST;
    }
    //function to use with only parallel layers
    /*public static boolean areDifferentLayers(Coordinates a, Coordinates b){
        if( (a.getZ() <= LFunction.PlaneBlue(a)) && (a.getZ() >= LFunction.PlaneYellow(a))
                && (b.getZ() <= LFunction.PlaneBlue(b)) && (b.getZ() >= LFunction.PlaneYellow(b)) ){
            return false;
        }
        else if( (a.getZ() <= LFunction.PlaneYellow(a)) && (a.getZ() >= LFunction.PlaneOrange(a))
                && (b.getZ() <= LFunction.PlaneYellow(b)) && (b.getZ() >= LFunction.PlaneOrange(b)) ){
            return false;
        } //when using 2 layers below condition is never checked
        else if((a.getZ() <= LFunction.PlaneOrange(a)) && (a.getZ() >= layersLowerBoundary)
                && (b.getZ() <= LFunction.PlaneOrange(b)) && (b.getZ() >= layersLowerBoundary)){
            return false;
        }
        return true;
    }*/
    //main function for serving layers
    public static boolean areDifferentLayers(Coordinates a, Coordinates b){
        Plane highest = planes.get(LAYER.HIGHEST), intermediate = planes.get(LAYER.INTERMEDIATE), lowest = planes.get(LAYER.LOWEST);
        if( highest.isPointBelowOrOnPlane(a) && intermediate.isPointAboveOrOnPlane(a)
                && highest.isPointBelowOrOnPlane(b) && intermediate.isPointAboveOrOnPlane(b) ){
            return false;
        }
        else if( intermediate.isPointBelowOrOnPlane(a) && lowest.isPointAboveOrOnPlane(a)
                && intermediate.isPointBelowOrOnPlane(b) && lowest.isPointAboveOrOnPlane(b) ){
            return false;
        } //when using 2 layers below condition is never checked
        else if(lowest.isPointBelowOrOnPlane(a) && (a.getZ() >= layersLowerBoundary)
                && lowest.isPointBelowOrOnPlane(b) && (b.getZ() >= layersLowerBoundary)){
            return false;
        }
        return true;
    }

    /* point variable represents InteriorNode central point*/
    public static boolean isDistanceToLayerBelowThreshold(LAYER layer, Coordinates point){
        double distance = planes.get(layer).distanceFromPlane(point);
        return (Double.compare(distance, brThreshold) == -1);
    }

    public static boolean arePointsCrossingLayer(LAYER layer, Coordinates a, Coordinates b){
        Plane plane = planes.get(layer);
        return (plane.isPointBelowOrOnPlane(a) && plane.isPointAboveOrOnPlane(b)) ||
                (plane.isPointBelowOrOnPlane(b) && plane.isPointAboveOrOnPlane(a));
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
