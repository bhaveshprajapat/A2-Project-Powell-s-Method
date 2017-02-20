package main;

public class BinarySearch extends LinMin {
    // binary search in one dimension
    @Override
    void startSearch() throws EvaluationException {
        Function function = new Function();
        Coordinate PreviousCoordinate = getStartPoint();
        int BoundaryRestriction = 1;
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            // Create direction vectors and set their values
            int vectorX, vectorY;
            boolean OptimisingByX = (getSearchDirection() == SearchDirection.Vector_I);
            if (OptimisingByX) {
                vectorX = 1;
            } else {
                vectorX = 0;
            }
            if (OptimisingByX) {
                vectorY = 0;
            } else {
                vectorY = 1;
            }
            // Creates lower boundary
            double lowerBoundaryXValue = PreviousCoordinate.getXValue() - (vectorX * (getBounds() / (BoundaryRestriction)));
            double lowerBoundaryYValue = PreviousCoordinate.getYValue() - (vectorY * (getBounds() / (BoundaryRestriction)));
            Coordinate lowerBoundary = new Coordinate(lowerBoundaryXValue, lowerBoundaryYValue);
            // Creates upper boundary
            double upperBoundaryXValue = PreviousCoordinate.getXValue() + (vectorX * (getBounds() / (BoundaryRestriction)));
            double upperBoundaryYValue = PreviousCoordinate.getYValue() + (vectorY * (getBounds() / (BoundaryRestriction)));
            Coordinate upperBoundary = new Coordinate(upperBoundaryXValue, upperBoundaryYValue);
            // All three coordinates are evaluated
            double zOFUpperBound = function.outputFOfXY(upperBoundary);
            double zOFLowerBound = function.outputFOfXY(lowerBoundary);
            double zOfCurrentPoint = function.outputFOfXY(getStartPoint());
            // Shifts upper bound if its output is highest
            if ((zOFUpperBound > zOfCurrentPoint)
                    && (zOFUpperBound > zOFLowerBound)) {
                upperBoundary.setXValue(getStartPoint().getXValue());
                upperBoundary.setYValue(getStartPoint().getYValue());
            }
            // Shifts lower bound if its output is highest
            if ((zOFLowerBound > zOfCurrentPoint)
                    && (zOFLowerBound > zOFUpperBound)) {
                lowerBoundary.setXValue(getStartPoint().getXValue());
                lowerBoundary.setYValue(getStartPoint().getYValue());
            }
            // Creates the midpoint between the new bounds and sets the current point
            Coordinate midpoint = new Coordinate(
                    (upperBoundary.getXValue() + lowerBoundary.getXValue()) / 2,
                    (upperBoundary.getYValue() + lowerBoundary.getYValue()) / 2);
            // When the current direction is optimised within the bounds, log it and change the direction
            if (Math.abs(function.outputFOfXY(midpoint) - function.outputFOfXY(PreviousCoordinate))
                    < getTolerance()) {
                setFinalCoordinate(midpoint);
                return;
            } else {
                // Restrict the bounds
                BoundaryRestriction *= 2;
                PreviousCoordinate = new Coordinate(midpoint.getXValue(), midpoint.getYValue());
            }
        }
    }
}
