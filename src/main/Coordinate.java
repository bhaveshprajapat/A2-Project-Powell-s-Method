package main;

/*
    Coordinate class to store coordinates
 */

public class Coordinate {
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
