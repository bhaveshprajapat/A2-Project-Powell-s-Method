package main;

/*
    Line Minimisation class performs a Golden Section Search in one dimension
 */

public class GoldenSectionSearch extends LinMin {
    @Override
    void startSearch() throws EvaluationException {
        // Initialise local variables
        Coordinate GoldenSection1;
        Coordinate GoldenSection2;
        Coordinate NewLowerBound;
        Coordinate NewUpperBound;
        double Phi = (1 + Math.sqrt(5)) / 2;
        boolean OptimisingByX = getSearchDirection() == SearchDirection.VECTOR_I;
        double PreviousZValue = Function.evaluate(getStartPoint());
        int VectorX = (getSearchDirection() == SearchDirection.VECTOR_I) ? 1 : 0;
        int VectorY = (getSearchDirection() == SearchDirection.VECTOR_J) ? 1 : 0;
        // Initialise upper and lower bounds
        Coordinate LowerBoundary = new Coordinate(
                getStartPoint().getXValue() - (VectorX * getBounds()),
                getStartPoint().getYValue() - (VectorY * getBounds()));
        Coordinate upperBound = new Coordinate(
                getStartPoint().getXValue() + (VectorX * getBounds()),
                getStartPoint().getYValue() + (VectorY * getBounds()));
        while (true) {
            // Increment the static counter
            LinMin.setCounter(getCounter() + 1);
            // Create the Golden Sections within the boundaries
            if (OptimisingByX) {
                GoldenSection1 = new Coordinate(
                        upperBound.getXValue() - ((upperBound.getXValue() - LowerBoundary.getXValue()) / Phi),
                        LowerBoundary.getYValue());
            } else {
                GoldenSection1 = new Coordinate(
                        LowerBoundary.getXValue(),
                        upperBound.getYValue() - (upperBound.getYValue() - LowerBoundary.getYValue()) / Phi);
            }
            if (OptimisingByX) {
                GoldenSection2 = new Coordinate(
                        LowerBoundary.getXValue() + ((upperBound.getXValue() - LowerBoundary.getXValue()) / Phi),
                        LowerBoundary.getYValue());
            } else {
                GoldenSection2 = new Coordinate(
                        LowerBoundary.getXValue(),
                        LowerBoundary.getYValue() + (upperBound.getYValue() - LowerBoundary.getYValue()) / Phi);
            }
            // Evaluate each Golden Section coordinate
            double FOfGoldenSection1 = Function.evaluate(GoldenSection1);
            double FOfGoldenSection2 = Function.evaluate(GoldenSection2);

            // If the second golden section is highest, set the upper bound to the second section
            if (FOfGoldenSection1 < FOfGoldenSection2) {
                NewLowerBound = new Coordinate(LowerBoundary.getXValue(), LowerBoundary.getYValue());
                NewUpperBound = new Coordinate(GoldenSection2.getXValue(), GoldenSection2.getYValue());
            } else {
                // Otherwise set the lower bound to the first section
                NewLowerBound = new Coordinate(GoldenSection1.getXValue(), GoldenSection1.getYValue());
                NewUpperBound = new Coordinate(upperBound.getXValue(), upperBound.getYValue());
            }
            // Finds and creates the midpoint
            Coordinate Midpoint = new Coordinate((NewLowerBound.getXValue() + NewUpperBound.getXValue()) / 2.0, (NewLowerBound.getYValue() + NewUpperBound.getYValue()) / 2.0);

            if (Math.abs(PreviousZValue - Function.evaluate(Midpoint)) < getTolerance()) {
                // If the stopping tolerance is met, return with the final coordinate
                setFinalCoordinate(Midpoint);
                return;
            } else {
                // Restrict the boundaries, set the previous function value and reiterate
                LowerBoundary = NewLowerBound;
                upperBound = NewUpperBound;
                PreviousZValue = Function.evaluate(Midpoint);
            }
        }
    }
}