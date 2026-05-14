package com.leonidasAndrei.asserta;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.text.Font;

/**
 * Hello world!
 *
 */
public class App extends Application {

    private Stage primary;
    @Override
    public void start(Stage stage) throws Exception {
        primary = stage;

        primary.setTitle("Asserta");
        primary.setResizable(false);

        Font.loadFont(getClass().getResourceAsStream("/com/leonidasAndrei/asserta/fonts/ARCADE_I.TTF"),12); //Arcade Interlaced
        Font.loadFont(getClass().getResourceAsStream("/com/leonidasAndrei/asserta/fonts/ARCADE_N.TTF"),12); //Arcade Normal

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/leonidasAndrei/asserta/fxml/MainMenu.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        scene.getStylesheets().add(
                App.class.getResource("/com/leonidasAndrei/asserta/css/style.css").toExternalForm()
        );

        primary.setScene(scene);
        primary.show();
    }

    public static void main( String[] args ) {
        launch();
    }
}
