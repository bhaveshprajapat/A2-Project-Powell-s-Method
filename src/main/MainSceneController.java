package main;

import com.sun.istack.internal.Nullable;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;

@SuppressWarnings("unchecked")
public class MainSceneController {
    @FXML
    public ProgressIndicator progressIndicator;
    @FXML
    public ScatterChart mainSceneGraph;
    @FXML
    public SplitPane splitPane;
    @FXML
    public TextField functionTextField;
    @FXML
    public TextField startPointXTextField;
    @FXML
    public TextField startPointYTextField;
    @FXML
    public TextField toleranceTextField;
    @FXML
    public TextField boundsTextField;
    @FXML
    public Label searchAlgorithmIndicator;
    @FXML
    public CheckBox clearExistingDataCheckbox;
    @FXML
    private SearchMethod AlgorithmToUse = SearchMethod.binarySearch;
    private PowellMethod runResult;
    private PowellMethod loadedResult;
    private PowellMethod powellMethod;

    public void setBinarySearchMode(ActionEvent actionEvent) {
        /*
            Switches the search mode to Binary
            and changes the text box to reflect this.
         */
        AlgorithmToUse = SearchMethod.binarySearch;
        searchAlgorithmIndicator.setText("Binary Search");
    }

    public void setGoldenSectionSearchMode(ActionEvent actionEvent) {
        /*
            Switches the search mode to Golden Section
            and changes the text box to reflect this
         */
        AlgorithmToUse = SearchMethod.goldenSectionSearch;
        searchAlgorithmIndicator.setText("Golden Section Search");
    }

