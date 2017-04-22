package main;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
    Powell Method Thread Class that can be written to a binary file
 */
public class PowellMethod extends Thread implements Serializable {
    // Private fields
    private static final long serialVersionUID = -4101739456805897681L;
    private double tolerance;
    private double bounds;
    private Coordinate startPoint;
    private boolean evaluationExceptionOccurred;
    private transient LinMin linMin;
    private ArrayList<Coordinate> unitVectorSearchList = new ArrayList<>();
    private List<Coordinate> exponentialSearchList = new ArrayList<>();
    private Coordinate finalCoordinate;
    private volatile boolean threadStoppedFlag;
    private int optimisationCounter;
    private transient ExponentialSearch exponentialSearch;

    // Class constructor
    public PowellMethod(double Tolerance, double Bounds, Coordinate StartPoint, SearchMethod SearchMethod) {
        tolerance = Tolerance;
        bounds = Bounds;
        startPoint = StartPoint;
        // Initialise linMin with the correct search method
        switch (SearchMethod) {
            case BINARY_SEARCH:
                // Load a binary search object
                linMin = new BinarySearch();
                break;
            case GOLDEN_SECTION_SEARCH:
                // Load a golden section search object
                linMin = new GoldenSectionSearch();
                break;
            case INVERSE_PARABOLIC_INTERPOLATION:
                linMin = new InverseParabolicInterpolation();
                break;
        }
    }

    public ExponentialSearch getExponentialSearch() {
        return exponentialSearch;
    }

    // Accessor method for bounds
    public double getBounds() {
        return bounds;
    }

    // startSearch method for powell method search that overrides startSearch in Thread
    @Override
    public synchronized void start() {
        // Call to super method for threading
        super.start();
        // Declare and initialise variables where necessary
        int Iterations = 0;
        boolean OptimisingByX = false;
        boolean InitialRun = true;

        int ListSize;
        // Add startSearch point to list
        unitVectorSearchList.add(startPoint);
        // Loop over linmin
        while (true) {
            if (threadStoppedFlag) {
                return;
            }
            // Set up the linmin object with the values passed in to this class
            ListSize = unitVectorSearchList.size();
            if (InitialRun) {
                linMin.setStartPoint(startPoint);
            } else {
                linMin.setStartPoint(unitVectorSearchList.get(ListSize - 1));
            }
            linMin.setTolerance(tolerance);
            linMin.setBounds(bounds);
            linMin.setSearchDirection(OptimisingByX ? SearchDirection.VECTOR_I : SearchDirection.VECTOR_J);
            // Execute linmin
            try {
                linMin.startSearch();
            } catch (EvaluationException CaughtException) {
                // If anything adverse occurs, exit the thread
                setEvaluationExceptionOccured();
                return;
            }
            // Check for a stop, since linmin was a blocking action
            if (threadStoppedFlag) {
                return;
            }
            // Adds linmin result to list
            unitVectorSearchList.add(linMin.getFinalCoordinate());
            ListSize = unitVectorSearchList.size();
            //reset last and current points
            Coordinate Last = unitVectorSearchList.get(ListSize - 2);
            Coordinate Current = linMin.getFinalCoordinate();
            // Calculates tolerance in case we're already at minimum
            try {
                if ((Math.abs(Function.evaluate(Last) - Function.evaluate(Current))
                        < tolerance) && (Iterations > 3)) {
                    break;
                } else {
                    // Run another optimisation
                    OptimisingByX = !OptimisingByX;
                    InitialRun = false;
                    Iterations += 1;
                }
            } catch (EvaluationException CaughtException) {
                // Some fatal error occured
                setEvaluationExceptionOccured();
                return;
            }
            // Exit the method
            if (Iterations > 3) {
                break;
            }
        }
        if (threadStoppedFlag) {
            return;
        }
        optimisationCounter = LinMin.getCounter();
        // initialise exponential search
        Coordinate NewStartPoint = unitVectorSearchList.get(unitVectorSearchList.size() - 1);
        exponentialSearch = new ExponentialSearch(NewStartPoint, tolerance);
        exponentialSearch.setVector(unitVectorSearchList.get(unitVectorSearchList.size() - 3), NewStartPoint);
        MainSceneController.getLog().add("Exponential Search Vector : " + exponentialSearch.getXVector() + "i. " + exponentialSearch.getYVector() + 'j');
        if (exponentialSearch.getStartPoint().equals(startPoint)) {
            finalCoordinate = startPoint;
            MainSceneController.getLog().add("No conjugate direction optimisation performed, already at minimum.");
            MainSceneController.getLog().add("The method may have been caught inside a local minimum, try higher bounds in this event.");
            return;
        }
        if ((exponentialSearch.getXVector() == 0.0D) && (exponentialSearch.getYVector() == 0.0D)) {
            finalCoordinate = startPoint;
            MainSceneController.getLog().add("No conjugate direction optimisation performed, already at minimum.");
            MainSceneController.getLog().add("The method may have been caught inside a local minimum, try higher bounds in this event.");
            return;
        }
        if (threadStoppedFlag) {
            setThreadStopped();
            return;
        }
        try {
            exponentialSearch.startSearch();
        } catch (EvaluationException CaughtException) {
            // TODO write a better comment than that
            // catch any errors that occur during running
            setEvaluationExceptionOccured();
            return;
        }
        if (threadStoppedFlag) {
            return;
        }
        optimisationCounter = LinMin.getCounter() + exponentialSearch.getOptimisationCounter();
        exponentialSearchList = exponentialSearch.getOptimisedCoordinateList();
        finalCoordinate = exponentialSearch.getFinalCoordinate();
    }

