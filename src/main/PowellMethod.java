package main;

import java.io.Serializable;
import java.util.ArrayList;

/*
    Powell Method Class
 */
public class PowellMethod extends Thread implements Serializable {
    private static final long serialVersionUID = -4101739456805897681L;
    private double Tolerance;
    private double Bounds;
    private Coordinate StartPoint;
    //TODO implement fatal exception handler in UI thread
    private boolean Completed = false;
    private boolean FatalExceptionOccurred = false;
    private boolean FatalErrorOccurred = false;
    private transient LinMin linMin;
    private ArrayList<Coordinate> UnitVectorSearchList = new ArrayList<>();
    private ArrayList<Coordinate> ConjugateDirectionSearchList = new ArrayList<>();
    private Coordinate finalCoordinate;
    private String function;
    private transient ConjugateDirectionSearch conjugateDirectionSearch;

    public PowellMethod(double tolerance, double bounds, Coordinate startPoint, SearchMethod searchMethod, String function) {
        Tolerance = tolerance;
        Bounds = bounds;
        StartPoint = startPoint;
        this.function = function;
        switch (searchMethod) {
            case binarySearch:
                setLinMin(new BinarySearch());
                break;
            case goldenSectionSearch:

                break;
        }
        setConjugateDirectionSearch(new ConjugateDirectionSearch());
    }

    public PowellMethod() {

    }

    public ConjugateDirectionSearch getConjugateDirectionSearch() {
        return conjugateDirectionSearch;
    }

    public void setConjugateDirectionSearch(ConjugateDirectionSearch conjugateDirectionSearch) {
        this.conjugateDirectionSearch = conjugateDirectionSearch;
    }

    public double getTolerance() {
        return Tolerance;
    }

    public double getBounds() {
        return Bounds;
    }

// --Commented out by Inspection START (05/02/2017 22:09):
//    public void setTolerance(double tolerance) {
//        Tolerance = tolerance;
//    }
// --Commented out by Inspection STOP (05/02/2017 22:09)

    public Coordinate getStartPoint() {
        return StartPoint;
    }

// --Commented out by Inspection START (05/02/2017 22:09):
//    public void setBounds(double bounds) {
//        Bounds = bounds;
//    }
// --Commented out by Inspection STOP (05/02/2017 22:09)

    @Override
    public synchronized void start() {
        super.start();
        // declare and initialise variables where necessary
        int Iterations = 0;
        boolean optimisingByX, initialRun;
        initialRun = true;
        optimisingByX = false;
        int ListSize;
        getUnitVectorSearchList().add(getStartPoint());
        // loop over linmin
        while (true) {
            // Set up the linmin object
            ListSize = getUnitVectorSearchList().size();
            getLinMin().setStartPoint(initialRun ? getStartPoint() : getUnitVectorSearchList().get(ListSize - 1));
            getLinMin().setTolerance(getTolerance());
            getLinMin().setBounds(getBounds());
            getLinMin().setSearchDirection(optimisingByX ? SearchDirection.Vector_I : SearchDirection.Vector_J);
            // Execute linmin
            try {
                getLinMin().startSearch();
            } catch (EvaluationException e) {
                // If anything adverse occurs, exit the thread
                setFatalErrorOccured();
                return;
            }
            // Adds linmin result to list
            getUnitVectorSearchList().add(getLinMin().getFinalCoordinate());

            ListSize = getUnitVectorSearchList().size();
            //reset last and current points
            Coordinate last = getUnitVectorSearchList().get(ListSize - 2);
            Coordinate current = getLinMin().getFinalCoordinate();
            // calculates tolerance in case we're already at minimum
            try {
                if (Math.abs(Function.outputFOfXY(last) - Function.outputFOfXY(current))
                        < getTolerance()) {
                    break;
                } else {
                    optimisingByX = !optimisingByX;
                    initialRun = false;
                    Iterations += 1;
                }
            } catch (EvaluationException e) {
                setFatalErrorOccured();
                setCompleted(false);
                return;
            }
            if (Iterations > 7) {
                break;
            }
        }


        // Begin conjugate direction search
        getConjugateDirectionSearch().setStartPoint(getUnitVectorSearchList().get(getUnitVectorSearchList().size() - 1));
        Coordinate lastPoint = getUnitVectorSearchList().get(getUnitVectorSearchList().size() - 1);
        Coordinate thirdFromLastPoint = getUnitVectorSearchList().get(getUnitVectorSearchList().size() - 3);
        getConjugateDirectionSearch().setVector(lastPoint, thirdFromLastPoint);
        getConjugateDirectionSearch().setBounds(getBounds());
        getConjugateDirectionSearch().setTolerance(getTolerance());
        //Execute conjugate direction search
        try {
            getConjugateDirectionSearch().start();
            setConjugateDirectionSearchList(getConjugateDirectionSearch().getConjugateDirectionSearchList());
            setFinalCoordinate(getConjugateDirectionSearch().getFinalCoordinate());
            System.out.println(getFinalCoordinate().toString());
            setCompleted(true);
        } catch (EvaluationException e) {
            //TODO wtf?
            setFatalErrorOccured();
            setFatalExceptionOccured();
            setConjugateDirectionSearchList(getConjugateDirectionSearch().getConjugateDirectionSearchList());
            setConjugateDirectionSearch(null);
        }

    }

// --Commented out by Inspection START (05/02/2017 22:09):
//    public void setStartPoint(Coordinate startPoint) {
//        StartPoint = startPoint;
//    }
// --Commented out by Inspection STOP (05/02/2017 22:09)

    public void setCompleted(boolean completed) {
        Completed = completed;
    }

// --Commented out by Inspection START (05/02/2017 22:09):
//    public boolean isCompleted() {
//        return Completed;
//    }
// --Commented out by Inspection STOP (05/02/2017 22:09)

    public void setFatalErrorOccured() {
        FatalErrorOccurred = true;
    }

// --Commented out by Inspection START (05/02/2017 22:09):
//    public boolean isFatalErrorOccured() {
//        return FatalErrorOccurred;
//    }
// --Commented out by Inspection STOP (05/02/2017 22:09)

    public ArrayList<Coordinate> getUnitVectorSearchList() {
        return UnitVectorSearchList;
    }

    public void setUnitVectorSearchList(ArrayList<Coordinate> unitVectorSearchList) {
        UnitVectorSearchList = unitVectorSearchList;
    }

    public ArrayList<Coordinate> getConjugateDirectionSearchList() {
        return ConjugateDirectionSearchList;
    }

    public void setConjugateDirectionSearchList(ArrayList<Coordinate> conjugateDirectionSearchList) {
        ConjugateDirectionSearchList = conjugateDirectionSearchList;
    }

    public LinMin getLinMin() {
        return linMin;
    }

    public void setLinMin(LinMin linMin) {
        this.linMin = linMin;
    }

// --Commented out by Inspection START (05/02/2017 22:09):
//    public boolean isFatalExceptionOccured() {
//        return FatalExceptionOccurred;
//    }
// --Commented out by Inspection STOP (05/02/2017 22:09)

    public void setFatalExceptionOccured() {
        FatalExceptionOccurred = true;
    }

    public Coordinate getFinalCoordinate() {
        return finalCoordinate;
    }

    public void setFinalCoordinate(Coordinate finalCoordinate) {
        this.finalCoordinate = finalCoordinate;
    }

// --Commented out by Inspection START (05/02/2017 22:09):
//    public SearchMethod getSearchMethod() {
//        return searchMethod;
//    }
// --Commented out by Inspection STOP (05/02/2017 22:09)

    public void setSearchMethod(SearchMethod searchMethod) {
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }
}