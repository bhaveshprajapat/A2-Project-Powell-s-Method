package main;

import java.io.Serializable;
import java.util.ArrayList;

/*
    Powell Method Thread Class that can be written to a binary file
 */
public class PowellMethod extends Thread implements Serializable {
    // Fields
    private static final long serialVersionUID = -4101739456805897681L;
    private double Tolerance;
    private double Bounds;
    private Coordinate StartPoint;
    private boolean FatalExceptionOccurred, Cancelled;
    private transient LinMin linMin;
    private ArrayList<Coordinate> UnitVectorSearchList = new ArrayList<>();
    private ArrayList<Coordinate> ConjugateDirectionSearchList = new ArrayList<>();
    private Coordinate FinalCoordinate;
    private transient PowellMethod.ExponentialSearch ExponentialSearch;
    private volatile boolean StopThreadFlag;
    // Class constructor
    public PowellMethod(double Tolerance, double Bounds, Coordinate StartPoint, SearchMethod SearchMethod) {
        this.Tolerance = Tolerance;
        this.Bounds = Bounds;
        this.StartPoint = StartPoint;
        // Initialise linMin with the correct search method
        switch (SearchMethod) {
            case BINARY_SEARCH:
                // Load a binary search object
                setLinMin(new BinarySearch());
                break;
            case GOLDEN_SECTION_SEARCH:
                // Load a golden section search object
                setLinMin(new GoldenSectionSearch());
                break;
        }
    }

    // Accessor method for Tolerance
    public double getTolerance() {
        return Tolerance;
    }

    // Accessor method for bounds
    public double getBounds() {
        return Bounds;
    }

    // gettter for startSearch point
    public Coordinate getStartPoint() {
        return StartPoint;
    }

    // startSearch method for powell method search that overrides startSearch in java.lang.Thread
    @Override
    public synchronized void start() {
        // Call to super method for threading
        super.start();
        // Declare and initialise variables where necessary
        int Iterations = 0;
        boolean OptimisingByX, InitialRun;
        InitialRun = true;
        OptimisingByX = false;
        int ListSize;
        // Add startSearch point to list
        getUnitVectorSearchList().add(getStartPoint());
        // Loop over linmin
        while (true) {
            if (StopThreadFlag) {
                Cancelled = true;
                return;
            }
            // Set up the linmin object with the values passed in to this class
            ListSize = getUnitVectorSearchList().size();
            if (InitialRun) {
                this.linMin.setStartPoint(getStartPoint());
            } else {
                this.linMin.setStartPoint(getUnitVectorSearchList().get(ListSize - 1));
            }
            this.linMin.setTolerance(getTolerance());
            this.linMin.setBounds(getBounds());
            this.linMin.setSearchDirection(OptimisingByX ? SearchDirection.VECTOR_I : SearchDirection.VECTOR_J);
            // Execute linmin
            try {
                this.linMin.startSearch();
            } catch (EvaluationException CaughtException) {
                // If anything adverse occurs, exit the thread
                setFatalExceptionOccured();
                return;
            }
            // Check for a stop, since linmin was a blocking action
            if (StopThreadFlag) {
                Cancelled = true;
                return;
            }
            // Adds linmin result to list
            getUnitVectorSearchList().add(this.linMin.getFinalCoordinate());
            ListSize = getUnitVectorSearchList().size();
            //reset last and current points
            Coordinate Last = getUnitVectorSearchList().get(ListSize - 2);
            Coordinate Current = this.linMin.getFinalCoordinate();
            // Calculates Tolerance in case we're already at minimum
            try {
                if ((Math.abs(Function.evaluate(Last) - Function.evaluate(Current))
                        < getTolerance()) && (Iterations > 3)) {
                    break;
                } else {
                    // Run another optimisation
                    OptimisingByX = !OptimisingByX;
                    InitialRun = false;
                    Iterations += 1;
                }
            } catch (EvaluationException CaughtException) {
                // Some fatal error occured
                setFatalExceptionOccured();
                return;
            }
            // Exit the method
            if (Iterations > 3) {
                break;
            }
        }
        if (StopThreadFlag) {
            Cancelled = true;
            return;
        }

        // initialise exponential search
        Coordinate NewStartPoint = getUnitVectorSearchList().get(getUnitVectorSearchList().size() - 1);
        this.ExponentialSearch = new ExponentialSearch(NewStartPoint, getTolerance());
        this.ExponentialSearch.setVector(getUnitVectorSearchList().get(getUnitVectorSearchList().size() - 3), NewStartPoint);
        MainSceneController.getLog().add("VECTOR" + this.ExponentialSearch.getXVector() + " " + this.ExponentialSearch.getYVector());
        if (this.ExponentialSearch.getStartPoint().equals(getStartPoint())) {
            setFinalCoordinate(getStartPoint());
            MainSceneController.getLog().add("No conjugate direction optimisation performed, already at minimum.");
            MainSceneController.getLog().add("The method may have been caught inside a local minimum, try higher bounds in this event.");
            return;
        }
        if ((this.ExponentialSearch.getXVector() == 0D) && (this.ExponentialSearch.getYVector() == 0D)) {
            setFinalCoordinate(getStartPoint());
            MainSceneController.getLog().add("No conjugate direction optimisation performed, already at minimum.");
            MainSceneController.getLog().add("The method may have been caught inside a local minimum, try higher bounds in this event.");
            return;
        }
        if (StopThreadFlag) {
            Cancelled = true;
            return;
        }
        try {
            this.ExponentialSearch.startSearch();
        } catch (EvaluationException CaughtException) {
            // catch any errors that occur during running
            setFatalExceptionOccured();
        }
        if (StopThreadFlag) {
            Cancelled = true;
            return;
        }
        setConjugateDirectionSearchList(this.ExponentialSearch.getOptimisedCoordinateList());
        setFinalCoordinate(this.ExponentialSearch.getFinalCoordinate());

    }

