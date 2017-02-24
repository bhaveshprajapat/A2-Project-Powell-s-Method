package main;


import java.util.ArrayList;

public class ConjugateDirectionExponentialSearch {
    private Coordinate startPoint;
    private double xVector;
    private double yVector;
    private Coordinate finalCoordinate;
    private ArrayList<Coordinate> searchList = new ArrayList<>();
    private double tolerance;

    public ConjugateDirectionExponentialSearch(Coordinate startPoint, double xVector, double yVector, double tolerance) {
        this.startPoint = startPoint;
        this.xVector = xVector;
        this.yVector = yVector;
        this.tolerance = tolerance;
    }

    public ConjugateDirectionExponentialSearch() {

    }

    public Coordinate getFinalCoordinate() {
        return finalCoordinate;
    }

    public void setFinalCoordinate(Coordinate finalCoordinate) {
        this.finalCoordinate = finalCoordinate;
    }

    public ArrayList<Coordinate> getSearchList() {
        return searchList;
    }

    public void setSearchList(ArrayList<Coordinate> searchList) {
        this.searchList = searchList;
    }

    void start() throws EvaluationException {
        searchList.add(startPoint);
        Function function = new Function();
        // declare local variables
        int maxpowerof2 = 2;
        Coordinate coordinate1, coordinate2, coordinate3;
        coordinate1 = startPoint;
        double tempXValue, tempYValue;
        double fOfCoordinate1, fOfCoordinate2, fOfCoordinate3;
        // while loop to increase the power of 2
        while (true) {
            // generate three coordinates with three different powers
            tempXValue = coordinate1.getXValue() + scaleVectorByPowerOf2(xVector, maxpowerof2 - 2);
            tempYValue = coordinate1.getYValue() + scaleVectorByPowerOf2(yVector, maxpowerof2 - 2);
            coordinate1 = new Coordinate(tempXValue, tempYValue);
            tempXValue = coordinate1.getXValue() + scaleVectorByPowerOf2(xVector, maxpowerof2 - 1);
            tempYValue = coordinate1.getYValue() + scaleVectorByPowerOf2(yVector, maxpowerof2 - 1);
            coordinate2 = new Coordinate(tempXValue, tempYValue);
            tempXValue = coordinate1.getXValue() + scaleVectorByPowerOf2(xVector, maxpowerof2);
            tempYValue = coordinate1.getYValue() + scaleVectorByPowerOf2(yVector, maxpowerof2);
            coordinate3 = new Coordinate(tempXValue, tempYValue);

            fOfCoordinate1 = function.outputFOfXY(coordinate1);
            fOfCoordinate2 = function.outputFOfXY(coordinate2);
            fOfCoordinate3 = function.outputFOfXY(coordinate3);
            if ((fOfCoordinate3 > fOfCoordinate2) && (fOfCoordinate3 > fOfCoordinate1)) {
                break;
            } else {
                maxpowerof2 += 1;
                searchList.add(coordinate3);
            }
        }
        // minimum is now bracketed by coordinate 1 and coordinate 3
        // begin 2D binary search
        double midpointX = (coordinate1.getXValue() + coordinate3.getXValue()) / 2;
        double midpointY = (coordinate1.getYValue() + coordinate3.getYValue()) / 2;
        Coordinate midpoint = new Coordinate(midpointX, midpointY);
        Coordinate upperBound, lowerBound;
        upperBound = coordinate3;
        lowerBound = coordinate1;
        double fOfUpperBound, fOfLowerBound;
        fOfLowerBound = function.outputFOfXY(lowerBound);
        fOfUpperBound = function.outputFOfXY(upperBound);
        searchList.add(midpoint);
        while (true) {
            if (fOfLowerBound > fOfUpperBound) {
                lowerBound = midpoint;
            }
            if (fOfUpperBound > fOfLowerBound) {
                upperBound = midpoint;
            }
            midpointX = (upperBound.getXValue() + lowerBound.getXValue()) / 2;
            midpointY = (upperBound.getYValue() + lowerBound.getYValue()) / 2;
            Coordinate newMidpoint = new Coordinate(midpointX, midpointY);
            searchList.add(newMidpoint);
            double fOfCurrentCoordinate = function.outputFOfXY(searchList.get(searchList.size() - 1));
            double fOfLastCoordinate = function.outputFOfXY(searchList.get(searchList.size() - 2));
            if (Math.abs(fOfCurrentCoordinate - fOfLastCoordinate) < tolerance) {
                setFinalCoordinate(searchList.get(searchList.size() - 1));
                break;
            }
        }

    }

    public Coordinate getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(Coordinate startPoint) {
        this.startPoint = startPoint;
    }

    public double getxVector() {
        return xVector;
    }

    public void setxVector(double xVector) {
        this.xVector = xVector;
    }

    public double getyVector() {
        return yVector;
    }

    public void setyVector(double yVector) {
        this.yVector = yVector;
    }

    public double getTolerance() {
        return tolerance;
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    private double scaleVectorByPowerOf2(double vector, int power) {
        return vector * Math.pow(2, power);
    }

    // sets the vectors on the object
    public void setVector(Coordinate one, Coordinate two) {
        double deltaY = two.getYValue() - one.getYValue();
        double deltaX = two.getXValue() - one.getXValue();
        setxVector(deltaX);
        setyVector(deltaY);
    }
}
