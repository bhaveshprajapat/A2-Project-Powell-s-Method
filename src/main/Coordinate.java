package main;

/*
    Coordinate class to store coordinates
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

    public double getXValue() {

        return XValue;
    }

    public void setXValue(double XValue) {
        this.XValue = XValue;
    }

    public double getYValue() {
        return YValue;
    }

    public void setYValue(double YValue) {
        this.YValue = YValue;
    }

}
