package de.Jan.JPack;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    public static Scene scene;
    public static Stage s;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("App"));
        stage.setScene(scene);

        Menu file = new Menu("File");

        MenuBar mbar = new MenuBar();
        mbar.getMenus().add(file);
        s = stage;
        s.setTitle("JPack");
        stage.show();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        StringBuilder arg = new StringBuilder();
        for(String s : args) {
            arg.append(s);
        }
        File f = new File(arg.toString());
        if(f.exists()) {
            MainController.ass = f;
        }
        launch();
    }

}