    public List<Coordinate> getUnitVectorSearchList() {
        return Collections.unmodifiableList(unitVectorSearchList);
    }

    // Accessor method for conjugate direction search list
    public List<Coordinate> getExponentialSearchList() {
        return Collections.unmodifiableList(exponentialSearchList);
    }

    // Accessor method for final coordinate
    public Coordinate getFinalCoordinate() {
        return finalCoordinate;
    }

    // Accessor method for function
    public String getFunctionString() {
        return Function.getInfixExpression();
    }

    // Accessor method for fatal exception boolean
    public boolean isEvaluationExceptionOccurred() {
        return evaluationExceptionOccurred;
    }

    // Mutator method for fatal exception
    public void setEvaluationExceptionOccured() {
        evaluationExceptionOccurred = true;
    }

    public boolean isThreadStopped() {
        return threadStoppedFlag;
    }

    public void setThreadStopped() {
        threadStoppedFlag = true;
    }

    public void runTwoDimensionAdjustment() {
        ArrayList<Coordinate> NewUnitVectorList = new ArrayList<>();
        ArrayList<Coordinate> NewConjugateList = new ArrayList<>();
        try {
            for (Coordinate CoordinateStepper : exponentialSearchList) {
                Coordinate newCoordinate = new Coordinate(CoordinateStepper.getXValue(), Function.evaluate(new Coordinate(CoordinateStepper.getXValue(), 0.0D)));
                NewUnitVectorList.add(newCoordinate);
            }
            for (Coordinate CoordinateStepper : unitVectorSearchList) {
                Coordinate newCoordinate = new Coordinate(CoordinateStepper.getXValue(), Function.evaluate(new Coordinate(CoordinateStepper.getXValue(), 0.0D)));
                NewConjugateList.add(newCoordinate);
            }

            finalCoordinate = new Coordinate(finalCoordinate.getXValue(), Function.evaluate(new Coordinate(finalCoordinate.getXValue(), 0.0D)));
            exponentialSearchList = NewConjugateList;
            unitVectorSearchList = NewUnitVectorList;
        } catch (EvaluationException CaughtException) {
            MainSceneController.getLog().add(CaughtException.getMessage());
        }
    }

    public int getOptimisationCounter() {
        return optimisationCounter;
    }

    // Exponential Search Inner Class
    public class ExponentialSearch {
        // Private Fields
        private final Coordinate StartPoint;
        private final ArrayList<Coordinate> OptimisedCoordinateList = new ArrayList<>();
        private final double Tolerance;
        private double XVector;
        private double YVector;
        private Coordinate FinalCoordinate;
        private int OptimisationCounter;
        private boolean divergenceDetected;

        public ExponentialSearch(Coordinate StartPoint, double Tolerance) {
            this.StartPoint = StartPoint;
            this.Tolerance = Tolerance;
        }

        public boolean isDivergenceDetected() {
            return divergenceDetected;
        }

        public Coordinate getFinalCoordinate() {
            return FinalCoordinate;
        }

        public List<Coordinate> getOptimisedCoordinateList() {
            return Collections.unmodifiableList(OptimisedCoordinateList);
        }

