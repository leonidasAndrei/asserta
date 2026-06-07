package com.leonidasAndrei.asserta;

import com.leonidasAndrei.asserta.controller.GameController;
import com.leonidasAndrei.asserta.model.Game;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Stage primary;
    private static Scene mainScene;
    private static boolean fullScreenMode = true;

    @Override
    public void start(Stage stage) throws Exception {
        primary = stage;

        primary.setTitle("Asserta");
        primary.setResizable(false);

        // primary.initStyle(javafx.stage.StageStyle.UNDECORATED);

        Font.loadFont(getClass().getResourceAsStream("/com/leonidasAndrei/asserta/assets/fonts/Venice-Classic.ttf"), 12); // Venice Classic

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/leonidasAndrei/asserta/fxml/MainMenu.fxml"));
        Parent content = fxmlLoader.load();

        Parent wrappedContent = createScaledWrapper(content);
        mainScene = new Scene(wrappedContent, 1280, 960);

        mainScene.getStylesheets().add(
                App.class.getResource("/com/leonidasAndrei/asserta/css/mystyle.css").toExternalForm()
        );

        mainScene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F11 || event.getCode() == KeyCode.F) {
                fullScreenMode = !primary.isFullScreen();
                primary.setFullScreenExitHint("");
                primary.setFullScreen(fullScreenMode);

                if (!fullScreenMode) {
                    primary.centerOnScreen();
                }
            }
        });

        primary.setScene(mainScene);
        primary.setFullScreenExitHint("");
        primary.setFullScreen(fullScreenMode);
        primary.show();
    }

    public static void switchScene(String filename) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/leonidasAndrei/asserta/fxml/" + filename + ".fxml"));
        Parent content = fxmlLoader.load();

        Parent wrappedContent = createScaledWrapper(content);
        mainScene.setRoot(wrappedContent);

        primary.setFullScreenExitHint("");
        primary.setFullScreen(fullScreenMode);
    }

    public static void switchScene(String fxmlName, Game game) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                App.class.getResource("/com/leonidasAndrei/asserta/fxml/" + fxmlName + ".fxml")
        );
        Parent content = loader.load();

        Object controller = loader.getController();
        if (controller instanceof GameController gc) {
            gc.initGame(game);
        }

        Parent wrappedContent = createScaledWrapper(content);
        mainScene.setRoot(wrappedContent);

        primary.setFullScreenExitHint("");
        primary.setFullScreen(fullScreenMode);
    }

    /**
     * Helper method to wrap layouts programmatically and apply scale handling.
     */
    private static Parent createScaledWrapper(Parent content) {
        StackPane letterboxWrapper = new StackPane();
        letterboxWrapper.setStyle("-fx-background-color: black;");
        letterboxWrapper.getChildren().add(content);

        if (content instanceof Region gameRegion) {
            gameRegion.setMinWidth(1280);
            gameRegion.setMinHeight(960);
            gameRegion.setMaxWidth(1280);
            gameRegion.setMaxHeight(960);

            Scale scaleTransform = new Scale(1, 1, 640, 480);
            gameRegion.getTransforms().add(scaleTransform);

            javafx.beans.value.ChangeListener<Number> resizeListener = (obs, oldVal, newVal) -> {
                double widthScale = letterboxWrapper.getWidth() / 1280.0;
                double heightScale = letterboxWrapper.getHeight() / 960.0;

                double idealScale = Math.min(widthScale, heightScale);

                if (idealScale > 0) {
                    scaleTransform.setX(idealScale);
                    scaleTransform.setY(idealScale);
                }
            };

            letterboxWrapper.widthProperty().addListener(resizeListener);
            letterboxWrapper.heightProperty().addListener(resizeListener);
        }

        return letterboxWrapper;
    }

    public static void main(String[] args) {
        launch();
    }
}