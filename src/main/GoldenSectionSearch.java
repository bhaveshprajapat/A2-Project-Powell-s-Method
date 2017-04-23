package main;

/*
    Line Minimisation class performs a Golden Section Search in one dimension
 */

public class GoldenSectionSearch extends LinMin {
    @Override
    void startSearch() throws EvaluationException {
        // Initialise local variables
        Coordinate GoldenSection1, GoldenSection2;
        Coordinate LowerBoundary, UpperBoundary;
        Coordinate NewLowerBound, NewUpperBound;
        Coordinate Midpoint;
        double Phi = (1 + Math.sqrt(5)) / 2;
        boolean OptimisingByX = getSearchDirection() == SearchDirection.VECTOR_I;
        int VectorX = (getSearchDirection() == SearchDirection.VECTOR_I) ? 1 : 0;
        int VectorY = (getSearchDirection() == SearchDirection.VECTOR_J) ? 1 : 0;
        double PreviousZValue = Function.evaluate(getStartPoint());
        double FOfGoldenSection1;
        double FOfGoldenSection2;
        // Initialise upper and lower bounds
        LowerBoundary = new Coordinate(
                getStartPoint().getXValue() - (VectorX * getBounds()),
                getStartPoint().getYValue() - (VectorY * getBounds()));
        UpperBoundary = new Coordinate(
                getStartPoint().getXValue() + (VectorX * getBounds()),
                getStartPoint().getYValue() + (VectorY * getBounds()));
        while (true) {
            // Increment the static counter
            LinMin.setCounter(getCounter() + 1);
            // Create the Golden Sections within the boundaries
            if (OptimisingByX) {
                GoldenSection1 = new Coordinate(
                        UpperBoundary.getXValue() - ((UpperBoundary.getXValue() - LowerBoundary.getXValue()) / Phi),
                        LowerBoundary.getYValue());
            } else {
                GoldenSection1 = new Coordinate(
                        LowerBoundary.getXValue(),
                        UpperBoundary.getYValue() - ((UpperBoundary.getYValue() - LowerBoundary.getYValue()) / Phi));
            }
            if (OptimisingByX) {
                GoldenSection2 = new Coordinate(
                        LowerBoundary.getXValue() + ((UpperBoundary.getXValue() - LowerBoundary.getXValue()) / Phi),
                        LowerBoundary.getYValue());
            } else {
                GoldenSection2 = new Coordinate(
                        LowerBoundary.getXValue(),
                        LowerBoundary.getYValue() + ((UpperBoundary.getYValue() - LowerBoundary.getYValue()) / Phi));
            }
            // Evaluate each Golden Section coordinate
            FOfGoldenSection1 = Function.evaluate(GoldenSection1);
            FOfGoldenSection2 = Function.evaluate(GoldenSection2);

            // If the second golden section is highest, set the upper bound to the second section
            if (FOfGoldenSection1 < FOfGoldenSection2) {
                NewLowerBound = new Coordinate(LowerBoundary.getXValue(), LowerBoundary.getYValue());
                NewUpperBound = new Coordinate(GoldenSection2.getXValue(), GoldenSection2.getYValue());
            } else {
                // Otherwise set the lower bound to the first section
                NewLowerBound = new Coordinate(GoldenSection1.getXValue(), GoldenSection1.getYValue());
                NewUpperBound = new Coordinate(UpperBoundary.getXValue(), UpperBoundary.getYValue());
            }
            // Finds and creates the midpoint
            Midpoint = new Coordinate((NewLowerBound.getXValue() + NewUpperBound.getXValue()) / 2.0, (NewLowerBound.getYValue() + NewUpperBound.getYValue()) / 2.0);

            if (Math.abs(PreviousZValue - Function.evaluate(Midpoint)) < getTolerance()) {
                // If the stopping tolerance is met, return with the final coordinate
                setFinalCoordinate(Midpoint);
                return;
            } else {
                // Restrict the boundaries, set the previous function value and reiterate
                LowerBoundary = NewLowerBound;
                UpperBoundary = NewUpperBound;
                PreviousZValue = Function.evaluate(Midpoint);
            }
        }
    }
}