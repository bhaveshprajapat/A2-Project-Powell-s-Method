package main;

import java.io.Serializable;
import java.util.ArrayList;

/*
    Powell Method Class that can be written to a binary file
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

    // Class constructor
    public PowellMethod(double tolerance, double bounds, Coordinate startPoint, SearchMethod searchMethod, String function) {
        Tolerance = tolerance;
        Bounds = bounds;
        StartPoint = startPoint;
        this.function = function;
        // initialise linMin with the correct search method
        switch (searchMethod) {
            case binarySearch:
                setLinMin(new BinarySearch());
                break;
            case goldenSectionSearch:
                setLinMin(new GoldenSectionSearch());
                break;
        }
        setConjugateDirectionSearch(new ConjugateDirectionSearch());
    }

    //Getter method for conjugate direction search
    public ConjugateDirectionSearch getConjugateDirectionSearch() {
        return conjugateDirectionSearch;
    }

    // setter for conjugate direction search
    public void setConjugateDirectionSearch(ConjugateDirectionSearch conjugateDirectionSearch) {
        this.conjugateDirectionSearch = conjugateDirectionSearch;
    }

    // getter for tolerance
    public double getTolerance() {
        return Tolerance;
    }

    // getter for bounds
    public double getBounds() {
        return Bounds;
    }

    // gettter for start point
    public Coordinate getStartPoint() {
        return StartPoint;
    }

    // start method for powell method search that overrides start in java.lang.Thread
    @Override
    public synchronized void start() {
        super.start();
        // declare and initialise variables where necessary
        int Iterations = 0;
        boolean optimisingByX, initialRun;
        initialRun = true;
        optimisingByX = false;
        int ListSize;
        // add start point to list
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
                    // run another optmisation
                    optimisingByX = !optimisingByX;
                    initialRun = false;
                    Iterations += 1;
                }
            } catch (EvaluationException e) {
                // some fatal error occured
                setFatalErrorOccured();
                setCompleted(false);
                return;
            }
            // exit the method
            if (Iterations > 7) {
                break;
            }
        }
        // initialise conjugate direction search
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
            // catch any errors that occur during running
            setFatalErrorOccured();
            setFatalExceptionOccured();
            setConjugateDirectionSearchList(getConjugateDirectionSearch().getConjugateDirectionSearchList());
            setConjugateDirectionSearch(null);
        }
    }

    // completed boolean setter
    public void setCompleted(boolean completed) {
        Completed = completed;
    }

    // fatal error boolean setter
    public void setFatalErrorOccured() {
        FatalErrorOccurred = true;
    }

    public ArrayList<Coordinate> getUnitVectorSearchList() {
        return UnitVectorSearchList;
    }

    public void setUnitVectorSearchList(ArrayList<Coordinate> unitVectorSearchList) {
        UnitVectorSearchList = unitVectorSearchList;
    }

    // getter for conjugate direction search list
    public ArrayList<Coordinate> getConjugateDirectionSearchList() {
        return ConjugateDirectionSearchList;
    }

    // setter for conjugate direction search list
    public void setConjugateDirectionSearchList(ArrayList<Coordinate> conjugateDirectionSearchList) {
        ConjugateDirectionSearchList = conjugateDirectionSearchList;
    }

    // getter for linmin
    public LinMin getLinMin() {
        return linMin;
    }

    // setter for linmin
    public void setLinMin(LinMin linMin) {
        this.linMin = linMin;
    }

    // fatal exception boolean setter
    public void setFatalExceptionOccured() {
        FatalExceptionOccurred = true;
    }

    // getter for final coordinate
    public Coordinate getFinalCoordinate() {
        return finalCoordinate;
    }

    // setter for final coordinate
    public void setFinalCoordinate(Coordinate finalCoordinate) {
        this.finalCoordinate = finalCoordinate;
    }

    // getter for function
    public String getFunction() {
        return function;
    }
}