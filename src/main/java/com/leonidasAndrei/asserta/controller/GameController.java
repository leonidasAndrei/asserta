package com.leonidasAndrei.asserta.controller;

import com.leonidasAndrei.asserta.App;
import com.leonidasAndrei.asserta.model.Card;
import com.leonidasAndrei.asserta.model.Game;
import com.leonidasAndrei.asserta.model.GameState.GamePhase;
import com.leonidasAndrei.asserta.model.Player;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
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

    private Game game;
    private final List<Card> selectedCards = new ArrayList<>();
    private boolean isDealingAnimationRunning = false;
    private boolean isInitialStartDone = false; // Prevents the initial Round 0 loop

    private static final double DECK_CX     = 640;
    private static final double DECK_CY     = 480;

    // ── INIT ──────────────────────────────────────────────────────────────────
    public void initGame(Game game) {
        this.game = game;
        this.selectedCards.clear();
        this.isDealingAnimationRunning = false;
        this.isInitialStartDone = false; // Reset state tracking for a new game window

        updateUI();
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────
    public void updateUI() {
        if (game == null) return;

        // FIX 1: Safely catch Round 0 exactly once without creating an infinite loop
        if (game.getState().getRound() == 0 && !isInitialStartDone) {
            isInitialStartDone = true;
            startNewRoundSequence();
            return;
        }

        GamePhase phase   = game.getState().getPhase();
        Player    current = game.getState().getCurrentPlayer();
        String    rank    = game.getState().getDeclaredSymbol();

        declaredRankLabel.setText(rank.isEmpty() ? "" : rank.toUpperCase() + "'S TABLE");
        roundLabel.setText("ROUND " + (game.getState().getRound() + 1));
        messageLabel.setText("");

        renderDeckZone();
        renderSeatsExcludingCards(isDealingAnimationRunning);
        renderHumanHand();

        boolean isHumanTurn = current != null && current.isHuman();
        boolean canBluff    = phase == GamePhase.WAITING && game.getState().getNumberOfTurns() > 0;

        // FIX 2: Keep the action bar (your hand container) visible during bot turns.
        // Only hide it completely while cards are actively flying through the air.
        actionBar.setVisible(!isDealingAnimationRunning);
        callBluffButton.setVisible(!isDealingAnimationRunning && isHumanTurn && canBluff);
        playButton.setVisible(!isDealingAnimationRunning && isHumanTurn);
        playButton.setDisable(selectedCards.isEmpty());

        switch (phase) {
            case NEW_ROUND -> {
                if (!isDealingAnimationRunning) {
                    startNewRoundSequence();
                }
            }
            case GAME_OVER -> {
                try { App.switchScene("GameOver", game); }
                catch (IOException e) { e.printStackTrace(); }
            }
            default -> {
                if (current != null && !current.isHuman() && phase != GamePhase.PICKING_POISON && !isDealingAnimationRunning) {
                    handleBotTurn();
                }
                if (phase == GamePhase.PICKING_POISON && !isDealingAnimationRunning) {
                    Player loser = game.getState().getLoser();
                    if (loser != null && !loser.isHuman()) handleBotPoisonPick();
                }
            }
        }
    }

    // ── CONTROLLED SEQUENCING ─────────────────────────────────────────────────
    private void startNewRoundSequence() {
        selectedCards.clear();
        isDealingAnimationRunning = true;

        // Force buttons away instantly so they can't be clicked during the 600ms pre-delay
        actionBar.setVisible(false);
        callBluffButton.setVisible(false);
        playButton.setVisible(false);

        runDelayed(() -> {
            // 1. Initialize backend game engine states and deal cards
            game.startGame();

            // 2. Play the visual splash overlays cleanly
            showRoundAnnouncement(() -> {

                // 3. Play the staggered flying card nodes
                animateCardDistribution(() -> {
                    // 4. Once animation completes fully, open UI and let bots act
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
            r.setStroke(Color.web("#748C45"));
            r.setStrokeWidth(2);
            r.getStrokeDashArray().addAll(8.0, 5.0);
            r.setArcWidth(8); r.setArcHeight(8);
            deckZone.getChildren().add(r);
            return;
        }

        double[] rots = {-18, 12, -8, 22,-15,  5,-25, 17, -6, 20,-12,  9};
        double[] txs  = { -8,  6, -3, 10, -6,  2,-11,  7, -2,  9, -5,  4};
        double[] tys  = {  4, -7,  9, -4,  7,-10,  3, -8, 11, -3,  6, -9};
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

    // ── SEATS ─────────────────────────────────────────────────────────────────
    private void renderSeatsExcludingCards(boolean hideAllCards) {
        seatsPane.getChildren().clear();
        List<Player> ordered = humanFirst(game.getState().getActivePlayers());
        Player current = game.getState().getCurrentPlayer();

        double boxWidth = 240;
        double boxHeight = 110;

        for (int i = 0; i < ordered.size() && i < 4; i++) {
            Player  p        = ordered.get(i);
            boolean isActive = p.equals(current);
            boolean isHuman  = p.isHuman();
            boolean isTop    = (i == 2);
            boolean isLeft   = (i == 1);
            boolean isRight  = (i == 3);

            Label name  = makeNameLabel(p.getUsername(), isActive);
            HBox  cards = (isHuman || hideAllCards) ? new HBox() : makeBotCards(p);

            VBox seat = new VBox(6);
            seat.setAlignment(Pos.CENTER);
            seat.getStyleClass().add("seat-box");

            seat.setMinWidth(boxWidth);   seat.setPrefWidth(boxWidth);   seat.setMaxWidth(boxWidth);
            seat.setMinHeight(boxHeight); seat.setPrefHeight(boxHeight); seat.setMaxHeight(boxHeight);

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
            }

            if (isLeft) {
                seat.setRotate(90);
                seat.setLayoutX(130 - (boxWidth / 2));
                seat.setLayoutY(480 - (boxHeight / 2));
            } else if (isRight) {
                seat.setRotate(-90);
                seat.setLayoutX(1150 - (boxWidth / 2));
                seat.setLayoutY(480 - (boxHeight / 2));
            } else if (isTop) {
                seat.setLayoutX(640 - (boxWidth / 2));
                seat.setLayoutY(85 - (boxHeight / 2));
            } else {
                seat.setLayoutX(640 - (boxWidth / 2));
                seat.setLayoutY(932 - (boxHeight / 2));
            }
            seatsPane.getChildren().add(seat);
        }
    }

    // ── HUMAN HAND ────────────────────────────────────────────────────────────
    private void renderHumanHand() {
        handBox.getChildren().clear();
        if (isDealingAnimationRunning) return;

        Player human = humanPlayer();
        if (human == null) return;

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
            double[] targetPos = getBotSeatScenePosition(i);

            double targetWidth = (i == 0) ? 96 : 48;
            double targetHeight = (i == 0) ? 136 : 68;

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
                    fly.setFromX(0); fly.setFromY(0);
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
        announcementLabel.setText("⚔  " + rank.toUpperCase() + "'S TABLE  ⚔");
        announcementOverlay.setOpacity(0);
        announcementOverlay.setVisible(true);

        // Standard sequence layout
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), announcementOverlay);
        fadeIn.setToValue(1.0);
        fadeIn.setOnFinished(e -> runDelayed(() -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), announcementOverlay);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> {
                announcementOverlay.setVisible(false);
                if (after != null) after.run();
            });
            fadeOut.play();
        }, 1800));
        fadeIn.play();
    }

    private void showBluffAlert(Runnable after) {
        bluffOverlay.setOpacity(0);
        bluffOverlay.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), bluffOverlay);
        fadeIn.setToValue(1.0);
        ScaleTransition scale = new ScaleTransition(Duration.millis(250), bluffLabel);
        scale.setFromX(0.4); scale.setToX(1.0);
        scale.setFromY(0.4); scale.setToY(1.0);

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
        ScaleTransition st = new ScaleTransition(Duration.millis(900), node);
        st.setFromX(1.0);  st.setToX(1.04);
        st.setFromY(1.0);  st.setToY(1.04);
        st.setAutoReverse(true);
        st.setCycleCount(Animation.INDEFINITE);
        st.play();
    }

    private void animateGhostsToDeck(List<double[]> sourcePositions, Runnable after) {
        if (sourcePositions.isEmpty()) { after.run(); return; }
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
            shrink.setToX(0.4); shrink.setToY(0.4);

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
                            b.getMinX() + b.getWidth()  / 2,
                            b.getMinY() + b.getHeight() / 2
                    });
                }
            }
        }
        return positions;
    }

    private double[] getBotSeatScenePosition(int seatIndex) {
        return switch (seatIndex) {
            case 1 -> new double[]{130,  480};
            case 2 -> new double[]{640,  85};
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
            Player loser = game.getState().getLoser();
            if (loser != null) showCupModal(loser);
            else updateUI();
        });
    }

    @FXML public void onWrongClicked() {
        cupModal.setVisible(false);
        game.pickPoison(game.getState().getPoisonedCup());
        updateUI();
    }

    @FXML public void onRightClicked() {
        cupModal.setVisible(false);
        game.pickPoison((game.getState().getPoisonedCup() + 1) % 3);
        updateUI();
    }

    private void showCupModal(Player loser) {
        cupModalTitle.setText(
                "\"" + loser.getUsername().toUpperCase() + "\" GOT CAUGHT BLUFFING!\n" +
                        "CHOOSE THEIR CONSEQUENCE CUP CAREFULLY:"
        );
        cupModal.setVisible(true);
    }

    @FXML public void onSettingsClicked() {
        System.out.println("SETTINGS");
    }

    // ── BOT MANAGEMENT ────────────────────────────────────────────────────────
    private void handleBotTurn() {
        List<Player> ordered = humanFirst(game.getState().getActivePlayers());
        Player current = game.getState().getCurrentPlayer();
        int seatIndex = 0;
        for (int i = 0; i < ordered.size(); i++) {
            if (ordered.get(i) == current) { seatIndex = i; break; }
        }
        final int finalSeatIndex = seatIndex;

        runDelayed(() -> {
            List<Card> played = game.playBotTurn();

            if (!played.isEmpty()) {
                List<double[]> positions = new ArrayList<>();
                double[] base = getBotSeatScenePosition(finalSeatIndex);
                for (int i = 0; i < played.size(); i++) {
                    positions.add(new double[]{
                            base[0] + (i - played.size() / 2.0) * 16,
                            base[1]
                    });
                }
                animateGhostsToDeck(positions, () -> {
                    if (game.getState().getPhase() == GamePhase.PICKING_POISON) {
                        Player loser = game.getState().getLoser();
                        if (loser != null && loser.isHuman()) showCupModal(loser);
                        else handleBotPoisonPick();
                    } else {
                        updateUI();
                    }
                });
            } else {
                if (game.getState().getPhase() == GamePhase.PICKING_POISON) {
                    Player loser = game.getState().getLoser();
                    if (loser != null && loser.isHuman()) showCupModal(loser);
                    else handleBotPoisonPick();
                } else {
                    updateUI();
                }
            }
        }, 2000);
    }

    private void handleBotPoisonPick() {
        runDelayed(() -> {
            game.pickPoison(new Random().nextInt(3));
            updateUI();
        }, 1500);
    }

    // ── IMAGE RETRIEVAL ───────────────────────────────────────────────────────
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
            iv.setFitWidth(w); iv.setFitHeight(h);
            return iv;
        }
        iv.setImage(new Image(is, w, h, false, false));
        iv.setFitWidth(w); iv.setFitHeight(h);
        iv.setPreserveRatio(false);
        iv.setSmooth(true);
        return iv;
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────
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
            if (players.get(i).isHuman()) { hi = i; break; }
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
            try { Thread.sleep(millis); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            Platform.runLater(action);
        });
        t.setDaemon(true);
        t.start();
    }
}