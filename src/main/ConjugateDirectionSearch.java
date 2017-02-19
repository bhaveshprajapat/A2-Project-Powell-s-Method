package main;

import java.util.ArrayList;
import java.util.Stack;

// Conjugate direction search class composed in to powell method
public class ConjugateDirectionSearch {
    private double vectorX, vectorY;
    private double tolerance, bounds;
    private Coordinate startPoint;
    private ArrayList<Coordinate> conjugateDirectionSearchList = new ArrayList<>();
    private Coordinate finalCoordinate;

    public ConjugateDirectionSearch() {
    }

    // sets the vectors on the object
    public void setVector(Coordinate one, Coordinate two) {
        double deltaY = two.getYValue() - one.getYValue();
        double deltaX = two.getXValue() - two.getYValue();
        setVectorX(deltaX);
        setVectorY(deltaY);
    }

    public double getVectorX() {
        return vectorX;
    }

    public void setVectorX(double vectorX) {
        this.vectorX = vectorX;
    }

    public double getVectorY() {
        return vectorY;
    }

    public void setVectorY(double vectorY) {
        this.vectorY = vectorY;
    }

    // begins conjugate direction search
    public void start() throws EvaluationException {
        // Initialise stack and get coordinates from unit vector searches
        Stack<Coordinate> localCoordinateStack = new Stack<>();
        /*
            Find the overall change in Y and X from the previous optimisations
            through unit vectors
         */
        localCoordinateStack.add(startPoint);
        int boundsTightener = 1;
        while (true) {
            Coordinate upperBounds = new Coordinate(
                    localCoordinateStack.peek().getXValue() + (getVectorX() * (getBounds() / boundsTightener)),
                    localCoordinateStack.peek().getYValue() + (getVectorY() * (getBounds() / boundsTightener)));
            Coordinate lowerBounds = new Coordinate(
                    localCoordinateStack.peek().getXValue() - (getVectorX() * (getBounds() / boundsTightener)),
                    localCoordinateStack.peek().getYValue() - (getVectorY() * (getBounds() / boundsTightener)));
            double fOfCurrentPoint = Function.outputFOfXY(localCoordinateStack.peek());
            double fOfUpperBound = Function.outputFOfXY(upperBounds);
            double fOfLowerBound = Function.outputFOfXY(lowerBounds);
            if ((fOfLowerBound > fOfCurrentPoint)
                    && (fOfLowerBound > fOfUpperBound)) {
                lowerBounds.setXValue(localCoordinateStack.peek().getXValue());
                lowerBounds.setYValue(localCoordinateStack.peek().getYValue());
                boundsTightener += 1;
            }
            if ((fOfUpperBound > fOfCurrentPoint)
                    && (fOfUpperBound > fOfLowerBound)) {
                upperBounds.setXValue(localCoordinateStack.peek().getXValue());
                upperBounds.setYValue(localCoordinateStack.peek().getYValue());
                boundsTightener += 1;
            }
            Coordinate midpoint = new Coordinate(((upperBounds.getXValue() + lowerBounds.getXValue()) / 2), ((upperBounds.getYValue() + lowerBounds.getYValue()) / 2));
            getConjugateDirectionSearchList().add(midpoint);
            Coordinate currentPoint = localCoordinateStack.pop();
            localCoordinateStack.add(midpoint);
            Coordinate previousPoint = localCoordinateStack.peek();
            if (Math.abs(Function.outputFOfXY(currentPoint) - Function.outputFOfXY(previousPoint))
                    < tolerance) {
                // TODO integrate logger
                setFinalCoordinate(localCoordinateStack.pop());
                break;
            }
        }
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    public void setStartPoint(Coordinate startPoint) {
        this.startPoint = startPoint;
    }

    public double getBounds() {
        return bounds;
    }

    public void setBounds(double bounds) {
        this.bounds = bounds;
    }

    public ArrayList<Coordinate> getConjugateDirectionSearchList() {
        return conjugateDirectionSearchList;
    }

    public Coordinate getFinalCoordinate() {
        return finalCoordinate;
    }

    public void setFinalCoordinate(Coordinate finalCoordinate) {
        this.finalCoordinate = finalCoordinate;
    }
}
