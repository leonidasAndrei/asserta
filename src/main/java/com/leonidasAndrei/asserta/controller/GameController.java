package com.leonidasAndrei.asserta.controller;

import com.leonidasAndrei.asserta.App;
import com.leonidasAndrei.asserta.model.Card;
import com.leonidasAndrei.asserta.model.Game;
import com.leonidasAndrei.asserta.model.GamePhase;
import com.leonidasAndrei.asserta.model.Player;
import com.leonidasAndrei.asserta.utils.AnimationHelper;
import com.leonidasAndrei.asserta.utils.AssetLoader;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameController {

    @FXML private Pane      gameWindow, seatsPane;
    @FXML private StackPane deckZone, bluffOverlay, cupModal, announcementOverlay;
    @FXML private Label     declaredRankLabel, roundLabel, messageLabel, cupModalTitle, victoryLabel;
    @FXML private HBox      handBox, actionBar;
    @FXML private Button    callBluffButton, playButton;
    @FXML private VBox      cupModalOverlay, eliminationBox, victoryBox, settingsBox;
    @FXML private Text      announcementTxt, bluffTxt;

    private Game game;
    private final List<Card> selectedCards = new ArrayList<>();
    private boolean isDealingAnimationRunning = false;
    private boolean isInitialStartDone = false;
    private boolean isSpectatingMode = false;
    private String lastActionMessage = "";
    private Timeline messageTimer;
    private Player lastActiveHuman = null;
    private boolean isPaused = false;

    private static final double DECK_CX = 640;
    private static final double DECK_CY = 480;

    // ── INIT ──────────────────────────────────────────────────────────────────
    public void initGame(Game game) {
        this.game = game;
        this.selectedCards.clear();
        this.isDealingAnimationRunning = false;
        this.isInitialStartDone = false;
        this.isSpectatingMode = false;
        this.lastActionMessage = "";
        this.lastActiveHuman = null;
        this.isPaused = false;
        if (messageTimer != null) messageTimer.stop();

        seatsPane.getChildren().clear();
        updateUI();
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────
    public void updateUI() {
        if (game == null) return;

        List<Player> humans = game.getState().getActivePlayers().stream().filter(Player::isHuman).toList();
        long aliveHumans = humans.stream().filter(p -> !p.isEliminated()).count();

        if (aliveHumans == 0 && !isSpectatingMode) {
            showModalSubView(eliminationBox);
            return;
        }

        if (game.getState().getRound() == 0 && !isInitialStartDone) {
            isInitialStartDone = true;
            startNewRoundSequence();
            return;
        }

        GamePhase phase = game.getState().getPhase();
        Player current = game.getState().getCurrentPlayer();
        String rank = game.getState().getDeclaredSymbol();

        declaredRankLabel.setText(rank.isEmpty() ? "" : rank.toUpperCase() + "'S TABLE");
        roundLabel.setText("ROUND " + (game.getState().getRound() + 1));
        messageLabel.setText(lastActionMessage);

        renderDeckZone();
        renderSeatsExcludingCards(isDealingAnimationRunning);
        renderHumanHand();

        boolean isHumanTurn = current != null && current.isHuman() && !current.isEliminated() && !isSpectatingMode;
        boolean canBluff = phase == GamePhase.WAITING && game.getState().getNumberOfTurns() > 0;

        actionBar.setVisible(!isDealingAnimationRunning && !isSpectatingMode);
        callBluffButton.setVisible(!isDealingAnimationRunning && isHumanTurn && canBluff);
        playButton.setVisible(!isDealingAnimationRunning && isHumanTurn);
        playButton.setDisable(selectedCards.isEmpty());

        switch (phase) {
            case NEW_ROUND -> {
                cupModal.setVisible(false);
                if (!isDealingAnimationRunning) startNewRoundSequence();
            }
            case PICKING_POISON -> {
                Player loser = game.getState().getLoser();
                if (loser != null && !cupModal.isVisible()) showCupModal(loser);
            }
            case GAME_OVER -> {
                cupModal.setVisible(false);
                showFinalWinnerOverlay();
            }
            default -> {
                cupModal.setVisible(false);
                if (!isPaused
                        && current != null
                        && (!current.isHuman() || current.isEliminated() || isSpectatingMode)
                        && !isDealingAnimationRunning) {
                    handleBotTurn();
                }
            }
        }
    }

    // ── CONTROLLED SEQUENCING ─────────────────────────────────────────────────
    private void startNewRoundSequence() {
        selectedCards.clear();
        lastActionMessage = "";
        if (messageTimer != null) messageTimer.stop();
        isDealingAnimationRunning = true;

        actionBar.setVisible(false);
        callBluffButton.setVisible(false);
        playButton.setVisible(false);

        AnimationHelper.runDelayed(() -> {
            game.startGame();
            showRoundAnnouncement(() -> animateCardDistribution(() -> {
                isDealingAnimationRunning = false;
                updateUI();
            }));
        }, 600);
    }

    // ── DECK ZONE ─────────────────────────────────────────────────────────────
    private void renderDeckZone() {
        deckZone.getChildren().clear();
        List<Card> table = game.getState().getTableCards();

        if (table.isEmpty()) {
            Rectangle r = new Rectangle(64, 96);
            r.setFill(Color.TRANSPARENT);
            r.setStroke(Color.web("#1a5230"));
            r.setStrokeWidth(2);
            r.getStrokeDashArray().addAll(8.0, 5.0);
            r.setArcWidth(8);
            r.setArcHeight(8);
            deckZone.getChildren().add(r);
            return;
        }

        double[] rots = {-18, 12, -8, 22, -15, 5, -25, 17, -6, 20, -12, 9};
        double[] txs = {-8, 6, -3, 10, -6, 2, -11, 7, -2, 9, -5, 4};
        double[] tys = {4, -7, 9, -4, 7, -10, 3, -8, 11, -3, 6, -9};
        int layers = Math.min(table.size(), rots.length);

        for (int i = 0; i < layers; i++) {
            ImageView iv = AssetLoader.loadCardBack(64, 96);
            iv.setRotate(rots[i]);
            iv.setTranslateX(txs[i]);
            iv.setTranslateY(tys[i]);
            iv.getStyleClass().add("card-style-default");
            deckZone.getChildren().add(iv);
        }
    }

    // ── SEATS (PERSISTENT NODE DESIGN) ────────────────────────────────────────
    private void renderSeatsExcludingCards(boolean hideAllCards) {
        List<Player> ordered = humanFirst(game.getState().getActivePlayers());
        Player current = game.getState().getCurrentPlayer();
        double boxWidth = 240, boxHeight = 110;

        if (seatsPane.getChildren().isEmpty()) {
            for (int i = 0; i < ordered.size() && i < 4; i++) {
                VBox seat = new VBox(6);
                seat.setAlignment(Pos.CENTER);
                seat.getStyleClass().add("seat-box");
                seat.setMinWidth(boxWidth); seat.setPrefWidth(boxWidth); seat.setMaxWidth(boxWidth);
                seat.setMinHeight(boxHeight); seat.setPrefHeight(boxHeight); seat.setMaxHeight(boxHeight);
                seat.setUserData(ordered.get(i));

                switch (i) {
                    case 1 -> {
                        seat.setRotate(90);
                        seat.setLayoutX(280 - (boxWidth / 2));
                        seat.setLayoutY(500 - (boxHeight / 2));
                    }
                    case 3 -> {
                        seat.setRotate(-90);
                        seat.setLayoutX(1003 - (boxWidth / 2));
                        seat.setLayoutY(500 - (boxHeight / 2));
                    }
                    case 2 -> {
                        seat.setLayoutX(640 - (boxWidth / 2));
                        seat.setLayoutY(270 - (boxHeight / 2));
                    }
                    default -> { seat.setLayoutX(640 - (boxWidth / 2)); seat.setLayoutY(932 - (boxHeight / 2)); }
                }
                seatsPane.getChildren().add(seat);
            }
        }

        for (Node node : seatsPane.getChildren()) {
            if (node instanceof VBox seat) {
                Player p = (Player) seat.getUserData();
                boolean isEliminated = p.isEliminated();
                boolean isActive = p.equals(current) && !isEliminated;

                seat.getChildren().clear();
                seat.getStyleClass().remove("seat-box-active");

                String displayName = p.getUsername().toUpperCase() + (isEliminated ? " (OUT)" : "");
                Label name = makeNameLabel(displayName, isActive);

                if (isEliminated) {
                    name.getStyleClass().removeAll("name-node-active", "name-node-inactive");
                    name.setStyle("-fx-text-fill: -banner-red; -fx-background-color: #222222;");
                    seat.setOpacity(0.5);
                } else {
                    seat.setOpacity(1.0);
                }

                HBox cards = (p.isHuman() || hideAllCards || isEliminated) ? new HBox() : makeBotCards(p);

                if (p.isHuman()) seat.getChildren().add(name);
                else if (seatsPane.getChildren().indexOf(seat) == 2) seat.getChildren().addAll(name, cards);
                else seat.getChildren().addAll(cards, name);

                if (isActive && !hideAllCards) {
                    seat.getStyleClass().add("seat-box-active");
                    AnimationHelper.pulseNode(seat);
                } else {
                    AnimationHelper.stopPulseNode(seat);
                }
            }
        }
    }

    // ── HUMAN HAND ────────────────────────────────────────────────────────────
    private void renderHumanHand() {
        handBox.getChildren().clear();
        if (isSpectatingMode) return;

        // show the CURRENT player's hand, not always the first human
        Player current = game.getState().getCurrentPlayer();
        if (current == null || !current.isHuman() || current.isEliminated()) return;

        // clear selection when a different human's turn starts
        if (lastActiveHuman != current) {
            selectedCards.clear();
            playButton.setDisable(true);
            lastActiveHuman = current;
        }

        for (Card card : current.getHand()) {
            boolean sel = isSelectedByIdentity(card);
            StackPane pane = new StackPane(AssetLoader.loadCardFront(card, 96, 136));
            pane.setUserData(card);
            pane.getStyleClass().add("card-container");
            applyCardStyle(pane, sel);

            pane.setOnMouseEntered(e -> {
                if (!isSelectedByIdentity(card)) {
                    AnimationHelper.animateCardLift(pane, -14);
                    pane.getStyleClass().remove("card-style-default");
                    pane.getStyleClass().add("card-style-hover");
                }
            });
            pane.setOnMouseExited(e -> {
                if (!isSelectedByIdentity(card)) {
                    AnimationHelper.animateCardLift(pane, 0);
                    pane.getStyleClass().remove("card-style-hover");
                    pane.getStyleClass().add("card-style-default");
                }
            });
            if (!isDealingAnimationRunning) pane.setOnMouseClicked(e -> toggleCard(card));
            handBox.getChildren().add(pane);
        }
    }

    private void applyCardStyle(StackPane pane, boolean selected) {
        pane.getStyleClass().removeAll("card-style-default", "card-style-hover", "card-style-selected");
        pane.setTranslateY(selected ? -20 : 0);
        pane.getStyleClass().add(selected ? "card-style-selected" : "card-style-default");
    }

    private boolean isSelectedByIdentity(Card card) {
        return selectedCards.stream().anyMatch(c -> c == card);
    }

    private void toggleCard(Card card) {
        if (isSelectedByIdentity(card)) {
            selectedCards.removeIf(c -> c == card);
        } else if (selectedCards.size() < 3) {
            selectedCards.add(card);
        } else {
            messageLabel.setText("MAX 3 CARDS SELECTED.");
            return;
        }
        playButton.setDisable(selectedCards.isEmpty());
        renderHumanHand();
    }

    // ── ANIMATIONS ────────────────────────────────────────────────────────────
    private void animateCardDistribution(Runnable after) {
        renderSeatsExcludingCards(true);
        handBox.getChildren().clear();

        List<Player> ordered = humanFirst(game.getState().getActivePlayers());
        Timeline dealerTimeline = new Timeline();
        int delayOffset = 0;

        for (Player p : ordered) {
            int seatIndex = getPlayerSeatIndex(p);
            double[] targetPos = AnimationHelper.getBotSeatScenePosition(seatIndex);
            double targetWidth = (seatIndex == 0) ? 96 : 48;
            double targetHeight = (seatIndex == 0) ? 136 : 68;

            for (int c = 0; c < p.getHand().size(); c++) {
                KeyFrame cardFrame = new KeyFrame(Duration.millis(delayOffset * 85), e -> {
                    ImageView dealingCard = AssetLoader.loadCardBack(targetWidth, targetHeight);
                    dealingCard.setLayoutX(DECK_CX - (targetWidth / 2));
                    dealingCard.setLayoutY(DECK_CY - (targetHeight / 2));
                    dealingCard.setOpacity(0.0);
                    dealingCard.getStyleClass().add("card-style-default");
                    gameWindow.getChildren().add(dealingCard);

                    TranslateTransition fly = new TranslateTransition(Duration.millis(320), dealingCard);
                    fly.setToX(targetPos[0] - DECK_CX);
                    fly.setToY(targetPos[1] - DECK_CY);

                    FadeTransition reveal = new FadeTransition(Duration.millis(80), dealingCard);
                    reveal.setToValue(1.0);

                    ParallelTransition group = new ParallelTransition(fly, reveal);
                    group.setOnFinished(evt -> gameWindow.getChildren().remove(dealingCard));
                    group.play();
                });
                dealerTimeline.getKeyFrames().add(cardFrame);
                delayOffset++;
            }
        }

        dealerTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(delayOffset > 0 ? ((delayOffset - 1) * 85) + 340 : 10), e -> {
            if (after != null) after.run();
        }));
        dealerTimeline.play();
    }

    private void showRoundAnnouncement(Runnable after) {
        showAnnouncement(game.getState().getDeclaredSymbol().toUpperCase() + "'S TABLE", after);
    }

    private void showAnnouncement(String text, Runnable after) {
        announcementTxt.setText(text);
        announcementOverlay.setOpacity(0);
        announcementOverlay.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(450), announcementOverlay);
        fadeIn.setToValue(1.0);
        fadeIn.setOnFinished(e -> AnimationHelper.runDelayed(() -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(450), announcementOverlay);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> {
                announcementOverlay.setVisible(false);
                if (after != null) after.run();
            });
            fadeOut.play();
        }, 1500));
        fadeIn.play();
    }

    private void showBluffAlert(Runnable after) {
        bluffOverlay.setOpacity(0);
        bluffOverlay.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), bluffOverlay);
        fadeIn.setToValue(1.0);
        ScaleTransition scale = new ScaleTransition(Duration.millis(250), bluffTxt);
        scale.setFromX(0.4); scale.setToX(1.0); scale.setFromY(0.4); scale.setToY(1.0);

        ParallelTransition show = new ParallelTransition(fadeIn, scale);
        show.setOnFinished(e -> AnimationHelper.runDelayed(() -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(350), bluffOverlay);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> {
                bluffOverlay.setVisible(false);
                after.run();
            });
            fadeOut.play();
        }, 1200));
        show.play();
    }

    private void animateGhostsToDeck(List<double[]> sourcePositions, Runnable after) {
        if (sourcePositions.isEmpty()) {
            after.run();
            return;
        }
        int[] done = {0};

        for (double[] pos : sourcePositions) {
            ImageView ghost = AssetLoader.loadCardBack(96, 136);
            ghost.setLayoutX(pos[0] - 48); ghost.setLayoutY(pos[1] - 68);
            ghost.getStyleClass().add("card-style-default");
            gameWindow.getChildren().add(ghost);

            TranslateTransition fly = new TranslateTransition(Duration.millis(380), ghost);
            fly.setToX(DECK_CX - pos[0]); fly.setToY(DECK_CY - pos[1]);

            FadeTransition fade = new FadeTransition(Duration.millis(380), ghost);
            fade.setToValue(0.1);

            ScaleTransition shrink = new ScaleTransition(Duration.millis(380), ghost);
            shrink.setToX(0.4); shrink.setToY(0.4);

            ParallelTransition anim = new ParallelTransition(fly, fade, shrink);
            anim.setOnFinished(e -> {
                gameWindow.getChildren().remove(ghost);
                if (++done[0] == sourcePositions.size()) after.run();
            });
            anim.play();
        }
    }

    private List<double[]> getHandCardPositions(List<Card> toPlay) {
        List<double[]> positions = new ArrayList<>();
        for (var node : handBox.getChildren()) {
            if (node instanceof StackPane sp && toPlay.contains(sp.getUserData())) {
                var b = sp.localToScene(sp.getBoundsInLocal());
                positions.add(new double[]{ b.getMinX() + b.getWidth() / 2, b.getMinY() + b.getHeight() / 2 });
            }
        }
        return positions;
    }

    // ── ACTIONS ───────────────────────────────────────────────────────────────
    @FXML
    public void onPlayClicked() {
        if (selectedCards.isEmpty()) return;
        List<Card> toPlay = new ArrayList<>(selectedCards);
        selectedCards.clear();
        playButton.setDisable(true);

        Player current = game.getState().getCurrentPlayer();
        String name = (current != null) ? current.getUsername().toUpperCase() : "PLAYER";
        startActionMessageTimer(name + " PLAYS " + toPlay.size() + " " + game.getState().getDeclaredSymbol().toUpperCase() + "(s)");

        animateGhostsToDeck(getHandCardPositions(toPlay), () -> {
            game.playTurn(toPlay);
            updateUI();
        });
    }

    @FXML
    public void onCallBluffClicked() {
        showBluffAlert(() -> {
            game.callBluff();
            updateUI();
        });
    }

    private void showModalSubView(VBox activeSubView) {
        cupModal.setVisible(true);
        cupModal.setManaged(true);
        List.of(cupModalOverlay, eliminationBox, victoryBox, settingsBox).forEach(view -> {
            boolean isTarget = view.equals(activeSubView);
            view.setVisible(isTarget);
            view.setManaged(isTarget);
        });
    }

    // ── DYNAMIC CUP MODAL LOGIC ───────────────────────────────────────────────
    private void showCupModal(Player loser) {
        cupModalTitle.setText("\"" + loser.getUsername().toUpperCase() + "\" WAS WRONG!\nSELECT A CUP TO DRINK CONSEQUENCES:");
        cupModalOverlay.getChildren().removeIf(node -> node instanceof HBox);

        HBox cupsRow = new HBox(50);
        cupsRow.setAlignment(Pos.CENTER);
        cupsRow.setPadding(new Insets(20, 40, 20, 40));

        boolean[] available = game.getState().getCupsAvailable();

        for (int i = 0; i < 4; i++) {
            final int index = i;
            ImageView cupImg = AssetLoader.loadCupImage(available[i] ? i + 1 : 0, 84, 144);
            StackPane cupPane = new StackPane(cupImg);
            cupPane.setUserData(index);
            cupPane.getStyleClass().add("card-container");
            cupPane.getStyleClass().add("card-style-default");

            if (!available[i]) {
                cupPane.setDisable(true);
            } else if (loser.isHuman()) {
                cupPane.setOnMouseEntered(e -> {
                    AnimationHelper.animateCardLift(cupPane, -14);
                    cupPane.getStyleClass().remove("card-style-default");
                    cupPane.getStyleClass().add("card-style-hover");
                });
                cupPane.setOnMouseExited(e -> {
                    AnimationHelper.animateCardLift(cupPane, 0);
                    cupPane.getStyleClass().remove("card-style-hover");
                    cupPane.getStyleClass().add("card-style-default");
                });
                cupPane.setOnMouseClicked(e -> handleCupPickedSequence(index, cupPane, cupsRow, loser));
            }
            cupsRow.getChildren().add(cupPane);
        }

        cupModalOverlay.getChildren().add(cupsRow);
        showModalSubView(cupModalOverlay);

        if (!loser.isHuman()) handleBotPoisonPick(cupsRow, loser);
    }

    private void showFinalWinnerOverlay() {
        List<Player> remaining = game.getState().getActivePlayers().stream().filter(p -> !p.isEliminated()).toList();
        victoryLabel.setText((remaining.isEmpty() ? "PLAYER" : remaining.get(0).getUsername().toUpperCase()) + " IS VICTORIOUS!");
        showModalSubView(victoryBox);
    }

    @FXML
    public void onSpectateClicked() {
        isSpectatingMode = true;
        cupModal.setVisible(false);
        updateUI();
    }

    @FXML
    public void onMainMenuClicked() {
        try { App.switchScene("Setup", game); } catch (IOException ex) { ex.printStackTrace(); }
    }

    private void handleCupPickedSequence(int chosenIndex, StackPane chosenPane, HBox cupsRow, Player loser) {
        cupsRow.setDisable(true);
        chosenPane.getStyleClass().remove("card-style-default");
        chosenPane.getStyleClass().add("card-style-selected");
        chosenPane.setTranslateY(-20);

        AnimationHelper.runDelayed(() -> {
            int livesBefore = loser.getLives();
            game.pickPoison(chosenIndex);
            cupModal.setVisible(false);
            processPoisonResult(loser, livesBefore);
        }, 900);
    }

    private void handleBotPoisonPick(HBox cupsRow, Player loser) {
        boolean[] available = game.getState().getCupsAvailable();
        List<Integer> choices = new ArrayList<>();
        for (int i = 0; i < 4; i++) if (available[i]) choices.add(i);

        if (choices.isEmpty()) { updateUI(); return; }
        int botChoice = choices.get(new Random().nextInt(choices.size()));

        AnimationHelper.runDelayed(() -> {
            if (botChoice < cupsRow.getChildren().size()) {
                StackPane target = (StackPane) cupsRow.getChildren().get(botChoice);
                target.getStyleClass().remove("card-style-default");
                target.getStyleClass().add("card-style-selected");
                target.setTranslateY(-20);
            }
            AnimationHelper.runDelayed(() -> {
                int livesBefore = loser.getLives();
                game.pickPoison(botChoice);
                cupModal.setVisible(false);
                processPoisonResult(loser, livesBefore);
            }, 1000);
        }, 1200);
    }

    private void processPoisonResult(Player loser, int livesBefore) {
        String msg = loser.getUsername().toUpperCase() + (loser.getLives() < livesBefore ? "\nLOST A LIFE!" : "\nSURVIVED!");
        showAnnouncement(msg, () -> {
            Player stillLoser = game.getState().getLoser();
            if (stillLoser != null && game.getState().getPhase() == GamePhase.PICKING_POISON) {
                showCupModal(stillLoser);
            } else {
                updateUI();
            }
        });
    }

    @FXML
    public void onSettingsClicked() {
        if (game != null && game.getState().getPhase() != GamePhase.GAME_OVER) {
            isPaused = true;
            showModalSubView(settingsBox);
        }
    }


    @FXML
    public void onReturnToGameClicked() {
        isPaused = false;
        cupModal.setVisible(false);
        cupModal.setManaged(false);
        updateUI();
    }

    @FXML
    public void onForfeitClicked() {
        cupModal.setVisible(false);
        cupModal.setManaged(false);

        Player forfeiter = humanPlayer();
        if (forfeiter == null) return;

        while (!forfeiter.isEliminated()) {
            forfeiter.loseLife();
        }
        game.getState().getActivePlayers().remove(forfeiter);

        showAnnouncement(forfeiter.getUsername().toUpperCase() + "\nFORFEITED...", () -> {
            if (game.checkWinner()) {
                updateUI();
            } else {
                game.getState().setPhase(GamePhase.NEW_ROUND);
                startNewRoundSequence();
            }
        });
    }

    @FXML
    public void onRematchClicked() {

    }

    // ── BOT MANAGEMENT ────────────────────────────────────────────────────────
    private void handleBotTurn() {
        Player currentBot = game.getState().getCurrentPlayer();
        final int finalSeatIndex = getPlayerSeatIndex(currentBot);

        AnimationHelper.runDelayed(() -> {
            if (isPaused) return;
            List<Card> played = game.playBotTurn();
            if (!played.isEmpty()) {
                if (currentBot != null) {
                    startActionMessageTimer(currentBot.getUsername().toUpperCase()
                            + " PLAYS " + played.size() + " "
                            + game.getState().getDeclaredSymbol().toUpperCase() + "(s)");
                }
                List<double[]> positions = new ArrayList<>();
                double[] base = AnimationHelper.getBotSeatScenePosition(finalSeatIndex);
                for (int i = 0; i < played.size(); i++) {
                    positions.add(new double[]{
                            base[0] + (i - played.size() / 2.0) * 16, base[1]
                    });
                }
                animateGhostsToDeck(positions, this::updateUI);
            } else {
                showBluffAlert(this::updateUI);
            }
        }, 2000);
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────
    private int getPlayerSeatIndex(Player p) {
        for (int i = 0; i < seatsPane.getChildren().size(); i++) {
            if (seatsPane.getChildren().get(i).getUserData() == p) return i;
        }
        return 0;
    }

    private void startActionMessageTimer(String msg) {
        lastActionMessage = msg;
        messageLabel.setText(lastActionMessage);

        if (messageTimer != null) messageTimer.stop();

        messageTimer = new Timeline(new KeyFrame(Duration.seconds(7), e -> {
            lastActionMessage = "";
            messageLabel.setText("");
        }));
        messageTimer.play();
    }

    private Label makeNameLabel(String text, boolean isActive) {
        Label l = new Label(text.toUpperCase());
        l.setMinWidth(120);
        l.setAlignment(Pos.CENTER);
        l.getStyleClass().addAll("name-label-node", isActive ? "name-node-active" : "name-node-inactive");
        return l;
    }

    private HBox makeBotCards(Player p) {
        HBox row = new HBox(4);
        row.setAlignment(Pos.CENTER);
        for (int j = 0; j < p.getHand().size(); j++) {
            ImageView back = AssetLoader.loadCardBack(48, 68);
            back.getStyleClass().add("card-style-default");
            row.getChildren().add(back);
        }
        return row;
    }

    private List<Player> humanFirst(List<Player> playersList) {
        int hi = 0;
        Player current = game.getState().getCurrentPlayer();

        for (int i = 0; i < playersList.size(); i++) {
            if (playersList.get(i).isHuman()) {
                hi = i;
                if (current != null && current.isHuman() && playersList.get(i).equals(current)) break;
            }
        }
        List<Player> out = new ArrayList<>();
        for (int i = 0; i < playersList.size(); i++) {
            out.add(playersList.get((hi + i) % playersList.size()));
        }
        return out;
    }

    private Player humanPlayer() {
        return game.getState().getActivePlayers().stream().filter(Player::isHuman).findFirst().orElse(null);
    }
}