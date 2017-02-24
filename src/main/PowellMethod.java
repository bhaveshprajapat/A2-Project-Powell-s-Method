package main;

import java.io.Serializable;
import java.util.ArrayList;

/*
    Powell Method Thread Class that can be written to a binary file
 */
public class PowellMethod extends Thread implements Serializable {
    private static final long serialVersionUID = -4101739456805897681L;
    private Function function;
    private double Tolerance;
    private double Bounds;
    private Coordinate StartPoint;
    private boolean FatalExceptionOccurred;
    private transient LinMin linMin;
    private ArrayList<Coordinate> UnitVectorSearchList = new ArrayList<>();
    private ArrayList<Coordinate> ConjugateDirectionSearchList = new ArrayList<>();
    private Coordinate finalCoordinate;
    private transient ConjugateDirectionExponentialSearch exponentialSearch;
    private volatile boolean stopThreadFlag;

    // Class constructor
    public PowellMethod(double tolerance, double bounds, Coordinate startPoint, SearchMethod searchMethod) {
        Tolerance = tolerance;
        Bounds = bounds;
        StartPoint = startPoint;
        function = new Function();
        // initialise linMin with the correct search method
        switch (searchMethod) {
            case binarySearch:
                setLinMin(new BinarySearch());
                break;
            case goldenSectionSearch:
                setLinMin(new GoldenSectionSearch());
                break;
        }
        setExponentialSearch(new ConjugateDirectionExponentialSearch());
    }

    public ConjugateDirectionExponentialSearch getExponentialSearch() {
        return exponentialSearch;
    }

    public void setExponentialSearch(ConjugateDirectionExponentialSearch exponentialSearch) {
        this.exponentialSearch = exponentialSearch;
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
            if (stopThreadFlag) {
                return;
            }
            // Set up the linmin object
            ListSize = getUnitVectorSearchList().size();
            if (initialRun) {
                getLinMin().setStartPoint(getStartPoint());
            } else {
                getLinMin().setStartPoint(getUnitVectorSearchList().get(ListSize - 1));
            }
            getLinMin().setTolerance(getTolerance());
            getLinMin().setBounds(getBounds());
            if (optimisingByX) {
                getLinMin().setSearchDirection(SearchDirection.Vector_I);
            } else {
                getLinMin().setSearchDirection(SearchDirection.Vector_J);
            }
            // Execute linmin
            try {
                getLinMin().startSearch();
            } catch (EvaluationException e) {
                // If anything adverse occurs, exit the thread
                setFatalExceptionOccured();
                return;
            }
            // Check for a stop, since linmin was a blocking action
            if (stopThreadFlag) {
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
                if ((Math.abs(function.outputFOfXY(last) - function.outputFOfXY(current))
                        < getTolerance()) && (Iterations > 3)) {
                    break;
                } else {
                    // run another optmisation
                    optimisingByX = !optimisingByX;
                    initialRun = false;
                    Iterations += 1;
                }
            } catch (EvaluationException e) {
                // some fatal error occured
                setFatalExceptionOccured();
                return;
            }
            // exit the method
            if (Iterations > 3) {
                break;
            }
        }
        if (stopThreadFlag) {
            return;
        }
        // initialise conjugate direction search
        /*getConjugateDirectionSearch().setStartPoint(getUnitVectorSearchList().get(getUnitVectorSearchList().size() - 1));
        Coordinate lastPoint = getUnitVectorSearchList().get(getUnitVectorSearchList().size() - 1);
        Coordinate thirdFromLastPoint = getUnitVectorSearchList().get(getUnitVectorSearchList().size() - 3);
        getConjugateDirectionSearch().setVector(lastPoint, thirdFromLastPoint);
        MainSceneController.getLog().add("VECTOR"+getConjugateDirectionSearch().getVectorX() + " " + getConjugateDirectionSearch().getVectorY());
        getConjugateDirectionSearch().setBounds(getBounds());
        getConjugateDirectionSearch().setTolerance(getTolerance());
        //Execute conjugate direction search
        try {
            getConjugateDirectionSearch().start();
        } catch (EvaluationException e) {
            // catch any errors that occur during running
            setFatalExceptionOccured();
            setConjugateDirectionSearchList(getConjugateDirectionSearch().getConjugateDirectionSearchList());
            setConjugateDirectionSearch(null);
        }
        if(stopThreadFlag){return;}
        setConjugateDirectionSearchList((ArrayList<Coordinate>) getConjugateDirectionSearch().getConjugateDirectionSearchList().clone());
        setFinalCoordinate(getConjugateDirectionSearch().getFinalCoordinate());*/

        // initialise exponential search
        Coordinate newStartPoint = getUnitVectorSearchList().get(getUnitVectorSearchList().size() - 1);
        getExponentialSearch().setStartPoint(newStartPoint);
        getExponentialSearch().setTolerance(getTolerance());
        getExponentialSearch().setVector(getUnitVectorSearchList().get(getUnitVectorSearchList().size() - 3), newStartPoint);
        MainSceneController.getLog().add("VECTOR" + getExponentialSearch().getxVector() + " " + getExponentialSearch().getyVector());
        if (getExponentialSearch().getStartPoint().equals(getStartPoint())) {
            setFinalCoordinate(getStartPoint());
            MainSceneController.getLog().add("No conjugate direction optimisation performed, already at minimum.");
            return;
        }
        if ((getExponentialSearch().getxVector() == 0D) && (getExponentialSearch().getyVector() == 0D)) {
            setFinalCoordinate(getStartPoint());
            MainSceneController.getLog().add("No conjugate direction optimisation performed, already at minimum.");
            return;
        }
        if (stopThreadFlag) {
            return;
        }
        try {
            getExponentialSearch().start();
        } catch (EvaluationException e) {
            // catch any errors that occur during running
            setFatalExceptionOccured();
        }
        if (stopThreadFlag) {
            return;
        }
        setConjugateDirectionSearchList((ArrayList<Coordinate>) getExponentialSearch().getSearchList().clone());
        setFinalCoordinate(getExponentialSearch().getFinalCoordinate());

    }

    public ArrayList<Coordinate> getUnitVectorSearchList() {
        return UnitVectorSearchList;
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
        setFatalExceptionOccurred(true);
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
    public String getFunctionString() {
        return function.getInfixExpression();
    }

    public boolean isFatalExceptionOccurred() {
        return FatalExceptionOccurred;
    }

    public void setFatalExceptionOccurred(boolean fatalExceptionOccurred) {
        FatalExceptionOccurred = fatalExceptionOccurred;
    }

    public boolean isStopThreadFlag() {
        return this.stopThreadFlag;
    }

    public void setStopThreadFlag(boolean stopThreadFlag) {
        this.stopThreadFlag = stopThreadFlag;
    }
}