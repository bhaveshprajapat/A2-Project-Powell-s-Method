package main;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;

public class MainSceneController {
    private static final ArrayList<String> log = new ArrayList<>();
    public ProgressIndicator ProgressIndicator;
    public ScatterChart<Number, Number> MainSceneGraph;
    public TextField FunctionTextField;
    public TextField StartPointXTextField;
    public TextField StartPointYTextField;
    public TextField BoundsTextField;
    public Label SearchAlgorithmIndicator;
    public CheckBox LayerDataCheckbox;
    public TextArea LogTextArea;
    public Slider ToleranceSlider;
    public CheckBox AUTO2DCheckBox;
    private SearchMethod AlgorithmToUse = SearchMethod.BINARY_SEARCH;
    private PowellMethod RunResult;
    private PowellMethod LoadedResult;
    private PowellMethod powellMethod;

    public static ArrayList<String> getLog() {
        return log;
    }

    // Converts an ArrayList to a Scatter Chart Series
    private static Series<Number, Number> createSeriesFromArrayList(List<Coordinate> ArrayListToConvert) {
        Series<Number, Number> objectToReturn = new Series<>();
        for (Coordinate loopCoordinate : ArrayListToConvert) {
            if (loopCoordinate != null) {
                Data<Number, Number> vectorLineSearchData = new Data<>();
                vectorLineSearchData.setXValue(loopCoordinate.getXValue());
                vectorLineSearchData.setYValue(loopCoordinate.getYValue());
                objectToReturn.getData().add(vectorLineSearchData);
            }
        }
        return objectToReturn;
    }

