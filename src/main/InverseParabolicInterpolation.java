package main;

/*
    Class to perform an inverse parabolic interpolation in one dimension
 */
public class InverseParabolicInterpolation extends LinMin {

    @Override
    void startSearch() throws EvaluationException {
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

            double a;
            double b;
            double c;
            if (OptimisingByX) {
                a = LowerBoundary.getXValue();
                b = PreviousGuess.getXValue();
                c = UpperBoundary.getXValue();
            } else {
                a = LowerBoundary.getYValue();
                b = PreviousGuess.getYValue();
                c = UpperBoundary.getYValue();
            }
            LinMin.setCounter(LinMin.getCounter() + 1);


            double numerator = (Math.pow(b - a, 2.0) * (Function.evaluate(PreviousGuess) - Function.evaluate(UpperBoundary))) - (StrictMath.pow(b - c, 2.0) * (Function.evaluate(PreviousGuess) - Function.evaluate(LowerBoundary)));
            double denominator = ((b - a) * (Function.evaluate(PreviousGuess) - Function.evaluate(UpperBoundary))) - ((b - c) * (Function.evaluate(PreviousGuess) - Function.evaluate(LowerBoundary)));

            double interpolate = b - (0.5 * (numerator / denominator));

            if (OptimisingByX) {
                NextGuess = new Coordinate(interpolate, PreviousGuess.getYValue());

                if (interpolate < PreviousGuess.getXValue()) {
                    UpperBoundary = new Coordinate(PreviousGuess.getXValue(), PreviousGuess.getYValue());
                } else if (interpolate > PreviousGuess.getXValue()) {
                    LowerBoundary = new Coordinate(PreviousGuess.getXValue(), PreviousGuess.getYValue());
                }

            } else {
                NextGuess = new Coordinate(PreviousGuess.getXValue(), interpolate);
                if (interpolate < PreviousGuess.getYValue()) {
                    UpperBoundary = new Coordinate(PreviousGuess.getXValue(), PreviousGuess.getYValue());
                } else if (interpolate > PreviousGuess.getYValue()) {
                    LowerBoundary = new Coordinate(PreviousGuess.getXValue(), PreviousGuess.getYValue());
                }
            }
            //Stop condition
            if (Math.abs(Function.evaluate(PreviousGuess) - Function.evaluate(NextGuess)) < getTolerance()) {
                setFinalCoordinate(NextGuess);
                return;
            } else {
                PreviousGuess = new Coordinate(NextGuess.getXValue(), NextGuess.getYValue());
            }
        }
    }
}
