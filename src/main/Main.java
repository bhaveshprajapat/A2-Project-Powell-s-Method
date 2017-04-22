package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/*
    Necessary class to load up the FX GUI
 */
public class Main extends Application {
    public static void main(String[] args) {
        Application.launch(args);

    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        final Class<? extends Main> aClass = getClass();
        Parent root = FXMLLoader.load(aClass.getResource("mainScene.fxml"));
        primaryStage.setTitle("Powell's Conjugate Direction Method");
        primaryStage.setScene(new Scene(root, 1000.0, 700.0));
        primaryStage.setMaximized(true);
        primaryStage.show();


    }
}