    public void saveThisSetOfResults(ActionEvent actionEvent) {
        /*
            Saves the set of results from the last run made in this session
         */
        if (getRunResult() != null) { // Check to ensure that a run result exists
            /*
                Creates a file save dialog
            */
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save results to a PMR File");
            // Set the extension filter to .pmr
            FileChooser.ExtensionFilter extensionFilter =
                    new FileChooser.ExtensionFilter("Powell's Method Result Files (*.pmr)", "*.pmr");
            fileChooser.getExtensionFilters().add(extensionFilter);
            /*
                Opens a file save dialog and appends the extension if it was deleted
            */
            File file = fileChooser.showSaveDialog(null);
            if (!file.getPath().toLowerCase().endsWith(".pmr")) {
                file = new File(file.getPath() + ".pmr");
            }
            /*
                Writes out the runResult object to the file specified.
            */
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                ObjectOutputStream out = new ObjectOutputStream(fileOutputStream);
                out.writeObject(getRunResult());
                // Flush and close file and file streams
                out.flush();
                out.close();
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e) {
                Alert IOAlert = new Alert(Alert.AlertType.ERROR);
                IOAlert.setTitle("Error during file write!");
                IOAlert.setHeaderText("Some error occurred during the file write process. For more information see below...");
                IOAlert.setContentText(e.getLocalizedMessage());
                IOAlert.show();
            }
        }
    }

    public void loadResultsFromFile(ActionEvent actionEvent) {
        // Un-grey out the graph
        mainSceneGraph.setOpacity(1);
        /*
            Open a file chooser to select a file
         */
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Results to PMR File");
        // Set extension filter
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("Powell's Method Result Files (*.pmr)", "*.pmr");
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
            setLoadedResult((PowellMethod) objectInputStream.readObject());
            objectInputStream.close();
            fileInputStream.close();
            // Loads the data to the graph
            updateGraphSameWindow(getLoadedResult().getFinalCoordinate(), "insert func", createSeriesFromArrayList(getLoadedResult().getUnitVectorSearchList()), createSeriesFromArrayList(getLoadedResult().getConjugateDirectionSearchList()), clearExistingDataCheckbox.isSelected());
        } catch (IOException | ClassNotFoundException e) {
            Alert IOAlert = new Alert(Alert.AlertType.ERROR);
            IOAlert.setTitle("Error during file opening!");
            IOAlert.setHeaderText("Some error occurred during the file opening process. For more information see below...");
            IOAlert.setContentText(e.getLocalizedMessage());
            IOAlert.show();
        }
    }

    public void loadTheseResultsInNewWindow(ActionEvent actionEvent) {
        // Un-grey out the graph
        mainSceneGraph.setOpacity(1);
        /*
            Loads the coordinate list from the loaded result
         */
        ArrayList<Coordinate> regularSearchList = getLoadedResult().getUnitVectorSearchList();
        Coordinate finalCoordinate = getLoadedResult().getFinalCoordinate();
        ArrayList<Coordinate> vectorSearchList = getLoadedResult().getConjugateDirectionSearchList();
        // Calls method to open a new graph in a new window with this data
        createSeriesAndDrawGraph(regularSearchList, finalCoordinate, vectorSearchList, "Insert func");
    }

    public void loadResultsFromFileInNewWindow(ActionEvent actionEvent) {
        PowellMethod results = null;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Results to PMR File");
        // Set extension filter
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("Powell's Method Result Files (*.pmr)", "*.pmr");
        fileChooser.getExtensionFilters().add(extFilter);
        List<File> files = fileChooser.showOpenMultipleDialog(null);

        try {
            for (File file : files) {
                if (file != null) {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(file);
                        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                        results = (PowellMethod) objectInputStream.readObject();
                        objectInputStream.close();
                        fileInputStream.close();
                    } catch (ClassNotFoundException | IOException fNFE) {
                        // TODO write handling code
                    }


                    if (results != null) {
                        progressIndicator.setProgress(1);
                        // Series 1: RegularSearchArrayList
                        ArrayList<Coordinate> regArrayList = results.getUnitVectorSearchList();
                        // Series 2 : final coordinate
                        Coordinate finalCoordinate = results.getFinalCoordinate();
                        // Series 3: VLS search data
                        ArrayList<Coordinate> vlsCoordinateList = results.getUnitVectorSearchList();
                        createSeriesAndDrawGraph(regArrayList, finalCoordinate, vlsCoordinateList, "insert func");
                    }
                }
            }
        } catch (NullPointerException e) {
            //TODO write handling code
        }
    }

    public void onRunButtonClicked(ActionEvent actionEvent) {
        if (powellMethod != null) {
            if (powellMethod.isAlive()) {
                powellMethod.interrupt();
            }
        }
        mainSceneGraph.opacityProperty().setValue(100);
        if (progressIndicator.disabledProperty().getValue()) {
            progressIndicator.setDisable(false);
            progressIndicator.setVisible(true);
        }

        progressIndicator.setDisable(false);
        progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

        // Initialise variables
        Function.setInfixExpression(functionTextField.getText());
        Function.convertInfixToPostfix();
        double startPointX;
        double startPointY;
        double tolerance;
        double bounds;
        try {
            startPointX = Double.parseDouble(startPointXTextField.getText());
            startPointY = Double.parseDouble(startPointYTextField.getText());
            tolerance = Double.parseDouble(toleranceTextField.getText());
            bounds = Double.parseDouble(boundsTextField.getText());
        } catch (NumberFormatException e) {
            // Show a message with the bad input
            Alert BadInputAlert = new Alert(Alert.AlertType.ERROR);
            BadInputAlert.setTitle("Invalid input in one or more boxes");
            BadInputAlert.setHeaderText("The following input was not recognised:");
            BadInputAlert.setContentText(e.getLocalizedMessage());
            BadInputAlert.show();
            progressIndicator.setProgress(0);
            return;
        }
        /*
        This try block shows errors if the function entered is badly formatted,
        or even if the function is blank. It then shows a message explaining the
        error in detail.
         */

        try {
            Function.outputFOfXY(new Coordinate(startPointX, startPointY));
        } catch (RuntimeException e) {
            progressIndicator.setProgress(0);
            Alert badFunction = new Alert(Alert.AlertType.ERROR);
            badFunction.setAlertType(Alert.AlertType.ERROR);
            badFunction.setTitle("Badly formatted function");
            badFunction.setHeaderText("The function entered could not be interpreted properly");
            badFunction.setContentText("Please enter a correctly formatted function. See details:"
                    + System.getProperty("line.separator")
                    + e.getLocalizedMessage());
            badFunction.show();
            return;
        } catch (EvaluationException e) {
            // TODO integrate logger?
            FutureTask<Void> errorDialog = new FutureTask<>(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Evaluation Exception");
                alert.setHeaderText("Something went wrong during an evaluation");
                alert.setContentText("It's possible that your expression has an error in it" + e.getMessage());
                alert.showAndWait();
                progressIndicator.setDisable(true);
            }, null);
            Platform.runLater(errorDialog);
        }

        Coordinate startCoordinate = new Coordinate(startPointX, startPointY);
        powellMethod = new PowellMethod(tolerance, bounds, startCoordinate, AlgorithmToUse, functionTextField.getText());
        powellMethod.start();
        setRunResult(powellMethod);
        FutureTask<Void> UIUpdate = new FutureTask<>(() -> {
            progressIndicator.setProgress(1);
            updateGraphSameWindow(powellMethod);
        }, null);
        Platform.runLater(UIUpdate);
    }

    private void updateGraphSameWindow(PowellMethod result) {
        Coordinate finalCoordinate = result.getFinalCoordinate();
        String function = result.getFunction();
        ScatterChart.Series RegularScatterChartSeries = createSeriesFromArrayList(result.getUnitVectorSearchList());
        ScatterChart.Series VectorScatterChartSeries = createSeriesFromArrayList(result.getConjugateDirectionSearchList());
        updateGraphSameWindow(finalCoordinate, function, RegularScatterChartSeries, VectorScatterChartSeries, clearExistingDataCheckbox.isSelected());
    }

    private void updateGraphSameWindow(Coordinate finalCoordinate, String function, ScatterChart.Series RegularScatterChartSeries, ScatterChart.Series VectorScatterChartSeries, @Nullable boolean clearExistingData) {
        // Removes existing data from the graph if necessary
        if (clearExistingData) {
            mainSceneGraph.getData().clear();
        }
        RegularScatterChartSeries.setName("Regular Search Data");
        VectorScatterChartSeries.setName("Vector Search Data");
        mainSceneGraph.getData().addAll(RegularScatterChartSeries, VectorScatterChartSeries);

        mainSceneGraph.setTitle("Graph showing path taken to " + finalCoordinate.toString());
        mainSceneGraph.setOnMouseClicked(event -> {
            String sep = System.getProperty("line.separator");
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information about this loaded result");
            alert.setContentText(
                    "The function used in this optimisation was:"
                            + sep + function
            );
            alert.show();
        });
        ScatterChart.Data<Number, Number> finalCoordinateData = new ScatterChart.Data<>();
        finalCoordinateData.setXValue(finalCoordinate.getXValue());
        finalCoordinateData.setYValue(finalCoordinate.getYValue());
        ScatterChart.Series finalCoordinateSeries = new ScatterChart.Series();
        finalCoordinateSeries.getData().add(finalCoordinateData);
        finalCoordinateSeries.setName("Final Coordinate");
        mainSceneGraph.getData().add(finalCoordinateSeries);

    }

