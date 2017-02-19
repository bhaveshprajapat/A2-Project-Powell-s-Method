package main;

/*
    Abstract class to define linmin behaviour
 */
public abstract class LinMin {
    private double tolerance;
    private double bounds;
    private Coordinate StartPoint;
    private SearchDirection searchDirection;
    private Coordinate finalCoordinate;
    public double getTolerance() {
        return tolerance;
    }
    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }
    public double getBounds() {
        return bounds;
    }
    public void setBounds(double bounds) {
        this.bounds = bounds;
    }
    public Coordinate getStartPoint() {
        return StartPoint;
    }
    public void setStartPoint(Coordinate startPoint) {
        StartPoint = startPoint;
    }
    public SearchDirection getSearchDirection() {
        return searchDirection;
    }
    public void setSearchDirection(SearchDirection searchDirection) {
        this.searchDirection = searchDirection;
    }
    public Coordinate getFinalCoordinate() {
        return finalCoordinate;
    }
    public void setFinalCoordinate(Coordinate finalCoordinate) {
        this.finalCoordinate = finalCoordinate;
    }
    /**
     * This method should perform a one dimensional search across the unit vector
     * specified in searchDirection
     */
    abstract void startSearch() throws EvaluationException;
}