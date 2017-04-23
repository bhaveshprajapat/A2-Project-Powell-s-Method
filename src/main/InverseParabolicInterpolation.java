package main;

/*
    Line Minimisation class performs an Inverse Parabolic Interpolation in one dimension
 */
public class InverseParabolicInterpolation extends LinMin {

    @Override
    void startSearch() throws EvaluationException {
        // Initialise variables
        Coordinate PreviousGuess, NextGuess;
        int VectorX, VectorY;
        boolean OptimisingByX;
        double LowerBoundaryXValue, LowerBoundaryYValue;
        double UpperBoundaryXValue, UpperBoundaryYValue;
        double numerator, denominator, interpolatedValue;
        Coordinate LowerBoundary, UpperBoundary;
        /*
            a, b & c are named as such to ease readability
        */
        double a; // Lower bound
        double b; // Current guess
        double c; // Upper bound
        PreviousGuess = getStartPoint(); // Initialise first guess
        // Create direction vectors and set their values
        OptimisingByX = getSearchDirection() == SearchDirection.VECTOR_I;
        VectorX = OptimisingByX ? 1 : 0;
        VectorY = OptimisingByX ? 0 : 1;
        // Creates lower boundary
        LowerBoundaryXValue = PreviousGuess.getXValue() - (VectorX * getBounds());
        LowerBoundaryYValue = PreviousGuess.getYValue() - (VectorY * getBounds());
        LowerBoundary = new Coordinate(LowerBoundaryXValue, LowerBoundaryYValue);
        // Creates upper boundary
        UpperBoundaryXValue = PreviousGuess.getXValue() + (VectorX * getBounds());
        UpperBoundaryYValue = PreviousGuess.getYValue() + (VectorY * getBounds());
        UpperBoundary = new Coordinate(UpperBoundaryXValue, UpperBoundaryYValue);

        while (true) {
            // Increment optimisation counter
            LinMin.setCounter(LinMin.getCounter() + 1);

            // Initialise a, b and c
            if (OptimisingByX) {
                a = LowerBoundary.getXValue();
                b = PreviousGuess.getXValue();
                c = UpperBoundary.getXValue();
            } else {
                a = LowerBoundary.getYValue();
                b = PreviousGuess.getYValue();
                c = UpperBoundary.getYValue();
            }
            // Calculate a new best guess through the inverse parabola
            numerator = (Math.pow(b - a, 2.0) * (Function.evaluate(PreviousGuess) - Function.evaluate(UpperBoundary))) - (Math.pow(b - c, 2.0) * (Function.evaluate(PreviousGuess) - Function.evaluate(LowerBoundary)));
            denominator = ((b - a) * (Function.evaluate(PreviousGuess) - Function.evaluate(UpperBoundary))) - ((b - c) * (Function.evaluate(PreviousGuess) - Function.evaluate(LowerBoundary)));
            interpolatedValue = b - (0.5 * (numerator / denominator));
            // Create a new coordinate with the interpolated value, and adjust either a or c to the previous guess
            if (OptimisingByX) {
                NextGuess = new Coordinate(interpolatedValue, PreviousGuess.getYValue());
                if (interpolatedValue < PreviousGuess.getXValue()) {
                    UpperBoundary = new Coordinate(PreviousGuess.getXValue(), PreviousGuess.getYValue());
                } else if (interpolatedValue > PreviousGuess.getXValue()) {
                    LowerBoundary = new Coordinate(PreviousGuess.getXValue(), PreviousGuess.getYValue());
                }
            } else {
                NextGuess = new Coordinate(PreviousGuess.getXValue(), interpolatedValue);
                if (interpolatedValue < PreviousGuess.getYValue()) {
                    UpperBoundary = new Coordinate(PreviousGuess.getXValue(), PreviousGuess.getYValue());
                } else if (interpolatedValue > PreviousGuess.getYValue()) {
                    LowerBoundary = new Coordinate(PreviousGuess.getXValue(), PreviousGuess.getYValue());
                }
            }

            // Stop condition determines whether we've fully optimised
            if (Math.abs(Function.evaluate(PreviousGuess) - Function.evaluate(NextGuess)) < getTolerance()) {
                setFinalCoordinate(NextGuess);
                return;
            } else {
                // Re-iterate
                PreviousGuess = new Coordinate(NextGuess.getXValue(), NextGuess.getYValue());
            }
        }
    }
}
