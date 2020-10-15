package de.Jan.JPack;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    public static Stage s;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("App"));
        stage.setScene(scene);

        Menu file = new Menu("File");

        MenuBar mbar = new MenuBar();
        mbar.getMenus().add(file);
        s = stage;
        stage.show();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}