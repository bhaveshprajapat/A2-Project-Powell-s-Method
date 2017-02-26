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
    private transient PowellMethod.ConjugateDirectionExponentialSearch exponentialSearch;
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
    }

    public PowellMethod.ConjugateDirectionExponentialSearch getExponentialSearch() {
        return exponentialSearch;
    }

    public void setExponentialSearch(PowellMethod.ConjugateDirectionExponentialSearch exponentialSearch) {
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
                if ((Math.abs(function.evaluate(last) - function.evaluate(current))
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

        // initialise exponential search
        Coordinate newStartPoint = getUnitVectorSearchList().get(getUnitVectorSearchList().size() - 1);
        setExponentialSearch(new PowellMethod.ConjugateDirectionExponentialSearch(newStartPoint, getTolerance()));
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
        setConjugateDirectionSearchList(getExponentialSearch().getCoordinatesOptimisedList());
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
        return stopThreadFlag;
    }

    public void setStopThreadFlag(boolean stopThreadFlag) {
        this.stopThreadFlag = stopThreadFlag;
    }

    public class ConjugateDirectionExponentialSearch {
        private Coordinate startPoint;
        private double xVector;
        private double yVector;
        private Coordinate finalCoordinate;
        private ArrayList<Coordinate> coordinatesOptimisedList = new ArrayList<>();
        private double tolerance;

        public ConjugateDirectionExponentialSearch(Coordinate startPoint, double tolerance) {
            this.startPoint = startPoint;
            this.tolerance = tolerance;
        }

        public Coordinate getFinalCoordinate() {
            return finalCoordinate;
        }

        public void setFinalCoordinate(Coordinate finalCoordinate) {
            this.finalCoordinate = finalCoordinate;
        }

        public ArrayList<Coordinate> getCoordinatesOptimisedList() {
            return coordinatesOptimisedList;
        }

        public void start() throws EvaluationException {
            coordinatesOptimisedList.add(startPoint);
            Function function = new Function();
            // declare local variables
            int maxpowerof2 = 2;
            Coordinate coordinate1, coordinate2, coordinate3;
            coordinate1 = startPoint;
            double tempXValue, tempYValue;
            double fOfCoordinate1, fOfCoordinate2, fOfCoordinate3;
            // while loop to increase the power of 2
            while (true) {
                // generate three coordinates with three different powers
                tempXValue = coordinate1.getXValue() + scaleVectorByPowerOf2(xVector, maxpowerof2 - 2);
                tempYValue = coordinate1.getYValue() + scaleVectorByPowerOf2(yVector, maxpowerof2 - 2);
                coordinate1 = new Coordinate(tempXValue, tempYValue);
                tempXValue = coordinate1.getXValue() + scaleVectorByPowerOf2(xVector, maxpowerof2 - 1);
                tempYValue = coordinate1.getYValue() + scaleVectorByPowerOf2(yVector, maxpowerof2 - 1);
                coordinate2 = new Coordinate(tempXValue, tempYValue);
                tempXValue = coordinate1.getXValue() + scaleVectorByPowerOf2(xVector, maxpowerof2);
                tempYValue = coordinate1.getYValue() + scaleVectorByPowerOf2(yVector, maxpowerof2);
                coordinate3 = new Coordinate(tempXValue, tempYValue);

                fOfCoordinate1 = function.evaluate(coordinate1);
                fOfCoordinate2 = function.evaluate(coordinate2);
                fOfCoordinate3 = function.evaluate(coordinate3);
                if ((fOfCoordinate3 > fOfCoordinate2) && (fOfCoordinate3 > fOfCoordinate1)) {
                    break;
                } else {
                    maxpowerof2 += 1;
                    coordinatesOptimisedList.add(coordinate3);
                }
            }
            // minimum is now bracketed by coordinate 1 and coordinate 3
            // begin 2D binary search
            double midpointX = (coordinate1.getXValue() + coordinate3.getXValue()) / 2;
            double midpointY = (coordinate1.getYValue() + coordinate3.getYValue()) / 2;
            Coordinate midpoint = new Coordinate(midpointX, midpointY);
            Coordinate upperBound, lowerBound;
            upperBound = coordinate3;
            lowerBound = coordinate1;
            double fOfUpperBound, fOfLowerBound;
            fOfLowerBound = function.evaluate(lowerBound);
            fOfUpperBound = function.evaluate(upperBound);
            coordinatesOptimisedList.add(midpoint);
            while (true) {
                if (fOfLowerBound > fOfUpperBound) {
                    lowerBound = midpoint;
                }
                if (fOfUpperBound > fOfLowerBound) {
                    upperBound = midpoint;
                }
                midpointX = (upperBound.getXValue() + lowerBound.getXValue()) / 2;
                midpointY = (upperBound.getYValue() + lowerBound.getYValue()) / 2;
                Coordinate newMidpoint = new Coordinate(midpointX, midpointY);
                coordinatesOptimisedList.add(newMidpoint);
                double fOfCurrentCoordinate = function.evaluate(coordinatesOptimisedList.get(coordinatesOptimisedList.size() - 1));
                double fOfLastCoordinate = function.evaluate(coordinatesOptimisedList.get(coordinatesOptimisedList.size() - 2));
                if (Math.abs(fOfCurrentCoordinate - fOfLastCoordinate) < tolerance) {
                    setFinalCoordinate(coordinatesOptimisedList.get(coordinatesOptimisedList.size() - 1));
                    break;
                }
            }

        }

        public Coordinate getStartPoint() {
            return startPoint;
        }

        public double getxVector() {
            return xVector;
        }

        public void setxVector(double xVector) {
            this.xVector = xVector;
        }

        public double getyVector() {
            return yVector;
        }

        public void setyVector(double yVector) {
            this.yVector = yVector;
        }

        private double scaleVectorByPowerOf2(double vector, int power) {
            return vector * Math.pow(2, power);
        }

        // sets the vectors on the object
        public void setVector(Coordinate one, Coordinate two) {
            double deltaY = two.getYValue() - one.getYValue();
            double deltaX = two.getXValue() - one.getXValue();
            setxVector(deltaX);
            setyVector(deltaY);
        }
    }
}