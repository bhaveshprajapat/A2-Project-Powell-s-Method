package main;

/*
    Coordinate class which stores a corresponding X and Y Value.
 */
import java.io.Serializable;

public class Coordinate implements Serializable {
    private static final long serialVersionUID = -5933363260711222652L;
    private double xValue;
    private double yValue;

    // Coordinate Constructor
    public Coordinate(double xValue, double yValue) {
        this.xValue = xValue;
        this.yValue = yValue;
    }

    // Returns a String to describe this Coordinate Object
    @Override
    public String toString() {
        return "Coordinate " + "-> X: " + xValue + " & Y: " + yValue;
    }

    // Accessor method for X Value
    public double getXValue() {
        return xValue;
    }

    // Mutator method for X Value
    public void setXValue(double xValue) {
        this.xValue = xValue;
    }

    // Accessor method for Y value
    public double getYValue() {
        return yValue;
    }

    // Mutator method for Y Value
    public void setYValue(double yValue) {
        this.yValue = yValue;
    }

    /*
        Overridden method from Object class which allows one Coordinate to be checked for equality of X and Y values with another.
     */
    @Override
    public boolean equals(Object obj) {
        Coordinate Comparison = (Coordinate) obj;
        if (Comparison.xValue == xValue) {
            if (Comparison.yValue == yValue) {
                return true;
            }
        }
        return false;
    }
}
