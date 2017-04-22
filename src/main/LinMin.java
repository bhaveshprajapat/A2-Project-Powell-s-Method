package main;

/*
    Abstract class to define line minimisation class behaviour
 */
public abstract class LinMin {
    private static int Counter;
    // Private fields
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
    public double getTolerance() {
        return tolerance;
    }

    //Mutator method for tolerance field
    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    // Accessor method for bounds field
    public double getBounds() {
        return bounds;
    }

    // Mutator method for bounds field
    public void setBounds(double bounds) {
        this.bounds = bounds;
    }

    // Accessor method for start point coordinate field
    public Coordinate getStartPoint() {
        return startPoint;
    }

    // Mutator method for start point coordinate field
    public void setStartPoint(Coordinate startPoint) {
        this.startPoint = startPoint;
    }

    // Accessor method for search direction field
    public SearchDirection getSearchDirection() {
        return searchDirection;
    }

    // Mutator method for search direction field
    public void setSearchDirection(SearchDirection searchDirection) {
        this.searchDirection = searchDirection;
    }

    // Accessor method for final coordinate field
    public Coordinate getFinalCoordinate() {
        return finalCoordinate;
    }

    // Mutator method for final coordinate field
    public void setFinalCoordinate(Coordinate finalCoordinate) {
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