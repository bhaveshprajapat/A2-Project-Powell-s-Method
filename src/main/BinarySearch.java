package main;

/*
    Line Minimisation class performs a Binary Search in one dimension
 */
public class BinarySearch extends LinMin {
    @Override
    void startSearch() throws EvaluationException {
        Coordinate PreviousCoordinate = getStartPoint();
        int BoundaryRestriction = 1;
        while (true) {
            // Increment the static counter
            LinMin.setCounter(getCounter() + 1);
            // Create direction vectors and set their values
            boolean OptimisingByX = getSearchDirection() == SearchDirection.VECTOR_I;
            int VectorX = OptimisingByX ? 1 : 0;
            int VectorY = OptimisingByX ? 0 : 1;
            // Creates lower boundary
            double LowerBoundaryXValue = PreviousCoordinate.getXValue() - (VectorX * (getBounds() / BoundaryRestriction));
            double LowerBoundaryYValue = PreviousCoordinate.getYValue() - (VectorY * (getBounds() / BoundaryRestriction));
            Coordinate LowerBoundary = new Coordinate(LowerBoundaryXValue, LowerBoundaryYValue);
            // Creates upper boundary
            double UpperBoundaryXValue = PreviousCoordinate.getXValue() + (VectorX * (getBounds() / BoundaryRestriction));
            double UpperBoundaryYValue = PreviousCoordinate.getYValue() + (VectorY * (getBounds() / BoundaryRestriction));
            Coordinate UpperBoundary = new Coordinate(UpperBoundaryXValue, UpperBoundaryYValue);
            // All three coordinates are evaluated
            double ZValueForUpperBoundary = Function.evaluate(UpperBoundary);
            double ZValueForLowerBoundary = Function.evaluate(LowerBoundary);
            double ZValueForCurrentPoint = Function.evaluate(getStartPoint());
            // Shifts upper bound if its output is highest
            if ((ZValueForUpperBoundary > ZValueForCurrentPoint)
                    && (ZValueForUpperBoundary > ZValueForLowerBoundary)) {
                UpperBoundary.setXValue(getStartPoint().getXValue());
                UpperBoundary.setYValue(getStartPoint().getYValue());
            }
            // Shifts lower bound if its output is highest
            if ((ZValueForLowerBoundary > ZValueForCurrentPoint)
                    && (ZValueForLowerBoundary > ZValueForUpperBoundary)) {
                LowerBoundary.setXValue(getStartPoint().getXValue());
                LowerBoundary.setYValue(getStartPoint().getYValue());
            }
            // Creates the Midpoint between the new bounds and sets the current point
            Coordinate Midpoint = new Coordinate(
                    (UpperBoundary.getXValue() + LowerBoundary.getXValue()) / 2.0,
                    (UpperBoundary.getYValue() + LowerBoundary.getYValue()) / 2.0);
            // When the current direction is optimised within the bounds, log it and change the direction
            if (Math.abs(Function.evaluate(Midpoint) - Function.evaluate(PreviousCoordinate))
                    < getTolerance()) {
                setFinalCoordinate(Midpoint);
                return;
            } else {
                // Restrict the bounds
                BoundaryRestriction *= 2;
                PreviousCoordinate = new Coordinate(Midpoint.getXValue(), Midpoint.getYValue());
            }
        }
    }
}