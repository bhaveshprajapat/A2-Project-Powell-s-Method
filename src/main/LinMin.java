package main;

/*
    Abstract class to define line minimisation class behaviour
 */
public abstract class LinMin {
    private static int Counter;
    // Private fields
    private double Tolerance;
    private double Bounds;
    private Coordinate StartPoint;
    private SearchDirection SearchDirection;
    private Coordinate FinalCoordinate;

    public static int getCounter() {
        return Counter;
    }

    public static void setCounter(int counter) {
        Counter = counter;
    }

    // Accessor method for tolerance field
    public double getTolerance() {
        return Tolerance;
    }

    //Mutator method for tolerance field
    public void setTolerance(double tolerance) {
        this.Tolerance = tolerance;
    }

    // Accessor method for bounds field
    public double getBounds() {
        return Bounds;
    }

    // Mutator method for bounds field
    public void setBounds(double bounds) {
        this.Bounds = bounds;
    }

    // Accessor method for start point coordinate field
    public Coordinate getStartPoint() {
        return StartPoint;
    }

    // Mutator method for start point coordinate field
    public void setStartPoint(Coordinate startPoint) {
        StartPoint = startPoint;
    }

    // Accessor method for search direction field
    public SearchDirection getSearchDirection() {
        return SearchDirection;
    }

    // Mutator method for search direction field
    public void setSearchDirection(SearchDirection searchDirection) {
        this.SearchDirection = searchDirection;
    }

    // Accessor method for final coordinate field
    public Coordinate getFinalCoordinate() {
        return FinalCoordinate;
    }

    // Mutator method for final coordinate field
    public void setFinalCoordinate(Coordinate finalCoordinate) {
        this.FinalCoordinate = finalCoordinate;
    }

    /*
     * This method will perform a one dimensional search across the unit vector
     * specified in the search direction field, from the start coordinate
     * field, within the bounds from the bounds field, and the tolerance
     * from the tolerance field.
     */
    abstract void startSearch() throws EvaluationException;
}