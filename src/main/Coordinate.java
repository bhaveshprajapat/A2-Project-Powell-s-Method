package main;

/*
    Coordinate class to store coordinates, which are serialisable
 */

import java.io.Serializable;

public class Coordinate implements Serializable {
    private static final long serialVersionUID = -5933363260711222652L;
    private double XValue;
    private double YValue;

    public Coordinate(double x_Value, double y_Value) {
        XValue = x_Value;
        YValue = y_Value;
    }

    @Override
    public String toString() {
        return "X: " + getXValue() + " Y: " + getYValue();
    }

    // x value getter
    public double getXValue() {
        return XValue;
    }

    // x value setter
    public void setXValue(double XValue) {
        this.XValue = XValue;
    }

    // y value getter
    public double getYValue() {
        return YValue;
    }

    // y value setter
    public void setYValue(double YValue) {
        this.YValue = YValue;
    }

    @Override
    public boolean equals(Object obj) {
        Coordinate comparison = (Coordinate) obj;
        return (comparison.getXValue() == getXValue()) && (comparison.getYValue() == getYValue());
    }
}
