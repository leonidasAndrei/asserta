package com.leonidasAndrei.asserta.controller;

import com.leonidasAndrei.asserta.App;
import com.leonidasAndrei.asserta.model.Card;
import com.leonidasAndrei.asserta.model.Game;
import com.leonidasAndrei.asserta.model.GamePhase;
import com.leonidasAndrei.asserta.model.Player;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameController {

    @FXML private Pane      rootPane;
    @FXML private Pane      seatsPane;
    @FXML private StackPane deckZone;
    @FXML private StackPane bluffOverlay;
    @FXML private StackPane cupModal;
    @FXML private StackPane announcementOverlay;
    @FXML private Label     declaredRankLabel;
    @FXML private Label     roundLabel;
    @FXML private Label     messageLabel;
    @FXML private Label     bluffLabel;
    @FXML private Label     announcementLabel;
    @FXML private Label     cupModalTitle;
    @FXML private HBox      handBox;
    @FXML private Button    callBluffButton;
    @FXML private Button    playButton;
    @FXML private HBox      actionBar;
    @FXML private VBox      cupModalOverlay;
    @FXML private VBox      eliminationBox;
    @FXML private VBox      victoryBox;
    @FXML private Label     victoryLabel;

    private Game game;
    private final List<Card> selectedCards = new ArrayList<>();
    private boolean isDealingAnimationRunning = false;
    private boolean isInitialStartDone = false;
    private boolean isSpectatingMode = false;
    private String lastActionMessage = "";
    private Timeline messageTimer;

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
        if (messageTimer != null) {
            messageTimer.stop();
        }

        seatsPane.getChildren().clear();

        updateUI();
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────
    public void updateUI() {
        if (game == null) return;

        List<Player> humans = game.getState().getActivePlayers().stream().filter(Player::isHuman).toList();
        long aliveHumans = humans.stream().filter(p -> !p.isEliminated()).count();

        if (aliveHumans == 0 && !isSpectatingMode) {
            showEliminationOrGameOverOverlay(humans.size() > 1);
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
                if (!isDealingAnimationRunning) {
                    startNewRoundSequence();
                }
            }
            case PICKING_POISON -> {
                Player loser = game.getState().getLoser();
                if (loser != null && !cupModal.isVisible()) {
                    showCupModal(loser);
                }
            }
            case GAME_OVER -> {
                cupModal.setVisible(false);
                showFinalWinnerOverlay();
            }
            default -> {
                cupModal.setVisible(false);
                if (current != null && (!current.isHuman() || current.isEliminated() || isSpectatingMode) && !isDealingAnimationRunning) {
                    handleBotTurn();
                }
            }
        }
    }

    // ── CONTROLLED SEQUENCING ─────────────────────────────────────────────────
    private void startNewRoundSequence() {
        selectedCards.clear();
        lastActionMessage = "";
        if (messageTimer != null) {
            messageTimer.stop();
        }
        isDealingAnimationRunning = true;

        actionBar.setVisible(false);
        callBluffButton.setVisible(false);
        playButton.setVisible(false);

        runDelayed(() -> {
            game.startGame();
            showRoundAnnouncement(() -> {
                animateCardDistribution(() -> {
                    isDealingAnimationRunning = false;
                    updateUI();
                });
            });
        }, 600);
    }

    // ── DECK ZONE ─────────────────────────────────────────────────────────────
    private void renderDeckZone() {
        deckZone.getChildren().clear();
        List<Card> table = game.getState().getTableCards();

        if (table.isEmpty()) {
            Rectangle r = new Rectangle(96, 136);
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
            ImageView iv = loadCardBack(96, 136);
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

        double boxWidth = 240;
        double boxHeight = 110;

        if (seatsPane.getChildren().isEmpty()) {
            for (int i = 0; i < ordered.size() && i < 4; i++) {
                Player p = ordered.get(i);
                VBox seat = new VBox(6);
                seat.setAlignment(Pos.CENTER);
                seat.getStyleClass().add("seat-box");

                seat.setMinWidth(boxWidth);
                seat.setPrefWidth(boxWidth);
                seat.setMaxWidth(boxWidth);
                seat.setMinHeight(boxHeight);
                seat.setPrefHeight(boxHeight);
                seat.setMaxHeight(boxHeight);
                seat.setUserData(p);

                if (i == 1) {
                    seat.setRotate(90);
                    seat.setLayoutX(130 - (boxWidth / 2));
                    seat.setLayoutY(480 - (boxHeight / 2));
                } else if (i == 3) {
                    seat.setRotate(-90);
                    seat.setLayoutX(1150 - (boxWidth / 2));
                    seat.setLayoutY(480 - (boxHeight / 2));
                } else if (i == 2) {
                    seat.setLayoutX(640 - (boxWidth / 2));
                    seat.setLayoutY(85 - (boxHeight / 2));
                } else {
                    seat.setLayoutX(640 - (boxWidth / 2));
                    seat.setLayoutY(932 - (boxHeight / 2));
                }
                seatsPane.getChildren().add(seat);
            }
        }

        for (Node node : seatsPane.getChildren()) {
            if (node instanceof VBox seat) {
                Player p = (Player) seat.getUserData();
                boolean isEliminated = p.isEliminated();
                boolean isActive = p.equals(current) && !isEliminated;
                boolean isHuman = p.isHuman();

                int index = seatsPane.getChildren().indexOf(seat);
                boolean isTop = (index == 2);

                seat.getChildren().clear();
                seat.getStyleClass().remove("seat-box-active");

                String displayName = p.getUsername().toUpperCase() + (isEliminated ? " (OUT)" : "");
                Label name = makeNameLabel(displayName, isActive);

                if (isEliminated) {
                    name.getStyleClass().removeAll("name-node-active", "name-node-inactive");
                    name.setStyle("-fx-text-fill: #555555; -fx-background-color: #222222;");
                    seat.setOpacity(0.35);
                } else {
                    seat.setOpacity(1.0);
                }

                HBox cards = (isHuman || hideAllCards || isEliminated) ? new HBox() : makeBotCards(p);

                if (isHuman) {
                    seat.getChildren().add(name);
                } else if (isTop) {
                    seat.getChildren().addAll(name, cards);
                } else {
                    seat.getChildren().addAll(cards, name);
                }

                if (isActive && !hideAllCards) {
                    seat.getStyleClass().add("seat-box-active");
                    pulseNode(seat);
                } else {
                    stopPulseNode(seat);
                }
            }
        }
    }

    // ── HUMAN HAND ────────────────────────────────────────────────────────────
    private void renderHumanHand() {
        handBox.getChildren().clear();
        if (isDealingAnimationRunning || isSpectatingMode) return;

        Player human = humanPlayer();
        if (human == null || human.isEliminated()) return;

        for (Card card : human.getHand()) {
            boolean sel = isSelectedByIdentity(card);
            ImageView img = loadCardFront(card, 96, 136);
            StackPane pane = new StackPane(img);
            pane.setUserData(card);
            pane.getStyleClass().add("card-container");

            applyCardStyle(pane, sel);

            pane.setOnMouseEntered(e -> {
                if (!isSelectedByIdentity(card)) {
                    animateCardLift(pane, -14);
                    pane.getStyleClass().removeAll("card-style-default");
                    pane.getStyleClass().add("card-style-hover");
                }
            });
            pane.setOnMouseExited(e -> {
                if (!isSelectedByIdentity(card)) {
                    animateCardLift(pane, 0);
                    pane.getStyleClass().removeAll("card-style-hover");
                    pane.getStyleClass().add("card-style-default");
                }
            });
            pane.setOnMouseClicked(e -> toggleCard(card));
            handBox.getChildren().add(pane);
        }
    }

    private void applyCardStyle(StackPane pane, boolean selected) {
        pane.getStyleClass().removeAll("card-style-default", "card-style-hover", "card-style-selected");
        if (selected) {
            pane.setTranslateY(-20);
            pane.getStyleClass().add("card-style-selected");
        } else {
            pane.setTranslateY(0);
            pane.getStyleClass().add("card-style-default");
        }
    }

    private void animateCardLift(StackPane pane, double targetY) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(100), pane);
        tt.setToY(targetY);
        tt.play();
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

        for (int i = 0; i < ordered.size(); i++) {
            Player p = ordered.get(i);
            int handSize = p.getHand().size();

            int seatIndex = getPlayerSeatIndex(p);
            double[] targetPos = getBotSeatScenePosition(seatIndex);

            double targetWidth = (seatIndex == 0) ? 96 : 48;
            double targetHeight = (seatIndex == 0) ? 136 : 68;

            for (int c = 0; c < handSize; c++) {
                final double tw = targetWidth;
                final double th = targetHeight;
                final double tx = targetPos[0];
                final double ty = targetPos[1];

                KeyFrame cardFrame = new KeyFrame(Duration.millis(delayOffset * 85), e -> {
                    ImageView dealingCard = loadCardBack(tw, th);
                    dealingCard.setLayoutX(DECK_CX - (tw / 2));
                    dealingCard.setLayoutY(DECK_CY - (th / 2));
                    dealingCard.setOpacity(0.0);
                    dealingCard.getStyleClass().add("card-style-default");

                    rootPane.getChildren().add(dealingCard);

                    TranslateTransition fly = new TranslateTransition(Duration.millis(320), dealingCard);
                    fly.setFromX(0);
                    fly.setFromY(0);
                    fly.setToX(tx - DECK_CX);
                    fly.setToY(ty - DECK_CY);

                    FadeTransition reveal = new FadeTransition(Duration.millis(80), dealingCard);
                    reveal.setToValue(1.0);

                    ParallelTransition animationGroup = new ParallelTransition(fly, reveal);
                    animationGroup.setOnFinished(evt -> rootPane.getChildren().remove(dealingCard));
                    animationGroup.play();
                });

                dealerTimeline.getKeyFrames().add(cardFrame);
                delayOffset++;
            }
        }

        double totalRuntimeMs = delayOffset > 0 ? ((delayOffset - 1) * 85) + 320 + 20 : 10;

        KeyFrame finishingFrame = new KeyFrame(Duration.millis(totalRuntimeMs), e -> {
            if (after != null) after.run();
        });

        dealerTimeline.getKeyFrames().add(finishingFrame);
        dealerTimeline.play();
    }

    private void showRoundAnnouncement(Runnable after) {
        String rank = game.getState().getDeclaredSymbol();
        showAnnouncement(rank.toUpperCase() + "'S TABLE", after);
    }

    private void showAnnouncement( String text, Runnable after) {
        announcementLabel.setText(text);
        announcementOverlay.setOpacity(0);
        announcementOverlay.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(450), announcementOverlay);
        fadeIn.setToValue(1.0);

        fadeIn.setOnFinished(e -> runDelayed(() -> {
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(450), announcementOverlay);
                    fadeOut.setToValue(0);
                    fadeOut.setOnFinished(ev -> {
                        announcementOverlay.setVisible(false);
                        if (after != null) {after.run();}
                    });
                    fadeOut.play();
                }, 1500)
        );
        fadeIn.play();
    }

    private void showBluffAlert(Runnable after) {
        bluffOverlay.setOpacity(0);
        bluffOverlay.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), bluffOverlay);
        fadeIn.setToValue(1.0);
        ScaleTransition scale = new ScaleTransition(Duration.millis(250), bluffLabel);
        scale.setFromX(0.4);
        scale.setToX(1.0);
        scale.setFromY(0.4);
        scale.setToY(1.0);

        ParallelTransition show = new ParallelTransition(fadeIn, scale);
        show.setOnFinished(e -> runDelayed(() -> {
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

    private void pulseNode(VBox node) {
        ScaleTransition existing = (ScaleTransition) node.getProperties().get("active-pulse");
        if (existing != null) {
            return;
        }

        ScaleTransition st = new ScaleTransition(Duration.millis(900), node);
        st.setFromX(1.0);
        st.setToX(1.04);
        st.setFromY(1.0);
        st.setToY(1.04);
        st.setAutoReverse(true);
        st.setCycleCount(Animation.INDEFINITE);

        node.getProperties().put("active-pulse", st);
        st.play();
    }

    private void stopPulseNode(VBox node) {
        ScaleTransition existing = (ScaleTransition) node.getProperties().get("active-pulse");
        if (existing != null) {
            existing.stop();
            node.getProperties().remove("active-pulse");
        }
        node.setScaleX(1.0);
        node.setScaleY(1.0);
    }

    private void animateGhostsToDeck(List<double[]> sourcePositions, Runnable after) {
        if (sourcePositions.isEmpty()) {
            after.run();
            return;
        }
        int[] done = {0};

        for (double[] pos : sourcePositions) {
            double cx = pos[0];
            double cy = pos[1];

            ImageView ghost = loadCardBack(96, 136);
            ghost.setLayoutX(cx - 48);
            ghost.setLayoutY(cy - 68);
            ghost.getStyleClass().add("card-style-default");
            rootPane.getChildren().add(ghost);

            TranslateTransition fly = new TranslateTransition(Duration.millis(380), ghost);
            fly.setToX(DECK_CX - cx);
            fly.setToY(DECK_CY - cy);

            FadeTransition fade = new FadeTransition(Duration.millis(380), ghost);
            fade.setToValue(0.1);

            ScaleTransition shrink = new ScaleTransition(Duration.millis(380), ghost);
            shrink.setToX(0.4);
            shrink.setToY(0.4);

            ParallelTransition anim = new ParallelTransition(fly, fade, shrink);
            anim.setOnFinished(e -> {
                rootPane.getChildren().remove(ghost);
                if (++done[0] == sourcePositions.size()) after.run();
            });
            anim.play();
        }
    }

    private List<double[]> getHandCardPositions(List<Card> toPlay) {
        List<double[]> positions = new ArrayList<>();
        for (var node : handBox.getChildren()) {
            if (node instanceof StackPane sp) {
                Card c = (Card) sp.getUserData();
                if (toPlay.stream().anyMatch(t -> t == c)) {
                    var b = sp.localToScene(sp.getBoundsInLocal());
                    positions.add(new double[]{
                            b.getMinX() + b.getWidth() / 2,
                            b.getMinY() + b.getHeight() / 2
                    });
                }
            }
        }
        return positions;
    }

    private double[] getBotSeatScenePosition(int seatIndex) {
        return switch (seatIndex) {
            case 1 -> new double[]{130, 480};
            case 2 -> new double[]{640, 85};
            case 3 -> new double[]{1150, 480};
            default -> new double[]{640, 770};
        };
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
        String rank = game.getState().getDeclaredSymbol().toUpperCase();
        startActionMessageTimer(name + " PLAYS " + toPlay.size() + " " + rank + "(s)");

        List<double[]> positions = getHandCardPositions(toPlay);

        animateGhostsToDeck(positions, () -> {
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

     // Helper method to toggle between the modal sub-views seamlessly
        private void showModalSubView(VBox activeSubView) {
            cupModal.setVisible(true);

            // Loop through and toggle visibility/management states
            List.of(cupModalOverlay, eliminationBox, victoryBox).forEach(view -> {
                boolean isTarget = view.equals(activeSubView);
                view.setVisible(isTarget);
                view.setManaged(isTarget);
            });
        }

        // ── DYNAMIC CUP MODAL LOGIC ───────────────────────────────────────────────
        private void showCupModal(Player loser) {
            cupModalTitle.setText("\"" + loser.getUsername().toUpperCase() + "\" WAS WRONG!\n" + "SELECT A CUP TO DRINK CONSEQUENCES:");

            // Clear dynamic elements safely without wiping structure
            cupModalOverlay.getChildren().removeIf(node -> node instanceof HBox);

            HBox cupsRow = new HBox(50);
            cupsRow.setAlignment(Pos.CENTER);
            cupsRow.setPadding(new Insets(20, 40, 20, 40));
            System.out.println(game.getState().getPoisonedCup());

            for (int i = 1; i < 4; i++) {
                final int index = i;
                ImageView cupImg = loadCupImage(index, 84, 144);
                StackPane cupPane = new StackPane(cupImg);
                cupPane.setUserData(index);
                cupPane.getStyleClass().addAll("card-container", "card-style-default");

                if (loser.isHuman()) {
                    cupPane.setOnMouseEntered(e -> {
                        animateCardLift(cupPane, -14);
                        cupPane.getStyleClass().removeAll("card-style-default");
                        cupPane.getStyleClass().add("card-style-hover");
                    });
                    cupPane.setOnMouseExited(e -> {
                        animateCardLift(cupPane, 0);
                        cupPane.getStyleClass().removeAll("card-style-hover");
                        cupPane.getStyleClass().add("card-style-default");
                    });
                    cupPane.setOnMouseClicked(e -> handleCupPickedSequence(index, cupPane, cupsRow, loser));
                }
                cupsRow.getChildren().add(cupPane);
            }

            cupModalOverlay.getChildren().add(cupsRow);
            showModalSubView(cupModalOverlay);

            if (!loser.isHuman()) {
                handleBotPoisonPick(cupsRow, loser);
            }
        }

        // ── GAME TERMINATION AND ELIMINATION MODAL LAYOUTS ────────────────────────
        private void showEliminationOrGameOverOverlay(boolean wasMultiplayerHumanGame) {
            showModalSubView(eliminationBox);
        }

        private void showFinalWinnerOverlay() {
            List<Player> remaining = game.getState().getActivePlayers().stream().filter(p -> !p.isEliminated()).toList();
            String winnerName = remaining.isEmpty() ? "PLAYER" : remaining.get(0).getUsername().toUpperCase();

            victoryLabel.setText("🏆  VICTORY  🏆\n\n" + winnerName + " IS THE WINNER!");
            showModalSubView(victoryBox);
        }

        // ── NEW FXML INTERACTIVE BUTTON HANDLERS ──────────────────────────────────
        @FXML
        public void onSpectateClicked() {
            isSpectatingMode = true;
            cupModal.setVisible(false);
            updateUI();
        }

        @FXML
        public void onMainMenuClicked() {
            try {
                App.switchScene("Setup", game);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    private void handleCupPickedSequence(int chosenIndex, StackPane chosenPane, HBox cupsRow, Player loser) {
        cupsRow.setDisable(true);

        chosenPane.getStyleClass().removeAll("card-style-default", "card-style-hover");
        chosenPane.getStyleClass().add("card-style-selected");
        chosenPane.setTranslateY(-20);

        runDelayed(() -> {
            cupModal.setVisible(false);

            int livesBefore = loser.getLives();
            game.pickPoison(chosenIndex);

            if (loser.getLives() < livesBefore) {
                showAnnouncement("💔 " + loser.getUsername().toUpperCase() + " LOST A LIFE! 💔", this::updateUI);

            } else {
                showAnnouncement("✨ " + loser.getUsername().toUpperCase() + " SURVIVED! ✨", this::updateUI);
            }
        }, 900);
    }

    private void handleBotPoisonPick(HBox cupsRow, Player loser) {
        int botChoice = new Random().nextInt(3);
        runDelayed(() -> {
            if (botChoice < cupsRow.getChildren().size()) {
                StackPane targetPane = (StackPane) cupsRow.getChildren().get(botChoice);
                targetPane.getStyleClass().removeAll("card-style-default");
                targetPane.getStyleClass().add("card-style-selected");
                targetPane.setTranslateY(-20);
            }
            runDelayed(() -> {
                cupModal.setVisible(false);

                int livesBefore = loser.getLives();
                game.pickPoison(botChoice);

                if (loser.getLives() < livesBefore) {
                    showAnnouncement("💔 " + loser.getUsername().toUpperCase() + " LOST A LIFE! 💔", this::updateUI);
                } else {
                    showAnnouncement("✨ " + loser.getUsername().toUpperCase() + " SURVIVED! ✨", this::updateUI);
                }
            }, 1000);

        }, 1200);
    }

    @FXML
    public void onSettingsClicked() {
        System.out.println("SETTINGS");
    }

    // ── BOT MANAGEMENT ────────────────────────────────────────────────────────
    private void handleBotTurn() {
        Player current = game.getState().getCurrentPlayer();
        final int finalSeatIndex = getPlayerSeatIndex(current);
        final Player currentBot = current;

        runDelayed(() -> {
            List<Card> played = game.playBotTurn();

            if (!played.isEmpty()) {
                if (currentBot != null) {
                    String name = currentBot.getUsername().toUpperCase();
                    String rank = game.getState().getDeclaredSymbol().toUpperCase();
                    startActionMessageTimer(name + " PLAYS " + played.size() + " " + rank + "(s)");
                }
                List<double[]> positions = new ArrayList<>();
                double[] base = getBotSeatScenePosition(finalSeatIndex);
                for (int i = 0; i < played.size(); i++) {
                    positions.add(new double[]{
                            base[0] + (i - played.size() / 2.0) * 16,
                            base[1]
                    });
                }
                animateGhostsToDeck(positions, this::updateUI);
            } else {
                showBluffAlert(this::updateUI);
            }
        }, 2000);
    }

    // ── IMAGE RETRIEVAL ───────────────────────────────────────────────────────
    private ImageView loadCupImage(int index, double w, double h) {
        return loadImage("/com/leonidasAndrei/asserta/images/bottles/" + index + ".png", w, h);
    }

    private ImageView loadCardBack(double w, double h) {
        return loadImage("/com/leonidasAndrei/asserta/images/cards/back/0.png", w, h);
    }

    private ImageView loadCardFront(Card card, double w, double h) {
        return loadImage(
                "/com/leonidasAndrei/asserta/images/cards/front/" +
                        card.getSuit().name().toLowerCase() + "/" + card.getRank() + ".png",
                w, h
        );
    }

    private ImageView loadImage(String path, double w, double h) {
        InputStream is = getClass().getResourceAsStream(path);
        ImageView iv = new ImageView();
        if (is == null) {
            System.out.println("❌ MISSING IMAGE PATH: " + path);
            iv.setFitWidth(w);
            iv.setFitHeight(h);
            return iv;
        }
        iv.setImage(new Image(is, w, h, false, false));
        iv.setFitWidth(w);
        iv.setFitHeight(h);
        iv.setPreserveRatio(false);
        iv.setSmooth(true);
        return iv;
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────
    private int getPlayerSeatIndex(Player p) {
        for (int i = 0; i < seatsPane.getChildren().size(); i++) {
            if (seatsPane.getChildren().get(i).getUserData() == p) {
                return i;
            }
        }
        return 0;
    }

    private void startActionMessageTimer(String msg) {
        lastActionMessage = msg;
        messageLabel.setText(lastActionMessage);

        if (messageTimer != null) {
            messageTimer.stop();
        }

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
        l.getStyleClass().add("name-label-node");
        l.getStyleClass().add(isActive ? "name-node-active" : "name-node-inactive");
        return l;
    }

    private HBox makeBotCards(Player p) {
        HBox row = new HBox(4);
        row.setAlignment(Pos.CENTER);
        for (int j = 0; j < p.getHand().size(); j++) {
            ImageView back = loadCardBack(48, 68);
            back.getStyleClass().add("card-style-default");
            row.getChildren().add(back);
        }
        return row;
    }

    private List<Player> humanFirst(List<Player> players) {
        int hi = 0;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).isHuman()) {
                hi = i;
                break;
            }
        }
        List<Player> out = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            out.add(players.get((hi + i) % players.size()));
        }
        return out;
    }

    private Player humanPlayer() {
        return game.getState().getActivePlayers()
                .stream().filter(Player::isHuman)
                .findFirst().orElse(null);
    }

    private void runDelayed(Runnable action, long millis) {
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Platform.runLater(action);
        });
        t.setDaemon(true);
        t.start();
    }
}