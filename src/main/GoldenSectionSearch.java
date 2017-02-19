package main;
public class GoldenSectionSearch extends LinMin {
    @Override
    void startSearch() throws EvaluationException {
        Function function = new Function();
        boolean optimisingByX = (getSearchDirection() == SearchDirection.Vector_I);
        double previousZValue = Function.outputFOfXY(getStartPoint());
        int vectorX = (getSearchDirection() == SearchDirection.Vector_J) ? 1 : 0;
        int vectorY = (getSearchDirection() == SearchDirection.Vector_J) ? 1 : 0;
        double lowerBoundaryXValue = getStartPoint().getXValue() - (vectorX * (getBounds()));
        double lowerBoundaryYValue = getStartPoint().getYValue() - (vectorY * (getBounds()));
        Coordinate lowerBound = new Coordinate(lowerBoundaryXValue, lowerBoundaryYValue);
        // Creates upper boundary
        double upperBoundaryXValue = getStartPoint().getXValue() + (vectorX * (getBounds()));
        double upperBoundaryYValue = getStartPoint().getYValue() + (vectorY * (getBounds()));
        Coordinate upperBound = new Coordinate(upperBoundaryXValue, upperBoundaryYValue);
        while (true) {
            double tau = (1 + Math.sqrt(5)) / 2;
            // Create the golden sections within the boundaries
            Coordinate goldenSection1 = new Coordinate(
                    optimisingByX ? (upperBound.getXValue() - ((upperBound.getXValue() - lowerBound.getXValue()) / tau)) : lowerBound.getXValue(),
                    optimisingByX ? lowerBound.getYValue() : (upperBound.getYValue() - ((upperBound.getYValue() - lowerBound.getYValue()) / tau)));
            Coordinate goldenSection2 = new Coordinate(
                    optimisingByX ? (lowerBound.getXValue() + ((upperBound.getXValue() - lowerBound.getXValue()) / tau)) : lowerBound.getXValue(),
                    optimisingByX ? lowerBound.getYValue() : (lowerBound.getYValue() + ((upperBound.getYValue() - lowerBound.getYValue()) / tau)));
            // Evaluate each golden section coordinate
            double zOfGoldenSection1 = Function.outputFOfXY(goldenSection1);
            double zOfGoldenSection2 = Function.outputFOfXY(goldenSection2);
            Coordinate newLowerBound, newUpperBound;
            // If the second golden section is highest, set the upper bound to the second section
            if (zOfGoldenSection1 < zOfGoldenSection2) {
                newLowerBound = new Coordinate(lowerBound.getXValue(), lowerBound.getYValue());
                newUpperBound = new Coordinate(goldenSection2.getXValue(), goldenSection2.getYValue());
            } else {
                // Otherwise set the lower bound to the first section
                newLowerBound = new Coordinate(goldenSection1.getXValue(), goldenSection1.getYValue());
                newUpperBound = new Coordinate(upperBound.getXValue(), upperBound.getYValue());
            }
            // Finds and creates the midpoint
            Coordinate midpoint = new Coordinate((lowerBound.getXValue() + upperBound.getXValue()) / 2, (lowerBound.getYValue() + upperBound.getYValue()) / 2);
        /*
            If a previous z value is provided, evaluate the tolerance and exit if the tolerance is met
            Otherwise, recurse this function with restricted bounds
         */
            if (Math.abs(previousZValue - Function.outputFOfXY(midpoint)) < getTolerance()) {
                setFinalCoordinate(midpoint);
                return;
            } else {
                lowerBound = newLowerBound;
                upperBound = newUpperBound;
                previousZValue = Function.outputFOfXY(midpoint);
            }
        }
    }
}