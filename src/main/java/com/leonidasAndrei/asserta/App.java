package com.leonidasAndrei.asserta;

import com.leonidasAndrei.asserta.model.Game;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.text.Font;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App extends Application {

    private static Stage primary;
    @Override
    public void start(Stage stage) throws Exception {
        primary = stage;

        primary.setTitle("Asserta");
        primary.setResizable(false);

        Font.loadFont(getClass().getResourceAsStream("/com/leonidasAndrei/asserta/fonts/ARCADE_I.TTF"), 12); //Arcade Interlaced
        Font.loadFont(getClass().getResourceAsStream("/com/leonidasAndrei/asserta/fonts/ARCADE_N.TTF"), 12); //Arcade Normal

        switchScene("MainMenu");

        primary.show();
    }

    public static void switchScene(String filename) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/leonidasAndrei/asserta/fxml/" + filename + ".fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(
                App.class.getResource("/com/leonidasAndrei/asserta/css/style.css").toExternalForm()
        );
        primary.setScene(scene);
    }


    public static void switchScene(String fxmlName, Game game) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                App.class.getResource("/com/leonidasAndrei/asserta/fxml/" + fxmlName + ".fxml")
        );
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(
                App.class.getResource("/com/leonidasAndrei/asserta/css/style.css").toExternalForm()
        );

        //Initialise game

        primary.setScene(scene);
    }

    public static void main(String[] args) {
        launch();
    }
}
