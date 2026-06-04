package com.leonidasAndrei.asserta;

import com.leonidasAndrei.asserta.controller.GameController;
import com.leonidasAndrei.asserta.model.Game;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.stage.StageStyle;

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
        primary.initStyle(StageStyle.UNDECORATED);

        Font.loadFont(getClass().getResourceAsStream("/com/leonidasAndrei/asserta/assets/fonts/Venice-Classic.ttf"), 12); //Venice Classic

        switchScene("MainMenu");

        primary.show();
    }

    public static void switchScene(String filename) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/leonidasAndrei/asserta/fxml/" + filename + ".fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(
                App.class.getResource("/com/leonidasAndrei/asserta/css/mystyle.css").toExternalForm()
        );
        primary.setScene(scene);
    }


    public static void switchScene(String fxmlName, Game game) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                App.class.getResource("/com/leonidasAndrei/asserta/fxml/" + fxmlName + ".fxml")
        );
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(
                App.class.getResource("/com/leonidasAndrei/asserta/css/mystyle.css").toExternalForm()
        );

        //Initialise game
        Object controller = loader.getController();
        if (controller instanceof GameController gc) {
            gc.initGame(game);
        }

        primary.setScene(scene);
    }

    public static void main(String[] args) {
        launch();
    }
}