        public void startSearch() throws EvaluationException {
            OptimisationCounter = 0;
            OptimisedCoordinateList.add(StartPoint);
            // Declare local variables
            int MaximumPowerOf2 = 2;  // Start at 2 so that 0,1 & 2 can be used initially
            Coordinate CoordinateAtPowerNMinus2 = StartPoint;
            Coordinate CoordinateAtPowerNMinus1;
            Coordinate CoordinateAtPowerN;
            double TempXValue;
            double TempYValue;
            double FOfCoordinate1;
            double FOfCoordinate2;
            double FOfCoordinate3;
            // while loop to increase the power of 2
            while (true) {
                OptimisationCounter = OptimisationCounter + 1;
                // generate three coordinates with three different powers
                TempXValue = CoordinateAtPowerNMinus2.getXValue() + scaleVectorByPowerOf2(XVector, MaximumPowerOf2 - 2);
                TempYValue = CoordinateAtPowerNMinus2.getYValue() + scaleVectorByPowerOf2(YVector, MaximumPowerOf2 - 2);
                CoordinateAtPowerNMinus2 = new Coordinate(TempXValue, TempYValue);
                TempXValue = CoordinateAtPowerNMinus2.getXValue() + scaleVectorByPowerOf2(XVector, MaximumPowerOf2 - 1);
                TempYValue = CoordinateAtPowerNMinus2.getYValue() + scaleVectorByPowerOf2(YVector, MaximumPowerOf2 - 1);
                CoordinateAtPowerNMinus1 = new Coordinate(TempXValue, TempYValue);
                TempXValue = CoordinateAtPowerNMinus2.getXValue() + scaleVectorByPowerOf2(XVector, MaximumPowerOf2);
                TempYValue = CoordinateAtPowerNMinus2.getYValue() + scaleVectorByPowerOf2(YVector, MaximumPowerOf2);
                CoordinateAtPowerN = new Coordinate(TempXValue, TempYValue);

                FOfCoordinate1 = Function.evaluate(CoordinateAtPowerNMinus2);
                FOfCoordinate2 = Function.evaluate(CoordinateAtPowerNMinus1);
                FOfCoordinate3 = Function.evaluate(CoordinateAtPowerN);
                if ((FOfCoordinate3 > FOfCoordinate2) && (FOfCoordinate3 > FOfCoordinate1)) {
                    break;
                } else {
                    // Condition detects divergence
                    if (MaximumPowerOf2 > Math.pow(Math.ceil(Math.abs(getBounds())), 8.0)) {
                        divergenceDetected = true;
                        return;
                    }
                    MaximumPowerOf2 += 1;
                    OptimisedCoordinateList.add(CoordinateAtPowerN);
                }
            }
            // minimum is now bracketed by coordinate 1 and coordinate 3
            // begin 2D binary search
            double MidpointX = (CoordinateAtPowerNMinus2.getXValue() + CoordinateAtPowerN.getXValue()) / 2.0;
            double MidpointY = (CoordinateAtPowerNMinus2.getYValue() + CoordinateAtPowerN.getYValue()) / 2.0;
            Coordinate Midpoint = new Coordinate(MidpointX, MidpointY);
            Coordinate UpperBound = CoordinateAtPowerN;
            Coordinate LowerBound = CoordinateAtPowerNMinus2;
            double FOfUpperBound = Function.evaluate(UpperBound);
            double FOfLowerBound = Function.evaluate(LowerBound);
            OptimisedCoordinateList.add(Midpoint);
            while (true) {
                if (FOfLowerBound > FOfUpperBound) {
                    LowerBound = Midpoint;
                }
                if (FOfUpperBound > FOfLowerBound) {
                    UpperBound = Midpoint;
                }
                MidpointX = (UpperBound.getXValue() + LowerBound.getXValue()) / 2.0;
                MidpointY = (UpperBound.getYValue() + LowerBound.getYValue()) / 2.0;
                Coordinate NewMidpoint = new Coordinate(MidpointX, MidpointY);
                OptimisedCoordinateList.add(NewMidpoint);
                double FOfCurrentCoordinate = Function.evaluate(OptimisedCoordinateList.get(OptimisedCoordinateList.size() - 1));
                double FOfLastCoordinate = Function.evaluate(OptimisedCoordinateList.get(OptimisedCoordinateList.size() - 2));
                if (Math.abs(FOfCurrentCoordinate - FOfLastCoordinate) < Tolerance) {
                    FinalCoordinate = OptimisedCoordinateList.get(OptimisedCoordinateList.size() - 1);
                    break;
                }
            }
        }

        public Coordinate getStartPoint() {
            return StartPoint;
        }

        public double getXVector() {
            return XVector;
        }

        public double getYVector() {
            return YVector;
        }

        private double scaleVectorByPowerOf2(double vector, int power) {
            return vector * Math.pow(2.0, power);
        }

        // sets the vectors on the object
        public void setVector(Coordinate firstCoordinate, Coordinate secondCoordinate) {
            double DeltaY = secondCoordinate.getYValue() - firstCoordinate.getYValue();
            YVector = DeltaY;
            XVector = secondCoordinate.getXValue() - firstCoordinate.getXValue();

        }

        public int getOptimisationCounter() {
            return OptimisationCounter;
        }

    }
}