package main;

/*
    Abstract class to define linmin behaviour
 */
public abstract class LinMin {
    private double Tolerance;
    private double Bounds;
    private Coordinate StartPoint;
    private SearchDirection SearchDirection;
    private Coordinate FinalCoordinate;

    public double getTolerance() {
        return Tolerance;
    }

    public void setTolerance(double tolerance) {
        this.Tolerance = tolerance;
    }

    public double getBounds() {
        return Bounds;
    }

    public void setBounds(double bounds) {
        this.Bounds = bounds;
    }

    public Coordinate getStartPoint() {
        return StartPoint;
    }

    public void setStartPoint(Coordinate startPoint) {
        StartPoint = startPoint;
    }

    public SearchDirection getSearchDirection() {
        return SearchDirection;
    }

    public void setSearchDirection(SearchDirection searchDirection) {
        this.SearchDirection = searchDirection;
    }

    public Coordinate getFinalCoordinate() {
        return FinalCoordinate;
    }

    public void setFinalCoordinate(Coordinate finalCoordinate) {
        this.FinalCoordinate = finalCoordinate;
    }

    /*
     * This method should perform a one dimensional search across the unit vector
     * specified in SearchDirection
     */
    abstract void startSearch() throws EvaluationException;
}