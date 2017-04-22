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
                setEvaluationExceptionOccurred();
                MainSceneController.getLog().add("Evaluation error during unit vector search! Please check your function is correctly formatted.");
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
                // Catches evaluation errors that might occur
                setEvaluationExceptionOccurred();
                MainSceneController.getLog().add("Evaluation error during stop criteria testing! Please check the function.");
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
            // Catches evaluation errors that might occur
            setEvaluationExceptionOccurred();
            MainSceneController.getLog().add("Evaluation error during exponential search! Please check your function is correctly formatted.");
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

    // Accessor method for exponential search list
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
    public void setEvaluationExceptionOccurred() {
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
        ArrayList<Coordinate> NewExponentialList = new ArrayList<>();
        try {
            for (Coordinate CoordinateStepper : exponentialSearchList) {
                Coordinate newCoordinate = new Coordinate(CoordinateStepper.getXValue(), Function.evaluate(new Coordinate(CoordinateStepper.getXValue(), 0.0D)));
                NewExponentialList.add(newCoordinate);
            }
            for (Coordinate CoordinateStepper : unitVectorSearchList) {
                Coordinate newCoordinate = new Coordinate(CoordinateStepper.getXValue(), Function.evaluate(new Coordinate(CoordinateStepper.getXValue(), 0.0D)));
                NewUnitVectorList.add(newCoordinate);
            }
            finalCoordinate = new Coordinate(finalCoordinate.getXValue(), Function.evaluate(new Coordinate(finalCoordinate.getXValue(), 0.0D)));
            exponentialSearchList = NewExponentialList;
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

        // Class constructor
        public ExponentialSearch(Coordinate StartPoint, double Tolerance) {
            this.StartPoint = StartPoint;
            this.Tolerance = Tolerance;
        }

        // Accessor method for divergence detector
        public boolean isDivergenceDetected() {
            return divergenceDetected;
        }

        // Accessor method for final coordinate
        public Coordinate getFinalCoordinate() {
            return FinalCoordinate;
        }

        // Accessor method for optimisied coordiante list (immutable)
        public List<Coordinate> getOptimisedCoordinateList() {
            return Collections.unmodifiableList(OptimisedCoordinateList);
        }

        // Subroutine to start search
        public void startSearch() throws EvaluationException {
            // Declare local variables, and initialise counter
            OptimisationCounter = 0;
            OptimisedCoordinateList.add(StartPoint);
            int MaximumPowerOf2 = 2;  // Start at 2 so that 0,1 & 2 can be used initially
            Coordinate CoordinateAtPowerNMinus2 = StartPoint;
            Coordinate CoordinateAtPowerNMinus1;
            Coordinate CoordinateAtPowerN;
            double TempXValue;
            double TempYValue;
            double FOfCoordinate1;
            double FOfCoordinate2;
            double FOfCoordinate3;
            /*
                Exponential Search Stage
             */
            // while loop to increase the power of 2
            while (true) {
                OptimisationCounter = OptimisationCounter + 1;
                // Generate three coordinates with the vector applied by three different powers of 2
                TempXValue = CoordinateAtPowerNMinus2.getXValue() + scaleVectorByPowerOf2(XVector, MaximumPowerOf2 - 2);
                TempYValue = CoordinateAtPowerNMinus2.getYValue() + scaleVectorByPowerOf2(YVector, MaximumPowerOf2 - 2);
                CoordinateAtPowerNMinus2 = new Coordinate(TempXValue, TempYValue);
                TempXValue = CoordinateAtPowerNMinus2.getXValue() + scaleVectorByPowerOf2(XVector, MaximumPowerOf2 - 1);
                TempYValue = CoordinateAtPowerNMinus2.getYValue() + scaleVectorByPowerOf2(YVector, MaximumPowerOf2 - 1);
                CoordinateAtPowerNMinus1 = new Coordinate(TempXValue, TempYValue);
                TempXValue = CoordinateAtPowerNMinus2.getXValue() + scaleVectorByPowerOf2(XVector, MaximumPowerOf2);
                TempYValue = CoordinateAtPowerNMinus2.getYValue() + scaleVectorByPowerOf2(YVector, MaximumPowerOf2);
                CoordinateAtPowerN = new Coordinate(TempXValue, TempYValue);
                // Evaluate all three coordinates
                FOfCoordinate1 = Function.evaluate(CoordinateAtPowerNMinus2);
                FOfCoordinate2 = Function.evaluate(CoordinateAtPowerNMinus1);
                FOfCoordinate3 = Function.evaluate(CoordinateAtPowerN);
                if ((FOfCoordinate3 > FOfCoordinate2) && (FOfCoordinate3 > FOfCoordinate1) || (FOfCoordinate1 > FOfCoordinate2) && (FOfCoordinate1 > FOfCoordinate3)) {
                    OptimisedCoordinateList.add(CoordinateAtPowerNMinus1);
                    OptimisedCoordinateList.add(CoordinateAtPowerN);
                    break;
                } else {
                    // Condition detects divergence where the exponential search searches too far away from original search area.
                    if (MaximumPowerOf2 > Math.pow(Math.ceil(Math.abs(getBounds() + 1)), 4.0)) {
                        divergenceDetected = true;
                        return;
                    }
                    MaximumPowerOf2 += 1;
                    OptimisedCoordinateList.add(CoordinateAtPowerNMinus2);
                }
            }
            // The minimum is now bracketed by coordinates 1 & 3

            /*
                2D Binary Search
             */
            // Calculate midpoint
            double MidpointX = (CoordinateAtPowerNMinus2.getXValue() + CoordinateAtPowerN.getXValue()) / 2.0;
            double MidpointY = (CoordinateAtPowerNMinus2.getYValue() + CoordinateAtPowerN.getYValue()) / 2.0;
            Coordinate LoopMidpoint = new Coordinate(MidpointX, MidpointY);
            // Calculate bounds
            Coordinate UpperBound = new Coordinate(CoordinateAtPowerN.getXValue(), CoordinateAtPowerN.getYValue());
            Coordinate LowerBound = new Coordinate(CoordinateAtPowerNMinus2.getXValue(), CoordinateAtPowerNMinus2.getYValue());

            while (true) {
                // Evaluate bounds, restrict bounds based on the highest
                double FOfUpperBound = Function.evaluate(UpperBound);
                double FOfLowerBound = Function.evaluate(LowerBound);
                if (FOfLowerBound > FOfUpperBound) {
                    LowerBound = new Coordinate(LoopMidpoint.getXValue(), LoopMidpoint.getYValue());
                }
                if (FOfUpperBound > FOfLowerBound) {
                    UpperBound = new Coordinate(LoopMidpoint.getXValue(), LoopMidpoint.getYValue());
                }
                // Calculate a new midpoint
                MidpointX = (UpperBound.getXValue() + LowerBound.getXValue()) / 2.0;
                MidpointY = (UpperBound.getYValue() + LowerBound.getYValue()) / 2.0;
                Coordinate NewMidpoint = new Coordinate(MidpointX, MidpointY);
                // Compare it with the stop condition
                double FOfCurrentCoordinate = Function.evaluate(NewMidpoint);
                double FOfLastCoordinate = Function.evaluate(OptimisedCoordinateList.get(OptimisedCoordinateList.size() - 1));
                OptimisedCoordinateList.add(NewMidpoint);
                if (Math.abs(FOfCurrentCoordinate - FOfLastCoordinate) < Tolerance) {
                    FinalCoordinate = NewMidpoint;
                    break;
                } else {
                    // Re-iterate
                    LoopMidpoint = new Coordinate(NewMidpoint.getXValue(), NewMidpoint.getYValue());
                }
            }
        }

        // Accessor method for start point
        public Coordinate getStartPoint() {
            return StartPoint;
        }

        // Accessor method for X Vector
        public double getXVector() {
            return XVector;
        }

        // Accessor method for Y Vector
        public double getYVector() {
            return YVector;
        }

        /*
            Returns 2^Power * Vector (used during exponential search)
         */
        private double scaleVectorByPowerOf2(double vector, int power) {
            return vector * Math.pow(2.0, power);
        }

        // Sets the vectors to search over
        public void setVector(Coordinate firstCoordinate, Coordinate secondCoordinate) {
            YVector = secondCoordinate.getYValue() - firstCoordinate.getYValue();
            XVector = secondCoordinate.getXValue() - firstCoordinate.getXValue();

        }

        /*
            Gets optimisation counter value
         */
        public int getOptimisationCounter() {
            return OptimisationCounter;
        }

    }
}