    public ArrayList<Coordinate> getUnitVectorSearchList() {
        return UnitVectorSearchList;
    }

    public void setUnitVectorSearchList(ArrayList<Coordinate> unitVectorSearchList) {
        UnitVectorSearchList = unitVectorSearchList;
    }

    // Accessor method for conjugate direction search list
    public ArrayList<Coordinate> getConjugateDirectionSearchList() {
        return ConjugateDirectionSearchList;
    }

    // Mutator method for conjugate direction search list
    public void setConjugateDirectionSearchList(ArrayList<Coordinate> conjugateDirectionSearchList) {
        ConjugateDirectionSearchList = conjugateDirectionSearchList;
    }

    // Mutator method for linmin
    public void setLinMin(LinMin linMin) {
        this.linMin = linMin;
    }

    // fatal exception boolean Mutator method method
    public void setFatalExceptionOccured() {
        setFatalExceptionOccurred();
    }

    // Accessor method for final coordinate
    public Coordinate getFinalCoordinate() {
        return FinalCoordinate;
    }

    // Mutator method for final coordinate
    public void setFinalCoordinate(Coordinate finalCoordinate) {
        this.FinalCoordinate = finalCoordinate;
    }

    // Accessor method for function
    public String getFunctionString() {
        return Function.getInfixExpression();
    }

    public boolean isFatalExceptionOccurred() {
        return FatalExceptionOccurred;
    }

    public void setFatalExceptionOccurred() {
        FatalExceptionOccurred = true;
    }

    public boolean isStopThreadFlag() {
        return StopThreadFlag;
    }

    public void setStopThreadFlag() {
        this.StopThreadFlag = true;
    }

    public void runTwoDimensionAdjustment() {
        ArrayList<Coordinate> NewUnitVectorList = new ArrayList<>();
        ArrayList<Coordinate> NewConjugateList = new ArrayList<>();
        try {
            for (Coordinate CoordinateStepper : getConjugateDirectionSearchList()) {
                Coordinate d = new Coordinate(CoordinateStepper.getXValue(), Function.evaluate(new Coordinate(CoordinateStepper.getXValue(), 0D)));
                NewUnitVectorList.add(d);
            }
            for (Coordinate CoordinateStepper : getUnitVectorSearchList()) {
                Coordinate e = new Coordinate(CoordinateStepper.getXValue(), Function.evaluate(new Coordinate(CoordinateStepper.getXValue(), 0D)));
                NewConjugateList.add(e);
            }
            Coordinate FinalCoordinate = getFinalCoordinate();
            setFinalCoordinate(new Coordinate(FinalCoordinate.getXValue(), Function.evaluate(new Coordinate(FinalCoordinate.getXValue(), 0D))));
            setConjugateDirectionSearchList(NewConjugateList);
            setUnitVectorSearchList(NewUnitVectorList);
        } catch (EvaluationException CaughtException) {
            MainSceneController.getLog().add(CaughtException.getMessage());


        }
    }

    public boolean getCancelled() {
        return Cancelled;
    }

    // Exponential Search Inner Class
    public class ExponentialSearch {

        private Coordinate StartPoint;
        private double XVector;
        private double YVector;
        private Coordinate FinalCoordinate;
        private ArrayList<Coordinate> OptimisedCoordinateList = new ArrayList<>();
        private double Tolerance;

        public ExponentialSearch(Coordinate StartPoint, double Tolerance) {
            this.StartPoint = StartPoint;
            this.Tolerance = Tolerance;
        }

        public Coordinate getFinalCoordinate() {
            return FinalCoordinate;
        }

        public void setFinalCoordinate(Coordinate FinalCoordinate) {
            this.FinalCoordinate = FinalCoordinate;
        }

        public ArrayList<Coordinate> getOptimisedCoordinateList() {
            return OptimisedCoordinateList;
        }

