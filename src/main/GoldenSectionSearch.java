package main;

public class GoldenSectionSearch extends LinMin {
    @Override
    void startSearch() throws EvaluationException {
        // TODO write a commment
        boolean OptimisingByX = (getSearchDirection() == SearchDirection.VECTOR_I);
        double PreviousZValue = Function.evaluate(getStartPoint());
        // Initialises vectors
        int VectorX = (getSearchDirection() == SearchDirection.VECTOR_I) ? 1 : 0;
        int VectorY = (getSearchDirection() == SearchDirection.VECTOR_J) ? 1 : 0;
        // Create lower boundary
        Coordinate LowerBoundary = new Coordinate(
                getStartPoint().getXValue() - (VectorX * (getBounds())),
                getStartPoint().getYValue() - (VectorY * (getBounds())));
        // Creates upper boundary
        Coordinate upperBound = new Coordinate(
                getStartPoint().getXValue() + (VectorX * (getBounds())),
                getStartPoint().getYValue() + (VectorY * (getBounds())));
        while (true) {
            double Phi = (1 + Math.sqrt(5)) / 2;
            // Create the golden sections within the boundaries
            Coordinate GoldenSection1 = new Coordinate(
                    OptimisingByX ? (upperBound.getXValue() - ((upperBound.getXValue() - LowerBoundary.getXValue()) / Phi)) : LowerBoundary.getXValue(),
                    OptimisingByX ? LowerBoundary.getYValue() : (upperBound.getYValue() - ((upperBound.getYValue() - LowerBoundary.getYValue()) / Phi)));
            Coordinate GoldenSection2 = new Coordinate(
                    OptimisingByX ? (LowerBoundary.getXValue() + ((upperBound.getXValue() - LowerBoundary.getXValue()) / Phi)) : LowerBoundary.getXValue(),
                    OptimisingByX ? LowerBoundary.getYValue() : (LowerBoundary.getYValue() + ((upperBound.getYValue() - LowerBoundary.getYValue()) / Phi)));
            // Evaluate each golden section coordinate
            double FOfGoldenSection1 = Function.evaluate(GoldenSection1);
            double FOfGoldenSection2 = Function.evaluate(GoldenSection2);
            Coordinate NewLowerBound, NewUpperBound;
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
            Coordinate Midpoint = new Coordinate((NewLowerBound.getXValue() + NewUpperBound.getXValue()) / 2, (NewLowerBound.getYValue() + NewUpperBound.getYValue()) / 2);

            if (Math.abs(PreviousZValue - Function.evaluate(Midpoint)) < getTolerance()) {
                // If the stopping tolerance is met return with the final coordinate
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