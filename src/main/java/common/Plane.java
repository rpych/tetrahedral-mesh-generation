package common;

import model.Coordinates;

public class Plane {

    private Double A;
    private Double B;
    private Double C;
    private Double D;

    public Plane(Double A, Double B, Double C, Double D){
        this.A = A;
        this.B = B;
        this.C = C;
        this.D = D;
    }

    public double distanceFromPlane(Coordinates point){
        double x = point.getX(), y = point.getY(), z = point.getZ();
        return Math.abs(A*x + B*y + C*z + D) / Math.sqrt(A*A + B*B + C*C);
    }

    public boolean isPointOnPlane(Coordinates point){
        double x = point.getX(), y = point.getY(), z = point.getZ();
        return Double.compare(A*x + B*y + C*z + D, 0.0) == 0; // A*x + B*y + C*z + D = 0
    }

    public boolean isPointAboveOrOnPlane(Coordinates point){
        double x = point.getX(), y = point.getY(), z = point.getZ();
        return Double.compare(A*x + B*y + C*z + D, 0.0) >= 0; // A*x + B*y + C*z + D > 0
    }

    public boolean isPointBelowOrOnPlane(Coordinates point){
        double x = point.getX(), y = point.getY(), z = point.getZ();
        return Double.compare(A*x + B*y + C*z + D, 0.0) <= 0; // A*x + B*y + C*z + D < 0
    }

    public Double getA() {
        return A;
    }

    public Double getB() {
        return B;
    }

    public Double getC() {
        return C;
    }

    public Double getD() {
        return D;
    }
}