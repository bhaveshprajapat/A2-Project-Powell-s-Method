package main;

/*
    Abstract class to define line minimisation class behaviour
 */
public abstract class LinMin {
    // Private fields
    private static int Counter;
    private double tolerance;
    private double bounds;
    private Coordinate startPoint;
    private SearchDirection searchDirection;
    private Coordinate finalCoordinate;

    public static int getCounter() {
        return Counter;
    }

    public static void setCounter(int counter) {
        Counter = counter;
    }

    // Accessor method for tolerance field
    public final double getTolerance() {
        return tolerance;
    }

    //Mutator method for tolerance field
    public final void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    // Accessor method for bounds field
    public final double getBounds() {
        return bounds;
    }

    // Mutator method for bounds field
    public final void setBounds(double bounds) {
        this.bounds = bounds;
    }

    // Accessor method for start point coordinate field
    public final Coordinate getStartPoint() {
        return startPoint;
    }

    // Mutator method for start point coordinate field
    public final void setStartPoint(Coordinate startPoint) {
        this.startPoint = startPoint;
    }

    // Accessor method for search direction field
    public final SearchDirection getSearchDirection() {
        return searchDirection;
    }

    // Mutator method for search direction field
    public final void setSearchDirection(SearchDirection searchDirection) {
        this.searchDirection = searchDirection;
    }

    // Accessor method for final coordinate field
    public final Coordinate getFinalCoordinate() {
        return finalCoordinate;
    }

    // Mutator method for final coordinate field
    public final void setFinalCoordinate(Coordinate finalCoordinate) {
        this.finalCoordinate = finalCoordinate;
    }

    /*
     * This method will perform a one dimensional search across the unit vector
     * specified in the search direction field, from the start coordinate
     * field, within the bounds from the bounds field, and the tolerance
     * from the tolerance field.
     */
    abstract void startSearch() throws EvaluationException;
}