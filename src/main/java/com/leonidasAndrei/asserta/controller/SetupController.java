package com.leonidasAndrei.asserta.controller;

import com.leonidasAndrei.asserta.App;
import com.leonidasAndrei.asserta.model.Game;
import com.leonidasAndrei.asserta.model.Player;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SetupController {

    @FXML private Pane lobbyWindow;
    @FXML private GridPane lobbyGrid;
    @FXML private Label lobbyStatusLbl, errorLbl;

    // A single clean data structures instead of untracked UI Nodes
    private final List<SlotState> slots = new ArrayList<>();
    private final int MAX_PLAYERS = 4;

    // Lightweight inner state tracker to bridge Data and UI
    private static class SlotState {
        boolean isHuman;
        TextField nameField; // Direct reference! No UI scraping needed.
        String defaultBotName;

        SlotState(boolean isHuman, String defaultBotName) {
            this.isHuman = isHuman;
            this.defaultBotName = defaultBotName;
            this.nameField = new TextField();
            this.nameField.setPromptText("ENTER NAME...");
            this.nameField.getStyleClass().add("name-field");
            this.nameField.setMaxWidth(420.0);

            // Apply character limit rule directly on creation
            this.nameField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.length() > 16) {
                    this.nameField.setText(oldVal);
                }
            });
        }
    }

    @FXML
    public void initialize() {
        // Initialize our 4 states explicitly
        slots.add(new SlotState(true, "Bot 1"));
        slots.add(new SlotState(false, "Bot 1"));
        slots.add(new SlotState(false, "Bot 2"));
        slots.add(new SlotState(false, "Bot 3"));

        javafx.application.Platform.runLater(() -> {
            lobbyWindow.requestFocus();
        });
        refreshLobbyGrid();
    }

    private void refreshLobbyGrid() {
        lobbyGrid.getChildren().clear();
        if (errorLbl != null) errorLbl.setText("");

        long humanCount = slots.stream().filter(s -> s.isHuman).count();
        lobbyStatusLbl.setText("Waiting for players... (" + humanCount + "/" + MAX_PLAYERS + ")");

        int botCounter = 1;

        for (int i = 0; i < MAX_PLAYERS; i++) {
            final int index = i;
            SlotState slot = slots.get(i);

            HBox cardContainer = new HBox();
            cardContainer.setAlignment(Pos.CENTER);
            cardContainer.setPadding(new Insets(15, 25, 15, 25));

            VBox innerContent = new VBox();
            innerContent.setAlignment(Pos.CENTER);

            if (slot.isHuman) {
                cardContainer.getStyleClass().add("lobby-card-tile-active");
                innerContent.setSpacing(8);

                Label headerLabel = new Label("PLAYER " + (i + 1));
                headerLabel.getStyleClass().add("lobby-player-header-label");

                Button removeBtn = new Button("REMOVE");
                removeBtn.getStyleClass().addAll("removeSlotBtn", "menuBtn", "dangerBtn");
                if (humanCount <= 1) removeBtn.setVisible(false);
                removeBtn.setOnAction(e -> handleRemovePlayer(index));

                // Reuse the clean layout-safe textfield reference
                innerContent.getChildren().addAll(headerLabel, slot.nameField, removeBtn);
            } else {
                cardContainer.getStyleClass().add("lobby-card-tile-empty");

                // Keep the internal dynamic naming correct even during shifts
                slot.defaultBotName = "Bot " + botCounter++;

                Label botLabel = new Label(slot.defaultBotName.toUpperCase() + "\n(CLICK TO JOIN)");
                botLabel.getStyleClass().add("lobby-player-header-label");
                innerContent.getChildren().add(botLabel);
                cardContainer.setOnMouseClicked(e -> handleAddPlayer());
            }

            cardContainer.getChildren().add(innerContent);

            int col = i % 2;
            int row = i / 2;
            GridPane.setHalignment(cardContainer, javafx.geometry.HPos.CENTER);
            GridPane.setValignment(cardContainer, javafx.geometry.VPos.CENTER);
            lobbyGrid.add(cardContainer, col, row);
        }
    }

    private void handleAddPlayer() {
        // Find the first non-human slot and turn it active
        for (SlotState slot : slots) {
            if (!slot.isHuman) {
                slot.isHuman = true;
                slot.nameField.clear(); // Wipe text field fresh for the new user input
                break;
            }
        }
        refreshLobbyGrid();
    }

    private void handleRemovePlayer(int index) {
        // Drop the active slot item completely
        slots.remove(index);
        // Create an empty backfill bot target at the end of the line
        slots.add(new SlotState(false, ""));
        refreshLobbyGrid();
    }

    @FXML
    public void onBackClicked() throws IOException {
        App.switchScene("MainMenu");
    }

    @FXML
    public void onStartClicked() throws IOException {
        if (errorLbl != null) errorLbl.setText("");

        // Count human players
        long humanCount = slots.stream().filter(s -> s.isHuman).count();

        if (humanCount == 0) {
            showError("You must add at least one human player.");
            return;
        }

        Game game = new Game();

        for (int i = 0; i < MAX_PLAYERS; i++) {
            SlotState slot = slots.get(i);
            int idPosition = i + 1;

            if (slot.isHuman) {
                String name = slot.nameField.getText().trim();
                if (name.isEmpty()) {
                    showError("Please enter a name for every player.");
                    return;
                }
                game.addPlayer(new Player(name, idPosition, true));
            } else {
                game.addPlayer(new Player(slot.defaultBotName, idPosition, false));
            }
        }

        game.startGame();
        App.switchScene("Game", game);
    }

    // Helper to keep code clean
    private void showError(String message) {
        if (errorLbl != null) {
            errorLbl.setText(message);
            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(e -> errorLbl.setText(""));
            delay.play();
        }
    }
}