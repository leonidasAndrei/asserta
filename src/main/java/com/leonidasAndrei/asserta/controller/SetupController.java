package com.leonidasAndrei.asserta.controller;

import com.leonidasAndrei.asserta.App;
import com.leonidasAndrei.asserta.model.Game;
import com.leonidasAndrei.asserta.model.Player;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SetupController {

    @FXML private VBox nameFieldsBox;
    @FXML private Label errorLabel;
    @FXML private Button addPlayerButton;

    private static final int MAX_PLAYERS = 4;

    private final List<HBox> playerRows = new ArrayList<>();

    @FXML
    public void initialize() {
        addPlayerRow(); // start with 1 player
    }

    @FXML
    public void onAddPlayerClicked() {
        if (playerRows.size() >= MAX_PLAYERS) return;

        addPlayerRow();

        if (playerRows.size() >= MAX_PLAYERS) {
            addPlayerButton.setVisible(false);
            addPlayerButton.setManaged(false);
        }
    }

    private void addPlayerRow() {
        HBox row = new HBox(10);
        row.setAlignment(javafx.geometry.Pos.CENTER);

        TextField tf = new TextField();

        tf.setTextFormatter(new javafx.scene.control.TextFormatter<String>(change -> { // MAX characters to 32
            if (change.getControlNewText().length() <= 32) {
                return change;
            }
            return null;
        }));

        tf.setPromptText("Player " + (playerRows.size() + 1) + " name");
        tf.getStyleClass().add("name-field");
        tf.prefWidthProperty().bind(nameFieldsBox.widthProperty().multiply(0.5));

        Button removeBtn = new Button("-");
        removeBtn.getStyleClass().add("menu-button");
        removeBtn.getStyleClass().add("utility-btn");

        removeBtn.setOnAction(e -> removePlayerRow(row));

        row.getChildren().addAll(tf, removeBtn);

        playerRows.add(row);
        refreshPlayerUI();
//        nameFieldsBox.getChildren().add(row);
    }
    private void removePlayerRow(HBox row) {
        if (playerRows.size() <= 1) {
            errorLabel.setText("At least one player is required.");
            return;
        }

        playerRows.remove(row);

        errorLabel.setText("");

        if (playerRows.size() < MAX_PLAYERS) {
            addPlayerButton.setVisible(true);
            addPlayerButton.setManaged(true);
        }

        refreshPlayerUI();
    }

    private void refreshPlayerUI() {
        nameFieldsBox.getChildren().clear();

        boolean canRemove = playerRows.size() > 1;

        for (int i = 0; i < playerRows.size(); i++) {
            HBox row = playerRows.get(i);

            TextField tf = (TextField) row.getChildren().get(0);

            Button removeBtn = (Button) row.getChildren().get(1);

            tf.setPromptText("Player " + (i + 1) + " name");

            removeBtn.setDisable(!canRemove);

            nameFieldsBox.getChildren().add(row);
        }
    }

    @FXML
    public void onStartClicked() throws IOException {
        Game game = new Game();

        int index = 1;

        for (HBox row : playerRows) {
            TextField tf = (TextField) row.getChildren().get(0);
            String name = tf.getText().trim();

            if (name.isEmpty()) {
                errorLabel.setText("Please enter a name for every player.");
                return;
            }

            game.addPlayer(new Player(name, index++, true));
        }

        // fill bots
        int humanCount = playerRows.size();

        for (int i = humanCount + 1; i <= MAX_PLAYERS; i++) {
            game.addPlayer(new Player("Bot " + (i - humanCount), i, false));
        }

        game.startGame();
        App.switchScene("Game", game);
    }

    @FXML
    public void onBackClicked() throws IOException {
        App.switchScene("MainMenu");
    }
}