    // displays the parameters on the main window's graph
    private static void displayGraph(Coordinate finalCoordinate, String function, Series... ScatterChartSeries) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setForceZeroInRange(false);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setForceZeroInRange(false);
        ScatterChart<Number, Number> scatterChart = new ScatterChart<>(xAxis, yAxis);
        scatterChart.setTitle(function + " - Scatter Graph of path taken to minimum point: " + finalCoordinate);
        scatterChart.setLegendSide(Side.RIGHT);
        xAxis.setLabel("x");
        yAxis.setLabel("y");
        // Show graph here
        Stage stage = new Stage();
        Scene scene = new Scene(scatterChart, 700.0, 700.0);
        stage.setScene(scene);
        stage.setTitle("Graph View of Function: " + function);
        stage.show();
        stage.setResizable(true);
        scatterChart.setAnimated(true);
        if (ScatterChartSeries != null) {
            for (Series<Number, Number> Series : ScatterChartSeries) {
                if (Series != null) {
                    scatterChart.getData().add(Series);
                }
            }
        }
    }

    private static void createSeriesAndDrawGraph(List<Coordinate> ArrayListForSeries1,
                                                 Coordinate finalCoordinate,
                                                 List<Coordinate> ArrayListForSeries3, String function) {
        // Series 1: UnitVector Search
        Series<Number, Number> UnitVectorSearchChartSeries = new Series<>();
        UnitVectorSearchChartSeries.setName("Unit Vector Search Data");
        Data<Number, Number> scatterData;
        for (Coordinate loopCoordinate : ArrayListForSeries1) {
            if (loopCoordinate != null) {
                scatterData = new Data<>();
                scatterData.setXValue(loopCoordinate.getXValue());
                scatterData.setYValue(loopCoordinate.getYValue());
                UnitVectorSearchChartSeries.getData().add(scatterData);
            }
        }
        // Series 2 : final coordinate
        Series<Number, Number> finalCoordinateChartSeries = new Series<>();
        double finalX = finalCoordinate.getXValue();
        double finalY = finalCoordinate.getYValue();
        Data<Number, Number> finalData;
        finalData = new Data<>();
        finalData.setXValue(finalX);
        finalData.setYValue(finalY);
        finalCoordinateChartSeries.setName("Final Coordinate");
        if (finalCoordinateChartSeries.getData() != null) {
            finalCoordinateChartSeries.getData().add(finalData);
        }
        // Series 3: Conjugate Direction search data
        Series<Number, Number> conjugateDirecrtionSearchChartSeries = new Series<>();
        for (Coordinate loopCoordinate : ArrayListForSeries3) {
            if (loopCoordinate != null) {
                Data<Number, Number> vectorLineSearchData = new Data<>();
                vectorLineSearchData.setXValue(loopCoordinate.getXValue());
                vectorLineSearchData.setYValue(loopCoordinate.getYValue());
                conjugateDirecrtionSearchChartSeries.getData().add(vectorLineSearchData);
            }
        }
        conjugateDirecrtionSearchChartSeries.setName("Conjugate Direction Search Data");
        displayGraph(finalCoordinate, function, UnitVectorSearchChartSeries, finalCoordinateChartSeries, conjugateDirecrtionSearchChartSeries);
    }

    public void setBinarySearchMode(ActionEvent actionEvent) {
        /*
            Switches the search mode to Binary
            and changes the text box to reflect this.
         */
        AlgorithmToUse = SearchMethod.BINARY_SEARCH;
        SearchAlgorithmIndicator.setText("Binary Search");
    }

    public void setInverseParabolicInterpolationSearchMode(ActionEvent actionEvent) {
        /*
            Switches the search mode to Parabolic Interpolation
            and changes the text box to reflect this.
         */
        AlgorithmToUse = SearchMethod.INVERSE_PARABOLIC_INTERPOLATION;
        SearchAlgorithmIndicator.setText("Inverse Parabolic Interpolation");
    }


    public void setGoldenSectionSearchMode(ActionEvent actionEvent) {
        /*
            Switches the search mode to Golden Section
            and changes the text box to reflect this
         */
        AlgorithmToUse = SearchMethod.GOLDEN_SECTION_SEARCH;
        SearchAlgorithmIndicator.setText("Golden Section Search");
    }

    public void saveThisSetOfResults(ActionEvent actionEvent) {
        /*
            Saves the set of results from the last run made in this session
         */
        if (RunResult != null) { // Check to ensure that a run result exists
            /*
                Creates a file save dialog
            */
            FileChooser FileChooser = new FileChooser();
            FileChooser.setTitle("Save results to a PMR File");
            // Set the extension filter to .pmr
            ExtensionFilter extensionFilter =
                    new ExtensionFilter("Powell's Method Result Files (*.pmr)", "*.pmr");
            FileChooser.getExtensionFilters().add(extensionFilter);
            /*
                Opens a file save dialog and appends the extension if it was deleted
            */
            File file = FileChooser.showSaveDialog(null);
            if (!file.getPath().toLowerCase().endsWith(".pmr")) {
                file = new File(file.getPath() + ".pmr");
            }
            /*
                Writes out the RunResult object to the file specified.
            */
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                ObjectOutputStream out = new ObjectOutputStream(fileOutputStream);
                out.writeObject(RunResult);
                // Flush and close file and file streams
                out.flush();
                out.close();

                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e) {
                Alert IOAlert = new Alert(AlertType.ERROR);
                IOAlert.setTitle("An error occured during the file write!");
                IOAlert.setHeaderText("For more information see below...");
                IOAlert.setContentText(e.getLocalizedMessage());
                IOAlert.show();
            }
        }
    }

    public void loadResultsFromFile(ActionEvent actionEvent) {
        // Un-set the transparency of the graph
        MainSceneGraph.setOpacity(1.0);
        // Open a file chooser to select a file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Results to a PMR File");
        // Set extension filter
        ExtensionFilter extFilter =
                new ExtensionFilter("Powell's Method Result Files (*.pmr)", "*.pmr");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(null);
        FileInputStream fileInputStream;
        // Test if file exists
        if (file == null) {
            return;
        }
        // Attempt to open the file
        try {
            fileInputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            // Deserialise the object and close streams
            LoadedResult = (PowellMethod) objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
            // Loads the data to the graph
            updateGraphInMainWindow(LoadedResult);
        } catch (IOException | ClassNotFoundException CaughtException) {
            Alert IOAlert = new Alert(AlertType.ERROR);
            IOAlert.setTitle("An error occured while opening the file!");
            IOAlert.setHeaderText("For more information see below...");
            IOAlert.setContentText(CaughtException.getLocalizedMessage());
            IOAlert.show();
        }
    }

    public void loadCurrentResultsInNewWindow(ActionEvent actionEvent) {
        // Un-grey out the graph
        MainSceneGraph.setOpacity(1.0);
        // Loads the coordinate list from the loaded result
        List<Coordinate> UnitVectorSearchList = LoadedResult.getUnitVectorSearchList();
        Coordinate FinalCoordinate = LoadedResult.getFinalCoordinate();
        List<Coordinate> ConjugateDirectionSearchList = LoadedResult.getExponentialSearchList();
        // Calls method to open a new graph in a new window with this data
        createSeriesAndDrawGraph(UnitVectorSearchList, FinalCoordinate, ConjugateDirectionSearchList, "Insert func");
    }

    public void loadResultsFromFileInNewWindow(ActionEvent actionEvent) {
        PowellMethod LoadedResults;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Results to PMR File");
        // Set extension filter
        ExtensionFilter extFilter =
                new ExtensionFilter("Powell's Method Result Files (*.pmr)", "*.pmr");
        fileChooser.getExtensionFilters().add(extFilter);
        List<File> files = fileChooser.showOpenMultipleDialog(null);
        try {
            for (File FileStepper : files) {
                if (FileStepper != null) {

                        FileInputStream fileInputStream = new FileInputStream(FileStepper);
                        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                        LoadedResults = (PowellMethod) objectInputStream.readObject();
                        objectInputStream.close();
                        fileInputStream.close();

                    if (LoadedResults != null) {
                        ProgressIndicator.setProgress(1.0);
                        // Series 1: UnitVectorSearchArrayList
                        List<Coordinate> UnitVectorSearchList = LoadedResults.getUnitVectorSearchList();
                        // Series 2 : final coordinate
                        Coordinate FinalCoordinate = LoadedResults.getFinalCoordinate();
                        // Series 3: conjugate direction search data
                        List<Coordinate> ConjugateDirectionSearchCoordinateList = LoadedResults.getUnitVectorSearchList();
                        createSeriesAndDrawGraph(UnitVectorSearchList, FinalCoordinate, ConjugateDirectionSearchCoordinateList, LoadedResults.getFunctionString());
                    }
                }
            }
        } catch (NullPointerException CaughtException) {
            log.add(CaughtException.getLocalizedMessage());
            updateLog();
        } catch (ClassNotFoundException | IOException exception) {
            log.add(exception.getLocalizedMessage());
        }
    }

    public void onRunButtonClicked(ActionEvent actionEvent) {
        LinMin.setCounter(0);
        if (powellMethod != null) {
            if (powellMethod.isThreadStopped()) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Cancelled.");
                alert.showAndWait();
            }
            if (powellMethod.isAlive()) {
                powellMethod.setThreadStopped();
                try {
                    powellMethod.join();
                } catch (InterruptedException e) {
                    log.add(e.getLocalizedMessage());
                }
                powellMethod = null;
                return;
            }
        }
        MainSceneGraph.opacityProperty().setValue(100);
        if (ProgressIndicator.disabledProperty().getValue()) {
            ProgressIndicator.setDisable(false);
            ProgressIndicator.setVisible(true);
        }
        ProgressIndicator.setDisable(false);
        ProgressIndicator.setProgress(javafx.scene.control.ProgressIndicator.INDETERMINATE_PROGRESS);
        // Initialise variables
        if ((FunctionTextField.getText() != null) && FunctionTextField.getText().isEmpty()) {
            Alert BadInputAlert = new Alert(AlertType.ERROR);
            BadInputAlert.setTitle("Invalid input in function text field");
            BadInputAlert.setHeaderText("No entry in the function text field");
            BadInputAlert.setContentText("The function text field must not be left blank, please revise.");
            BadInputAlert.show();
            ProgressIndicator.setProgress(0);
            return;
        }
        String ParseSine = FunctionTextField.getText().replaceAll("sin", "s");
        String ParseCos = ParseSine.replaceAll("cos", "c");
        String ParsePi = ParseCos.replaceAll("π", "p");
        String ParseTan = ParsePi.replaceAll("tan", "t");
        Function.setInfixExpression(ParseTan);
        double StartPointX;
        double StartPointY;
        double Tolerance = Math.pow(0.1, ToleranceSlider.getValue());
        double Bounds;

        StartPointX = Double.parseDouble(StartPointXTextField.getText());
        StartPointY = Double.parseDouble(StartPointYTextField.getText());
        Bounds = Double.parseDouble(BoundsTextField.getText());
        if ((Bounds <= 0.0D) || (Bounds == 1)) {
            Alert BadInputAlert = new Alert(AlertType.ERROR);
            BadInputAlert.setTitle("Invalid input in one or more boxes");
            BadInputAlert.setHeaderText("The following input was not recognised:");
            BadInputAlert.setContentText("The bounds ");
            BadInputAlert.show();
            ProgressIndicator.setProgress(0);
            return;
        }

        /*
        This try block shows errors if the function entered is badly formatted,
        or even if the function is blank. It then shows a message explaining the
        error in detail.
         */
        try {
            Function.evaluate(new Coordinate(StartPointX, StartPointY));
        } catch (RuntimeException e) {
            ProgressIndicator.setProgress(0);
            Alert badFunction = new Alert(AlertType.ERROR);
            badFunction.setAlertType(AlertType.ERROR);
            badFunction.setTitle("Badly formatted function");
            badFunction.setHeaderText("The function entered could not be interpreted properly");
            badFunction.setContentText("Please enter a correctly formatted function. See details:"
                    + System.getProperty("line.separator")
                    + e.getLocalizedMessage());
            badFunction.show();
            return;
        } catch (EvaluationException e) {
            FutureTask<Void> errorDialog = new FutureTask<>(() -> {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Evaluation Exception");
                alert.setHeaderText("Something went wrong during an evaluation");
                alert.setContentText("It's possible that your expression has an error in it" + e.getMessage());
                alert.showAndWait();
                ProgressIndicator.setDisable(true);
            }, null);
            Platform.runLater(errorDialog);
        }
        powellMethod = new PowellMethod(Tolerance, Bounds, new Coordinate(StartPointX, StartPointY), AlgorithmToUse);
        powellMethod.start();
        FutureTask<Void> UIUpdate = new FutureTask<>(() -> {
            if (!powellMethod.isEvaluationExceptionOccurred() && !powellMethod.isThreadStopped()) {
                log.add("Optimisation complete at :" + LocalDateTime.now());
                log.add("Number of distinct optimisations required: " + powellMethod.getOptimisationCounter());
                updateLog();
                if (!LayerDataCheckbox.isSelected()) {
                    MainSceneGraph.getData().clear();
                }
                RunResult = powellMethod;
                LoadedResult = powellMethod;
                ProgressIndicator.setProgress(1.0);
                if (powellMethod.getExponentialSearch().isDivergenceDetected()) {
                    Alert DivergenceAlert = new Alert(AlertType.INFORMATION);
                    DivergenceAlert.setTitle("Divergence Detected");
                    DivergenceAlert.setHeaderText("Divergence Detected");
                    DivergenceAlert.setContentText("See the log for more information");
                    DivergenceAlert.show();
                    log.add("While performing the exponential vector search, the algorithm diverged far from the expected search area. The graph should illustrate this. Increment the bounds to increase the search area if necessary.");
                    updateLog();
                }
                if (powellMethod.isThreadStopped()) {
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Cancelled.");
                    alert.show();
                }
                if (AUTO2DCheckBox.isSelected()) {
                    String functionString = Function.getInfixExpression();
                    boolean oneDimensionOnly = (functionString.contains("x") && !functionString.contains("y")) || (!functionString.contains("x") && functionString.contains("y"));
                    if (oneDimensionOnly) {
                        powellMethod.runTwoDimensionAdjustment();
                    }
                }
                updateGraphInMainWindow(powellMethod);
                powellMethod = null;
            } else {
                Alert CancellationDialog = new Alert(AlertType.INFORMATION);
                CancellationDialog.setTitle("Something went wrong...");
                CancellationDialog.setContentText("Please see the log for more information.");
                String errorString;
                if (powellMethod.isThreadStopped()) {
                    errorString = "The current optimisation was stopped";
                    log.add(errorString);
                    updateLog();
                }
                if (powellMethod.isEvaluationExceptionOccurred()) {
                    errorString = "Function could not be evaluated - please check for errors in the function";
                    log.add(errorString);
                    updateLog();
                }
                CancellationDialog.showAndWait();
                RunResult = null;
                LoadedResult = null;
            }

        }, null);
        Platform.runLater(UIUpdate);

    }

    // Updates the graph in the main window with the result
    private void updateGraphInMainWindow(PowellMethod result) {
        Coordinate finalCoordinate = result.getFinalCoordinate();
        Series UnitVectorScatterChartSeries = createSeriesFromArrayList(result.getUnitVectorSearchList());
        Series ConjugateDirectionScatterChartSeries = createSeriesFromArrayList(result.getExponentialSearchList());
        // Removes existing data from the graph if necessary
        if (!LayerDataCheckbox.isSelected()) {
            MainSceneGraph.getData().clear();
        }
        UnitVectorScatterChartSeries.setName("Unit Vector Search Data");
        ConjugateDirectionScatterChartSeries.setName("Vector Search Data");
        MainSceneGraph.getData().addAll(UnitVectorScatterChartSeries, ConjugateDirectionScatterChartSeries);
        MainSceneGraph.setTitle("Graph showing path taken to " + finalCoordinate);
        Data<Number, Number> finalCoordinateData = new Data<>();
        finalCoordinateData.setXValue(finalCoordinate.getXValue());
        finalCoordinateData.setYValue(finalCoordinate.getYValue());
        Series<Number, Number> finalCoordinateSeries = new Series<>();
        finalCoordinateSeries.getData().add(finalCoordinateData);
        finalCoordinateSeries.setName("Final Coordinate");
        MainSceneGraph.getData().add(finalCoordinateSeries);
    }

    //Changes the function text field to the Booth function
    public void setToBoothFunction(ActionEvent actionEvent) {
        FunctionTextField.setText("(x+2*y-7)^2 + (2*x+y-5)^2");
    }

    //Changes the function text field to a 3D Paraboloid
    public void setToParaboloid(ActionEvent actionEvent) {
        FunctionTextField.setText("(x+2)^2 + (y-2)^2");
    }

    //Changes the function text field to the Matyas function
    public void setToMatyasFunction(ActionEvent actionEvent) {
        FunctionTextField.setText("0.26*(x^2 + y^2) - 0.48*x*y");
    }

    //Changes the function text field to the McCormick function
    public void setToMcCormickFunction() {
        FunctionTextField.setText("(x+y)sin1 + (x-y)^2 -1.5*x+2.5*y+1");
    }

    //Changes the function text field to the Levi function
    public void setToLeviFunctionN13() {
        FunctionTextField.setText("(3*p*x)sin2+((x-1)^2)*(1+(3*p*y)sin2)+((y-1)^2)*(1+(2*p*y)sin2)");
    }

    //adds sin to the text field
    public void addSin(ActionEvent actionEvent) {
        FunctionTextField.setText(FunctionTextField.getText() + "(OPERAND)sin1");
    }

    //adds cos to the text field
    public void AddCos(ActionEvent actionEvent) {
        FunctionTextField.setText(FunctionTextField.getText() + "(OPERAND)cos1");
    }

    // adds tan to the text field
    public void AddTan(ActionEvent actionEvent) {
        FunctionTextField.setText(FunctionTextField.getText() + "(OPERAND)tan1");
    }

    // adds sin2 to the text field
    public void addSinSquared(ActionEvent actionEvent) {
        FunctionTextField.setText(FunctionTextField.getText() + "(OPERAND)sin2");
    }

    // adds cos2 to the function text field
    public void addCosSquared(ActionEvent actionEvent) {
        FunctionTextField.setText(FunctionTextField.getText() + "(OPERAND)cos2");
    }

    // adds tan2 to the function text field
    public void addTanSquared(ActionEvent actionEvent) {
        FunctionTextField.setText(FunctionTextField.getText() + "(OPERAND)tan2");
    }

    // adds e to the function text field
    public void addE(ActionEvent actionEvent) {
        FunctionTextField.setText(FunctionTextField.getText() + "e^(OPERAND)");
    }

    // adds pi to the function text field
    public void addPi(ActionEvent actionEvent) {
        FunctionTextField.setText(FunctionTextField.getText() + 'π');
    }

    public void closeButtonClicked(ActionEvent actionEvent) {
        System.exit(0);
    }

    public void helpButtonClicked(ActionEvent actionEvent) {
        try {
            final Class<? extends MainSceneController> aClass = getClass();
            Stage stage = new Stage();
            Parent root = FXMLLoader.load(aClass.getResource("" + "helpScene.fxml"));
            stage.setTitle("Powell's Method Help");
            stage.setScene(new Scene(root, 700.0, 300.0));
            stage.show();
        } catch (IOException ignored) {
            log.add("Failed to load help.");
            updateLog();
        }
    }

    public void onGraphClicked(MouseEvent mouseEvent) {
        if (LoadedResult != null) {
            String LineBreak = System.getProperty("line.separator");
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Information about this loaded result");
            alert.setContentText(
                    "The function used in this optimisation was:"
                            + LineBreak + LoadedResult.getFunctionString()
            );
            alert.show();
        } else {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setContentText(
                    "Click on the graph after running/loading an optimisation to view the function used."
            );
            alert.show();
        }
    }

    public void updateLog() {
        String lineSeparator = System.getProperty("line.separator");
        LogTextArea.clear();
        LogTextArea.setText("");
        for (String stepper : log) {
            LogTextArea.setText(LogTextArea.getText() + lineSeparator + stepper);
        }
    }

    public void clearResults(ActionEvent actionEvent) {
        MainSceneGraph.getData().clear();
        MainSceneGraph.setTitle("No results to show");
        MainSceneGraph.setOpacity(0.25);
    }

}