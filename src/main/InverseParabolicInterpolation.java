package main;

/*
    Line Minimisation class performs an Inverse Parabolic Interpolation in one dimension
 */
public class InverseParabolicInterpolation extends LinMin {

    @Override
    void startSearch() throws EvaluationException {
        // Initialise placeholder variables
        Coordinate PreviousGuess = getStartPoint();
        Coordinate NextGuess;
        // Create direction vectors and set their values
        boolean OptimisingByX = getSearchDirection() == SearchDirection.VECTOR_I;
        int VectorX = OptimisingByX ? 1 : 0;
        int VectorY = OptimisingByX ? 0 : 1;
        // Creates lower boundary
        double LowerBoundaryXValue = PreviousGuess.getXValue() - (VectorX * getBounds());
        double LowerBoundaryYValue = PreviousGuess.getYValue() - (VectorY * getBounds());
        Coordinate LowerBoundary = new Coordinate(LowerBoundaryXValue, LowerBoundaryYValue);
        // Creates upper boundary
        double UpperBoundaryXValue = PreviousGuess.getXValue() + (VectorX * getBounds());
        double UpperBoundaryYValue = PreviousGuess.getYValue() + (VectorY * getBounds());
        Coordinate UpperBoundary = new Coordinate(UpperBoundaryXValue, UpperBoundaryYValue);

        while (true) {
            // Increment optimisation counter
            LinMin.setCounter(LinMin.getCounter() + 1);
            /*
                a, b & c are named as such to ease readability
             */
            double a; // Lower bound
            double b; // Current guess
            double c; // Upper bound
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
            double numerator = (Math.pow(b - a, 2.0) * (Function.evaluate(PreviousGuess) - Function.evaluate(UpperBoundary))) - (Math.pow(b - c, 2.0) * (Function.evaluate(PreviousGuess) - Function.evaluate(LowerBoundary)));
            double denominator = ((b - a) * (Function.evaluate(PreviousGuess) - Function.evaluate(UpperBoundary))) - ((b - c) * (Function.evaluate(PreviousGuess) - Function.evaluate(LowerBoundary)));
            double interpolatedValue = b - (0.5 * (numerator / denominator));
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