/*
    private PowellMethod generateResultObject(ArrayList<Coordinate> vlsCoordinateList, ArrayList<Coordinate> regularSearchCoordinateList, Coordinate finalCoordinate,
                                              String expression, SearchMethod searchMethod) {
        PowellMethod objectToReturn = new PowellMethod();
        objectToReturn.setConjugateDirectionSearchList(vlsCoordinateList);
        objectToReturn.setUnitVectorSearchList(regularSearchCoordinateList);
        objectToReturn.setFinalCoordinate(finalCoordinate);
        objectToReturn.setSearchMethod(searchMethod);
        objectToReturn.setFunction(expression);
        return objectToReturn;
    }*/

    private ScatterChart.Series<Number, Number> createSeriesFromArrayList(ArrayList<Coordinate> ArrayListToConvert) {
        ScatterChart.Series<Number, Number> objectToReturn = new ScatterChart.Series();
        for (Coordinate loopCoordinate : ArrayListToConvert) {
            if (loopCoordinate != null) {
                ScatterChart.Data<Number, Number> vectorLineSearchData = new ScatterChart.Data<>();
                vectorLineSearchData.setXValue(loopCoordinate.getXValue());
                vectorLineSearchData.setYValue(loopCoordinate.getYValue());
                objectToReturn.getData().add(vectorLineSearchData);
            }
        }
        return objectToReturn;
    }

    private void createSeriesAndDrawGraph(ArrayList<Coordinate> ArrayListForSeries1,
                                          Coordinate finalCoordinate,
                                          ArrayList<Coordinate> ArrayListForSeries3, String function) {

        ScatterChart.Series RegularSearchChartSeries = new ScatterChart.Series();
        RegularSearchChartSeries.setName("Regular Search Data");
        ScatterChart.Data<Number, Number> scatterData;
        for (Coordinate loopCoordinate : ArrayListForSeries1) {
            if (loopCoordinate != null) {
                scatterData = new ScatterChart.Data<>();
                scatterData.setXValue(loopCoordinate.getXValue());
                scatterData.setYValue(loopCoordinate.getYValue());
                RegularSearchChartSeries.getData().add(scatterData);
            }

        }

        // Series 2 : final coordinate
        ScatterChart.Series finalCoordinateChartSeries = new ScatterChart.Series();
        double finalX = finalCoordinate.getXValue();
        double finalY = finalCoordinate.getYValue();
        ScatterChart.Data<Number, Number> finalData;
        finalData = new ScatterChart.Data<>();
        finalData.setXValue(finalX);
        finalData.setYValue(finalY);
        finalCoordinateChartSeries.setName("Final Coordinate");

        if (finalCoordinateChartSeries.getData() != null) {
            finalCoordinateChartSeries.getData().add(finalData);
        }

        // Series 3: VLS search data
        ScatterChart.Series vectorSearchChartSeries = new ScatterChart.Series();


        //noinspection Convert2streamapi
        for (Coordinate loopCoordinate : ArrayListForSeries3) {
            if (loopCoordinate != null) {
                ScatterChart.Data<Number, Number> vectorLineSearchData = new ScatterChart.Data<>();
                vectorLineSearchData.setXValue(loopCoordinate.getXValue());
                vectorLineSearchData.setYValue(loopCoordinate.getYValue());
                vectorSearchChartSeries.getData().add(vectorLineSearchData);
            }
        }
        vectorSearchChartSeries.setName("Vector Search Data");
        displayGraph(finalCoordinate, function, RegularSearchChartSeries, finalCoordinateChartSeries, vectorSearchChartSeries);
    }

    private void displayGraph(Coordinate finalCoordinate, String function, ScatterChart.Series... ScatterChartSeries) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setForceZeroInRange(false);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setForceZeroInRange(false);
        ScatterChart<Number, Number> scatterChart = new ScatterChart<>(xAxis, yAxis);
        scatterChart.setTitle(function + " - Scatter Graph of path taken to minimum point: " + finalCoordinate.toString());
        scatterChart.setLegendSide(Side.RIGHT);
        xAxis.setLabel("X");
        yAxis.setLabel("Y");


        // Show graph here
        Stage stage = new Stage();
        Scene scene = new Scene(scatterChart, 700, 700);
        stage.setScene(scene);
        stage.setTitle("Graph View of Function: " + function);
        stage.show();
        stage.setResizable(true);
        scatterChart.setAnimated(true);

        if (ScatterChartSeries != null) {
            for (ScatterChart.Series Series : ScatterChartSeries) {
                if (Series != null) {
                    scatterChart.getData().add(Series);
                }
            }
        }
    }

    private PowellMethod getLoadedResult() {
        return loadedResult;
    }

    private void setLoadedResult(PowellMethod loadedResult) {
        this.loadedResult = loadedResult;
    }

    private PowellMethod getRunResult() {
        return runResult;
    }

    private void setRunResult(PowellMethod runResult) {
        this.runResult = runResult;
    }

    public void setToBoothFunction(ActionEvent actionEvent) {
        functionTextField.setText("(x+2*y-7)^2 + (2*x+y-5)^2");
    }

    public void setToMatyasFunction(ActionEvent actionEvent) {
        functionTextField.setText("0.26*(x^2 + y^2) - 0.48*x*y");
    }

    public void twoDimensionalGraphMode(ActionEvent actionEvent) {
        mainSceneGraph.getData().clear();
        try {
            throw new EvaluationException("DON'T PUSH THIS");
            //PowellMethod loadedIn2DMode = this.getRunResult().xAgainstfX();
            //this.updateGraphSameWindow(loadedIn2DMode.getFinalCoordinate(), loadedIn2DMode.getExpression(), this.createSeriesFromArrayList(loadedIn2DMode.getRegularSearchCoordinateList()), this.createSeriesFromArrayList(loadedIn2DMode.getVectorLineSearchCoordinateList()), this.clearExistingDataCheckbox.isSelected());
        } catch (EvaluationException e) {
            // TODO create handling code
        }

    }

    public void setToMcCormickFunction() {
        functionTextField.setText("(x+y)s1 + (x-y)^2 -1.5*x+2.5*y+1");
    }

    public void setToLeviFunctionN13() {
        functionTextField.setText("(3*p*x)s2+((x-1)^2)*(1+(3*p*y)s2)+((y-1)^2)*(1+(2*p*y)s2)");
    }

    public void addSin(ActionEvent actionEvent) {
        functionTextField.setText(functionTextField.getText() + "()s1");
    }

    public void AddCos(ActionEvent actionEvent) {
        functionTextField.setText(functionTextField.getText() + "()c1");
    }

    public void AddTan(ActionEvent actionEvent) {
        functionTextField.setText(functionTextField.getText() + "()t1");
    }

    public void addSinSquared(ActionEvent actionEvent) {
        functionTextField.setText(functionTextField.getText() + "()s2");
    }

    public void addCosSquared(ActionEvent actionEvent) {
        functionTextField.setText(functionTextField.getText() + "()c2");
    }

    public void addTanSquared(ActionEvent actionEvent) {
        functionTextField.setText(functionTextField.getText() + "");
    }

    public void addE(ActionEvent actionEvent) {
        functionTextField.setText(functionTextField.getText() + "e^()");
    }

    public void addPi(ActionEvent actionEvent) {
        functionTextField.setText(functionTextField.getText() + "p");
    }

    public void closeButtonClicked(ActionEvent actionEvent) {
        System.exit(0);
    }

    @SuppressWarnings("EmptyMethod")
    public void helpButtonClicked(ActionEvent actionEvent) {
        //TODO write some help
    }

}