        public void startSearch() throws EvaluationException {
            OptimisedCoordinateList.add(StartPoint);
            // Declare local variables
            int MaximumPowerof2 = 2;  // Start at 2 so that 0,1 & 2 can be used initially
            Coordinate CoordinateAtPowerNMinus2 = StartPoint, CoordinateAtPowerNMinus1, CoordinateAtPowerN;
            double TempXValue, TempYValue;
            double FOfCoordinate1, FOfCoordinate2, FOfCoordinate3;
            // while loop to increase the power of 2
            while (true) {
                // generate three coordinates with three different powers
                TempXValue = CoordinateAtPowerNMinus2.getXValue() + scaleVectorByPowerOf2(XVector, MaximumPowerof2 - 2);
                TempYValue = CoordinateAtPowerNMinus2.getYValue() + scaleVectorByPowerOf2(YVector, MaximumPowerof2 - 2);
                CoordinateAtPowerNMinus2 = new Coordinate(TempXValue, TempYValue);
                TempXValue = CoordinateAtPowerNMinus2.getXValue() + scaleVectorByPowerOf2(XVector, MaximumPowerof2 - 1);
                TempYValue = CoordinateAtPowerNMinus2.getYValue() + scaleVectorByPowerOf2(YVector, MaximumPowerof2 - 1);
                CoordinateAtPowerNMinus1 = new Coordinate(TempXValue, TempYValue);
                TempXValue = CoordinateAtPowerNMinus2.getXValue() + scaleVectorByPowerOf2(XVector, MaximumPowerof2);
                TempYValue = CoordinateAtPowerNMinus2.getYValue() + scaleVectorByPowerOf2(YVector, MaximumPowerof2);
                CoordinateAtPowerN = new Coordinate(TempXValue, TempYValue);

                FOfCoordinate1 = Function.evaluate(CoordinateAtPowerNMinus2);
                FOfCoordinate2 = Function.evaluate(CoordinateAtPowerNMinus1);
                FOfCoordinate3 = Function.evaluate(CoordinateAtPowerN);
                if ((FOfCoordinate3 > FOfCoordinate2) && (FOfCoordinate3 > FOfCoordinate1)) {
                    break;
                } else {
                    MaximumPowerof2 += 1;
                    OptimisedCoordinateList.add(CoordinateAtPowerN);
                }
            }
            // minimum is now bracketed by coordinate 1 and coordinate 3
            // begin 2D binary search
            double MidpointX = (CoordinateAtPowerNMinus2.getXValue() + CoordinateAtPowerN.getXValue()) / 2;
            double MidpointY = (CoordinateAtPowerNMinus2.getYValue() + CoordinateAtPowerN.getYValue()) / 2;
            Coordinate Midpoint = new Coordinate(MidpointX, MidpointY);
            Coordinate UpperBound, LowerBound;
            UpperBound = CoordinateAtPowerN;
            LowerBound = CoordinateAtPowerNMinus2;
            double FOfUpperBound, FOfLowerBound;
            FOfLowerBound = Function.evaluate(LowerBound);
            FOfUpperBound = Function.evaluate(UpperBound);
            OptimisedCoordinateList.add(Midpoint);
            while (true) {
                if (FOfLowerBound > FOfUpperBound) {
                    LowerBound = Midpoint;
                }
                if (FOfUpperBound > FOfLowerBound) {
                    UpperBound = Midpoint;
                }
                MidpointX = (UpperBound.getXValue() + LowerBound.getXValue()) / 2;
                MidpointY = (UpperBound.getYValue() + LowerBound.getYValue()) / 2;
                Coordinate NewMidpoint = new Coordinate(MidpointX, MidpointY);
                OptimisedCoordinateList.add(NewMidpoint);
                double FOfCurrentCoordinate = Function.evaluate(OptimisedCoordinateList.get(OptimisedCoordinateList.size() - 1));
                double FOfLastCoordinate = Function.evaluate(OptimisedCoordinateList.get(OptimisedCoordinateList.size() - 2));
                if (Math.abs(FOfCurrentCoordinate - FOfLastCoordinate) < Tolerance) {
                    setFinalCoordinate(OptimisedCoordinateList.get(OptimisedCoordinateList.size() - 1));
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

        public void setXVector(double XVector) {
            this.XVector = XVector;
        }

        public double getYVector() {
            return YVector;
        }

        public void setYVector(double YVector) {
            this.YVector = YVector;
        }

        private double scaleVectorByPowerOf2(double vector, int power) {
            return vector * Math.pow(2, power);
        }

        // sets the vectors on the object
        public void setVector(Coordinate one, Coordinate two) {
            double DeltaY = two.getYValue() - one.getYValue();
            double DeltaX = two.getXValue() - one.getXValue();
            setXVector(DeltaX);
            setYVector(DeltaY);
        }
    }
}